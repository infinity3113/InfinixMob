package com.infinity3113.infinixmob.listeners;

import com.google.gson.reflect.TypeToken;
import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable; // AÑADIR IMPORTACIÓN

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class ItemListener implements Listener {

    private final InfinixMob plugin;
    private final String ARROW_ITEM_ID_KEY = "infinix_arrow_item_id";

    public ItemListener(InfinixMob plugin) {
        this.plugin = plugin;
        startTwoHandedWeaponTask(); // Iniciar la nueva tarea
    }

    // --- NUEVA TAREA PARA ARMAS A DOS MANOS ---
    private void startTwoHandedWeaponTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    if (isCustomItem(mainHand)) {
                        plugin.getItemManager().getItem(mainHand.getItemMeta().getPersistentDataContainer().get(CustomItem.CUSTOM_TAG_KEY, PersistentDataType.STRING))
                            .ifPresent(customItem -> {
                                String handStyle = customItem.getConfig().getString("hand-style", "ONE_HANDED");
                                if (handStyle.equalsIgnoreCase("TWO_HANDED")) {
                                    ItemStack offHand = player.getInventory().getItemInOffHand();
                                    if (offHand != null && offHand.getType() != Material.AIR) {
                                        player.getInventory().setItemInOffHand(null);
                                        player.getInventory().addItem(offHand).forEach((index, item) -> {
                                            player.getWorld().dropItemNaturally(player.getLocation(), item);
                                        });
                                    }
                                }
                            });
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L); // Revisa cada 5 ticks
    }
    // --- FIN DE LA NUEVA TAREA ---

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;

        if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
            Player player = (Player) event.getDamager();
            ItemStack weapon = player.getInventory().getItemInMainHand();

            if (isCustomItem(weapon)) {
                if (weapon.getType() == Material.BOW || weapon.getType() == Material.CROSSBOW) {
                    event.setDamage(0.1);
                } else {
                    event.setDamage(calculateDamage(weapon.getItemMeta().getPersistentDataContainer(), (LivingEntity) event.getEntity()));
                }
            }
        }

        if (event.getDamager() instanceof Arrow && event.getEntity() instanceof LivingEntity) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.hasMetadata(ARROW_ITEM_ID_KEY)) {
                String itemId = arrow.getMetadata(ARROW_ITEM_ID_KEY).get(0).asString();
                Optional<CustomItem> customItemOpt = plugin.getItemManager().getItem(itemId);

                if (customItemOpt.isPresent()) {
                    ItemStack bow = customItemOpt.get().buildItemStack();
                    event.setDamage(calculateDamage(bow.getItemMeta().getPersistentDataContainer(), (LivingEntity) event.getEntity()));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        ItemStack bow = event.getBow();
        if (isCustomItem(bow)) {
            String itemId = bow.getItemMeta().getPersistentDataContainer().get(CustomItem.CUSTOM_TAG_KEY, PersistentDataType.STRING);
            event.getProjectile().setMetadata(ARROW_ITEM_ID_KEY, new FixedMetadataValue(plugin, itemId));
        }
    }

    private boolean isCustomItem(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(CustomItem.CUSTOM_TAG_KEY, PersistentDataType.STRING);
    }
    
    private double calculateDamage(PersistentDataContainer container, LivingEntity victim) {
        String statsJson = container.get(CustomItem.STATS_KEY, PersistentDataType.STRING);
        if (statsJson == null) return 1.0;

        Type type = new TypeToken<Map<String, Double>>(){}.getType();
        Map<String, Double> stats = plugin.getGson().fromJson(statsJson, type);

        double baseDamage = stats.getOrDefault("damage", 1.0);
        double critChance = stats.getOrDefault("crit-chance", 0.0) / 100.0;
        double critDamageMultiplier = 1.0 + (stats.getOrDefault("crit-damage", 0.0) / 100.0);

        double finalDamage = baseDamage;
        if (ThreadLocalRandom.current().nextDouble() < critChance) {
            finalDamage *= critDamageMultiplier;
        }

        if (container.has(CustomItem.ELEMENTAL_DAMAGE_KEY, PersistentDataType.STRING)) {
            String elementalJson = container.get(CustomItem.ELEMENTAL_DAMAGE_KEY, PersistentDataType.STRING);
            Map<String, Double> elementalDamages = plugin.getGson().fromJson(elementalJson, type);

            PersistentDataContainer victimContainer = victim.getPersistentDataContainer();
            Map<String, Double> weaknesses = new HashMap<>();
            if (victimContainer.has(CustomItem.WEAKNESSES_KEY, PersistentDataType.STRING)) {
                String weaknessesJson = victimContainer.get(CustomItem.WEAKNESSES_KEY, PersistentDataType.STRING);
                weaknesses = plugin.getGson().fromJson(weaknessesJson, type);
            }

            for (Map.Entry<String, Double> entry : elementalDamages.entrySet()) {
                String element = entry.getKey().toLowerCase();
                double damage = entry.getValue();
                double multiplier = weaknesses.getOrDefault(element, 1.0);
                finalDamage += (damage * multiplier);
            }
        }
        
        return finalDamage;
    }
}