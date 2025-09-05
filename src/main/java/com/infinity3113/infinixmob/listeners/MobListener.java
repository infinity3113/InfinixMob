package com.infinity3113.infinixmob.listeners;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mobs.CustomMob;
import com.infinity3113.infinixmob.items.CustomItem;
import com.infinity3113.infinixmob.utils.PlaceholderParser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MobListener implements Listener {

    private final InfinixMob plugin;
    private final Gson gson = new Gson();
    private static final Pattern NUMERIC_CONDITION_PATTERN = Pattern.compile("([a-zA-Z_]+)\\s*([<>=!]+)\\s*([0-9.]+)");
    private static final Pattern GENERIC_PATTERN = Pattern.compile("([a-zA-Z_]+)\\{(.+)\\}");
    private final Set<UUID> combatCooldown = new HashSet<>();

    public MobListener(InfinixMob plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        
        LivingEntity entity = (LivingEntity) event.getEntity();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (entity.isValid() && entity.hasMetadata("InfinixMobID")) {
                handleTrigger("~onSpawn", entity, null);
            }
        }, 1L);
    }

    public void handleTimerTick(LivingEntity mob) {
        PersistentDataContainer container = mob.getPersistentDataContainer();
        int currentTick = container.getOrDefault(plugin.TIMER_KEY, PersistentDataType.INTEGER, 0) + 1;
        container.set(plugin.TIMER_KEY, PersistentDataType.INTEGER, currentTick);

        LivingEntity target = null;
        if (mob instanceof Creature) {
            target = ((Creature) mob).getTarget();
        }

        handleTrigger("~onTimer", mob, target);
    }

    public void handleTrigger(String triggerName, LivingEntity caster, Entity target) {
        if (!caster.hasMetadata("InfinixMobID")) return;
        String mobId = caster.getMetadata("InfinixMobID").get(0).asString();
        Optional<CustomMob> mobOpt = plugin.getMobManager().getMob(mobId);
        if (!mobOpt.isPresent()) return;

        List<Map<?, ?>> skillRules = mobOpt.get().getConfig().getMapList("Skills");
        if (skillRules == null || skillRules.isEmpty()) return;

        for (Map<?, ?> rule : skillRules) {
            String trigger = (String) rule.get("trigger");
            if (trigger == null) continue;

            String[] triggerParts = trigger.split(":");
            String baseTrigger = triggerParts[0];

            if (baseTrigger.equalsIgnoreCase("~onTimer")) {
                if (triggerName.equalsIgnoreCase("~onTimer")) {
                    try {
                        int interval = Integer.parseInt(triggerParts[1]);
                        PersistentDataContainer container = caster.getPersistentDataContainer();
                        int currentTick = container.getOrDefault(plugin.TIMER_KEY, PersistentDataType.INTEGER, 0);

                        if (currentTick >= interval) {
                            container.set(plugin.TIMER_KEY, PersistentDataType.INTEGER, 0);
                            executeSkillLogic(rule, caster, target);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            } else if (baseTrigger.equalsIgnoreCase(triggerName)) {
                executeSkillLogic(rule, caster, target);
            }
        }
    }

    private void executeSkillLogic(Map<?, ?> rule, LivingEntity caster, Entity target) {
        double chance = 1.0;
        if (rule.get("chance") instanceof Number) {
            chance = ((Number) rule.get("chance")).doubleValue();
        }
        if (Math.random() > chance) return;

        Object conditions = rule.get("conditions");
        if (conditions instanceof List) {
            boolean allConditionsMet = true;
            for (String condition : (List<String>) conditions) {
                if (!checkCondition(condition, caster, target)) {
                    allConditionsMet = false;
                    break;
                }
            }
            if (!allConditionsMet) return;
        } else if (conditions instanceof String) {
            if (!checkCondition((String) conditions, caster, target)) return;
        }

        String skillId = (String) rule.get("skill");
        if (skillId != null) {
            plugin.getSkillManager().executeSkill(skillId, caster, target, null);
        }
    }
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!projectile.hasMetadata("InfinixMob_ProjectileSkill")) return;
        
        String skillOnHitId = projectile.getMetadata("InfinixMob_ProjectileSkill").get(0).asString();
        UUID ownerUUID = UUID.fromString(projectile.getMetadata("InfinixMob_Owner").get(0).asString());
        
        // --- INICIO DE LA CORRECCIÓN ---
        // Recuperamos el ID de la habilidad original. Si no existe, usamos la habilidad de impacto como fallback.
        String parentSkillId = skillOnHitId; 
        if(projectile.hasMetadata("InfinixMob_ParentSkill")){
            parentSkillId = projectile.getMetadata("InfinixMob_ParentSkill").get(0).asString();
        }
        // --- FIN DE LA CORRECCIÓN ---
        
        Entity ownerEntity = plugin.getServer().getEntity(ownerUUID);
        if (!(ownerEntity instanceof LivingEntity) || ownerEntity.isDead()) return;
        
        LivingEntity owner = (LivingEntity) ownerEntity;
        
        Location impactLocation;
        if (event.getHitBlock() != null) {
            impactLocation = event.getHitBlock().getLocation();
        } else if (event.getHitEntity() != null) {
            impactLocation = event.getHitEntity().getLocation();
        } else {
            return;
        }
        
        ArmorStand targetDummy = (ArmorStand) impactLocation.getWorld().spawnEntity(impactLocation, EntityType.ARMOR_STAND);
        targetDummy.setVisible(false);
        targetDummy.setGravity(false);
        targetDummy.setMarker(true);
        
        // --- INICIO DE LA CORRECCIÓN ---
        // Pasamos ambos IDs de habilidad al SkillManager.
        plugin.getSkillManager().executeSkill(skillOnHitId, parentSkillId, owner, targetDummy, null);
        // --- FIN DE LA CORRECCIÓN ---
        
        plugin.getServer().getScheduler().runTaskLater(plugin, targetDummy::remove, 20L);
    }

    @EventHandler
    public void onEntityShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof LivingEntity) || !event.getEntity().hasMetadata("InfinixMobID")) return;
        
        LivingEntity caster = (LivingEntity) event.getEntity();
        String mobId = caster.getMetadata("InfinixMobID").get(0).asString();

        plugin.getMobManager().getMob(mobId).ifPresent(customMob -> {
            List<Map<?, ?>> skillRules = customMob.getConfig().getMapList("Skills");
            if (skillRules == null) return;

            for (Map<?, ?> rule : skillRules) {
                String trigger = (String) rule.get("trigger");
                if (trigger != null && trigger.equalsIgnoreCase("~onShoot")) {
                    
                    double chance = 1.0;
                    if (rule.get("chance") instanceof Number) {
                        chance = ((Number) rule.get("chance")).doubleValue();
                    }
                    if (Math.random() > chance) {
                        continue;
                    }

                    String skillId = (String) rule.get("skill");
                    if (skillId != null) {
                        event.getProjectile().setMetadata("InfinixMob_ProjectileSkill", new FixedMetadataValue(plugin, skillId));
                        event.getProjectile().setMetadata("InfinixMob_Owner", new FixedMetadataValue(plugin, caster.getUniqueId().toString()));
                    }
                }
            }
        });
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity victim = (LivingEntity) event.getEntity();
        
        if (event.getCause() == EntityDamageEvent.DamageCause.LAVA && victim.hasMetadata("InfinixMob_LavaImmunity")) {
            event.setCancelled(true);
            return;
        }

        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) event;
            Entity damager = damageByEntityEvent.getDamager();

            if (victim.hasMetadata("InfinixMobID") && damager instanceof Player) {
                plugin.getThreatManager().addThreat(victim, (Player) damager, damageByEntityEvent.getFinalDamage());
            }
            if (damager.hasMetadata("InfinixMobID")) {
                handleTrigger("~onAttack", (LivingEntity) damager, victim);
                handleEnterCombat((LivingEntity) damager, victim);
            }
            if (victim.hasMetadata("InfinixMobID")) {
                handleTrigger("~onDamaged", victim, damager);
                handleEnterCombat(victim, damager);
                if (damageByEntityEvent.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                    handleTrigger("~onDamagedByProjectile", victim, damager);
                }
            }
        } else {
            if (victim.hasMetadata("InfinixMobID")) {
                handleTrigger("~onDamaged", victim, null);
            }
        }
        if (victim.hasMetadata("InfinixMobID") && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            handleTrigger("~onLand", victim, null);
        }
    }

    private void handleEnterCombat(LivingEntity mob, Entity target) {
        if (!combatCooldown.contains(mob.getUniqueId())) {
            handleTrigger("~onEnterCombat", mob, target);
            combatCooldown.add(mob.getUniqueId());
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        event.getEntity().getPersistentDataContainer().remove(plugin.TIMER_KEY);
        
        LivingEntity deceased = event.getEntity();
        if (deceased.hasMetadata("InfinixMobID")) {
            plugin.getWeakPointManager().removeWeakPointsFor(deceased);
            plugin.getMobManager().unregisterMob(deceased);
            combatCooldown.remove(deceased.getUniqueId());
        }
        if (deceased.hasMetadata("InfinixMob_PreventDrops")) {
            event.getDrops().clear();
        }
        if (deceased.hasMetadata("InfinixMobID")) {
            String mobId = deceased.getMetadata("InfinixMobID").get(0).asString();
            plugin.getMobManager().getMob(mobId).ifPresent(customMob -> {
                if (customMob.getConfig().isList("drops")) {
                    event.getDrops().clear();
                    List<Map<?, ?>> dropList = customMob.getConfig().getMapList("drops");
                    for (Map<?, ?> dropInfo : dropList) {
                        double chance = 1.0;
                        if (dropInfo.get("chance") instanceof Number) {
                            chance = ((Number) dropInfo.get("chance")).doubleValue();
                        }
                        if (Math.random() <= chance) {
                            String itemIdentifier = (String) dropInfo.get("item");
                            if (itemIdentifier == null) continue;
                            int amount = 1;
                            Object amountObj = dropInfo.get("amount");
                            if (amountObj instanceof Number) {
                                amount = ((Number) amountObj).intValue();
                            } else if (amountObj instanceof List && ((List<?>) amountObj).size() == 2) {
                                List<?> range = (List<?>) amountObj;
                                int min = ((Number) range.get(0)).intValue();
                                int max = ((Number) range.get(1)).intValue();
                                amount = ThreadLocalRandom.current().nextInt(min, max + 1);
                            }
                            Optional<ItemStack> itemStackOptional = plugin.getItemManager().getItem(itemIdentifier)
                                .map(customItem -> customItem.buildItemStack())
                                .or(() -> {
                                    Material material = Material.matchMaterial(itemIdentifier.toUpperCase());
                                    return material != null ? Optional.of(new ItemStack(material)) : Optional.empty();
                                });
                            if (itemStackOptional.isPresent()) {
                                ItemStack itemStack = itemStackOptional.get();
                                if (itemStack.getType() != Material.AIR && amount > 0) {
                                    itemStack.setAmount(amount);
                                    event.getDrops().add(itemStack);
                                }
                            }
                        }
                    }
                }
            });
        }
        if (deceased.getKiller() != null && deceased.getKiller().hasMetadata("InfinixMobID")) {
            handleTrigger("~onKill", deceased.getKiller(), deceased);
        }
        if (deceased.hasMetadata("InfinixMobID")) {
            handleTrigger("~onDeath", deceased, deceased.getKiller());
        }
    }
    
    @EventHandler
    public void onCombust(EntityCombustEvent event) {
        if (event.getEntity() instanceof LivingEntity && event.getEntity().hasMetadata("InfinixMobID")) {
            handleTrigger("~onCombust", (LivingEntity) event.getEntity(), null);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().hasMetadata("InfinixMobID")) {
            handleTrigger("~onInteract", (LivingEntity) event.getRightClicked(), event.getPlayer());
        }
    }
    
    @EventHandler
    public void onEntityHeal(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof LivingEntity && event.getEntity().hasMetadata("InfinixMobID")) {
            handleTrigger("~onHeal", (LivingEntity) event.getEntity(), null);
        }
    }
    
    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (event.getTarget() == null || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        Entity casterEntity = event.getEntity();
        Entity targetEntity = event.getTarget();
        if (casterEntity.hasMetadata("InfinixMob_Faction") && targetEntity.hasMetadata("InfinixMob_Faction")) {
            List<MetadataValue> casterFactionMeta = casterEntity.getMetadata("InfinixMob_Faction");
            List<MetadataValue> targetFactionMeta = targetEntity.getMetadata("InfinixMob_Faction");
            if (!casterFactionMeta.isEmpty() && !targetFactionMeta.isEmpty()) {
                String casterFaction = casterFactionMeta.get(0).asString();
                String targetFaction = targetFactionMeta.get(0).asString();
                if (casterFaction != null && casterFaction.equals(targetFaction)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (event.getEntity().hasMetadata("InfinixMobID")) {
            handleTrigger("~onTarget", (LivingEntity) event.getEntity(), event.getTarget());
        }
    }

    @EventHandler
    public void onTeleport(EntityTeleportEvent event) {
        if (event.getEntity() instanceof LivingEntity && event.getEntity().hasMetadata("InfinixMobID")) {
            handleTrigger("~onTeleport", (LivingEntity) event.getEntity(), null);
        }
    }
    
    private boolean checkCondition(String condition, LivingEntity caster, Entity target) {
        boolean negate = false;
        if (condition.startsWith("!")) {
            negate = true;
            condition = condition.substring(1);
        }
        Matcher minionMatcher = Pattern.compile("minions_alive\\{type=([a-zA-Z0-9_]+);radius=([0-9]+)\\}").matcher(condition);
        if (minionMatcher.find()) {
            long count = countMinions(caster, minionMatcher.group(1), Double.parseDouble(minionMatcher.group(2)));
            condition = condition.replace(minionMatcher.group(0), String.valueOf(count));
        }
        condition = PlaceholderParser.parse(condition, caster, target);
        try {
            Matcher numericMatcher = NUMERIC_CONDITION_PATTERN.matcher(condition);
            if (numericMatcher.matches()) {
                String variable = numericMatcher.group(1).toLowerCase();
                String operator = numericMatcher.group(2);
                double value = Double.parseDouble(numericMatcher.group(3));
                double actualValue = 0;
                switch (variable) {
                    case "health": actualValue = caster.getHealth(); break;
                    case "distance": if (target == null) return false; actualValue = caster.getLocation().distance(target.getLocation()); break;
                    case "worldtime": actualValue = caster.getWorld().getTime(); break;
                }
                boolean result = false;
                switch (operator) {
                    case ">": result = actualValue > value; break;
                    case "<": result = actualValue < value; break;
                    case "=": result = actualValue == value; break;
                    case ">=": result = actualValue >= value; break;
                    case "<=": result = actualValue <= value; break;
                    case "!=": result = actualValue != value; break;
                }
                return negate ? !result : result;
            }
            Matcher genericMatcher = GENERIC_PATTERN.matcher(condition.toLowerCase());
            if (genericMatcher.matches()) {
                String type = genericMatcher.group(1);
                String params = genericMatcher.group(2);
                boolean result = false;
                switch (type) {
                    case "in_biome": result = caster.getLocation().getBlock().getBiome().name().equalsIgnoreCase(params); break;
                    case "has_potion_effect":
                        PotionEffectType effect = PotionEffectType.getByName(params.toUpperCase());
                        result = effect != null && caster.hasPotionEffect(effect);
                        break;
                    case "has_meta":
                        String[] metaParams = params.split("=");
                        NamespacedKey key = new NamespacedKey(plugin, metaParams[0]);
                        String expectedValue = metaParams.length > 1 ? metaParams[1] : null;
                        if (caster.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                            if (expectedValue != null) {
                                result = caster.getPersistentDataContainer().get(key, PersistentDataType.STRING).equalsIgnoreCase(expectedValue);
                            } else {
                                result = true;
                            }
                        }
                        break;
                    case "target_has_item":
                        if (!(target instanceof Player)) return false;
                        ItemStack item = ((Player) target).getInventory().getItemInMainHand();
                        result = item != null && item.getType().name().equalsIgnoreCase(params);
                        break;
                    case "mob_count":
                        String[] mobCountParams = params.split(";");
                        String mobType = mobCountParams[0].split("=")[1];
                        int radius = Integer.parseInt(mobCountParams[1].split("=")[1]);
                        result = countMobs(caster, mobType, radius) > 0;
                        break;
                }
                return negate ? !result : result;
            }
            boolean result = false;
            switch (condition.toLowerCase()) {
                case "is_underground": result = caster.getLocation().getY() < 50; break;
                case "is_thundering": result = caster.getWorld().isThundering(); break;
                case "target_is_burning": result = target != null && target.getFireTicks() > 0; break;
                case "target_is_player": result = target instanceof Player; break;
                case "target_is_sneaking": result = target instanceof Player && ((Player) target).isSneaking(); break;
                case "is_on_fire": result = caster.getFireTicks() > 0; break;
                case "is_day": result = caster.getWorld().getTime() >= 0 && caster.getWorld().getTime() < 12300; break;
                case "is_night": result = caster.getWorld().getTime() >= 12300; break;
                case "is_raining": result = caster.getWorld().hasStorm(); break;
                case "is_in_water": result = caster.isInWater(); break;
                case "is_on_ground": result = caster.isOnGround(); break;
                case "target_is_looking_at_caster":
                    if (!(target instanceof Player)) return false;
                    Player player = (Player) target;
                    Vector toEntity = caster.getEyeLocation().toVector().subtract(player.getEyeLocation().toVector());
                    double dot = toEntity.normalize().dot(player.getEyeLocation().getDirection());
                    result = dot > 0.99;
                    break;
                default:
                    return true;
            }
             return negate ? !result : result;
        } catch (Exception e) {
            return true;
        }
    }
    
    private long countMinions(LivingEntity owner, String mobType, double radius) {
        String ownerId = owner.getUniqueId().toString();
        return owner.getWorld().getNearbyEntities(owner.getLocation(), radius, radius, radius).stream()
            .filter(entity -> entity.hasMetadata("InfinixMob_Owner"))
            .filter(entity -> entity.getMetadata("InfinixMob_Owner").get(0).asString().equals(ownerId))
            .filter(entity -> entity.hasMetadata("InfinixMobID"))
            .filter(entity -> entity.getMetadata("InfinixMobID").get(0).asString().equalsIgnoreCase(mobType))
            .count();
    }
    
    private long countMobs(LivingEntity caster, String mobType, double radius) {
        return caster.getWorld().getNearbyEntities(caster.getLocation(), radius, radius, radius).stream()
            .filter(entity -> entity.hasMetadata("InfinixMobID"))
            .filter(entity -> entity.getMetadata("InfinixMobID").get(0).asString().equalsIgnoreCase(mobType))
            .count();
    }
}