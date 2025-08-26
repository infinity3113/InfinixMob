package com.infinity3113.infinixmob.core;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mobs.CustomMob;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MobManager {

    private final InfinixMob plugin;
    private final Map<String, CustomMob> customMobs = new HashMap<>();
    private final Map<UUID, List<BukkitTask>> mobTimers = new HashMap<>();
    private final Set<LivingEntity> activeMobs = new HashSet<>();
    private BukkitTask timerSkillTask;

    public MobManager(InfinixMob plugin) {
        this.plugin = plugin;
    }

    public void loadMobs() {
        customMobs.clear();
        File mobsFolder = new File(plugin.getDataFolder(), "Mobs");
        if (!mobsFolder.exists()) mobsFolder.mkdirs();

        File[] files = mobsFolder.listFiles();
        if (files == null) return;

        for (File mobFile : files) {
            if (mobFile.getName().endsWith(".yml")) {
                FileConfiguration mobConfig = new YamlConfiguration();
                try {
                    mobConfig.load(mobFile);
                    for (String key : mobConfig.getKeys(false)) {
                        CustomMob mob = new CustomMob(plugin, key, mobConfig.getConfigurationSection(key));
                        customMobs.put(key.toLowerCase(), mob);
                        plugin.getLogger().info("Loaded Mob: " + key);
                    }
                } catch (IOException | InvalidConfigurationException e) {
                    plugin.getLogger().severe("Could not load mob file: " + mobFile.getName() + ". Check for YAML errors.");
                    e.printStackTrace();
                }
            }
        }
    }
    
    // --- NUEVA LÓGICA DE TIMERS ---
    public void startTimerSkillTicker() {
        if (timerSkillTask != null) {
            timerSkillTask.cancel();
        }

        timerSkillTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Iteramos sobre una copia de los mobs activos para evitar problemas si un mob muere en medio del bucle
                for (LivingEntity mob : new HashSet<>(getActiveMobs())) {
                    if (mob.isValid() && !mob.isDead()) {
                        // Llamamos al manejador de triggers del MobListener
                        plugin.getMobListener().handleTimerTick(mob);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Se ejecuta cada segundo (20 ticks)
    }

    public Optional<CustomMob> getMob(String id) {
        return Optional.ofNullable(customMobs.get(id.toLowerCase()));
    }

    public Optional<CustomMob> getMob(LivingEntity entity) {
        if (entity.hasMetadata("InfinixMobID")) {
            String mobId = entity.getMetadata("InfinixMobID").get(0).asString();
            return getMob(mobId);
        }
        return Optional.empty();
    }

    public Set<String> getLoadedMobIds() {
        Set<String> ids = new HashSet<>();
        customMobs.values().forEach(mob -> ids.add(mob.getInternalName()));
        return ids;
    }

    public LivingEntity spawnMob(String id, Location location) {
        Optional<CustomMob> optionalMob = getMob(id);
        if (optionalMob.isPresent()) {
            CustomMob customMob = optionalMob.get();
            try {
                String entityTypeString = customMob.getConfig().getString("type", "ZOMBIE").trim().toUpperCase();
                EntityType type = EntityType.valueOf(entityTypeString);
                LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, type);
                customMob.applyToEntity(entity);
                activeMobs.add(entity);
                return entity;
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Invalid entity type in mob '" + customMob.getInternalName() + "': " + customMob.getConfig().getString("type"));
                return null;
            }
        }
        return null;
    }

    public void unregisterMob(LivingEntity mob) {
        activeMobs.remove(mob);
        plugin.getThreatManager().clearThreat(mob.getUniqueId());
        plugin.getStatusEffectManager().clearEffects(mob.getUniqueId());
        cancelMobTimers(mob.getUniqueId());
    }

    public void cancelMobTimers(UUID mobUuid) {
        if (mobTimers.containsKey(mobUuid)) {
            mobTimers.get(mobUuid).forEach(BukkitTask::cancel);
            mobTimers.remove(mobUuid);
        }
    }

    public Set<LivingEntity> getActiveMobs() {
        activeMobs.removeIf(e -> !e.isValid() || e.isDead());
        return activeMobs;
    }
}