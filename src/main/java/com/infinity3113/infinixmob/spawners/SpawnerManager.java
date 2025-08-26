package com.infinity3113.infinixmob.core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SpawnerManager {

    private final InfinixMob plugin;
    public final NamespacedKey SPAWNER_KEY;
    private final Gson gson = new Gson();

    public SpawnerManager(InfinixMob plugin) {
        this.plugin = plugin;
        this.SPAWNER_KEY = new NamespacedKey(plugin, "infinix_spawner_data");
    }

    public void startSpawnerTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                tickSpawners();
            }
        }.runTaskTimer(plugin, 200L, 40L); // 10s delay, 2s interval
    }

    private void tickSpawners() {
        plugin.getServer().getWorlds().forEach(world -> {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (BlockState blockState : chunk.getTileEntities()) {
                    if (blockState instanceof TileState) {
                        TileState tileState = (TileState) blockState;
                        if (tileState.getPersistentDataContainer().has(SPAWNER_KEY, PersistentDataType.STRING)) {
                            tickSpawnerBlock(tileState.getBlock());
                        }
                    }
                }
            }
        });
    }

    private void tickSpawnerBlock(Block block) {
        if (!(block.getState() instanceof TileState)) return;
        PersistentDataContainer container = ((TileState) block.getState()).getPersistentDataContainer();
        String json = container.get(SPAWNER_KEY, PersistentDataType.STRING);
        Map<String, String> data = deserializeData(json);

        int activationRange = Integer.parseInt(data.getOrDefault("activationRange", "64"));
        
        boolean playerNearby = block.getWorld().getPlayers().stream()
            .anyMatch(p -> p.getLocation().distanceSquared(block.getLocation()) <= (long) activationRange * activationRange);

        if (!playerNearby) return;

        long lastSpawn = Long.parseLong(data.getOrDefault("lastSpawn", "0"));
        int interval = Integer.parseInt(data.getOrDefault("interval", "10")) * 1000;
        if (System.currentTimeMillis() - lastSpawn < interval) {
            return;
        }

        long currentMobs = countMobsForSpawner(block);
        int maxMobs = Integer.parseInt(data.getOrDefault("maxMobs", "5"));
        if (currentMobs >= maxMobs) {
            return;
        }

        spawnMobFor(block);
        data.put("lastSpawn", String.valueOf(System.currentTimeMillis()));
        saveSpawnerData(block, data);
    }
    
    private void spawnMobFor(Block block) {
        if (!(block.getState() instanceof TileState)) return;
        PersistentDataContainer container = ((TileState) block.getState()).getPersistentDataContainer();
        String json = container.get(SPAWNER_KEY, PersistentDataType.STRING);
        Map<String, String> data = deserializeData(json);
        
        Map<String, Integer> mobTypes = deserializeMobTypes(data.getOrDefault("mobTypes", "{}"));
        String mobToSpawn = chooseRandomMob(mobTypes);
        if (mobToSpawn == null) return;

        int radius = Integer.parseInt(data.getOrDefault("radius", "10"));
        Location spawnLocation = findSafeSpawnLocation(block.getLocation(), radius);
        if (spawnLocation == null) return;

        LivingEntity spawnedMob = plugin.getMobManager().spawnMob(mobToSpawn, spawnLocation);
        if (spawnedMob != null) {
            spawnedMob.setMetadata("InfinixMob_SpawnerLocation", new FixedMetadataValue(plugin, block.getLocation().toString()));
        }
    }

    private long countMobsForSpawner(Block block) {
        String spawnerLocation = block.getLocation().toString();
        return block.getWorld().getEntitiesByClass(LivingEntity.class).stream()
            .filter(e -> e.hasMetadata("InfinixMob_SpawnerLocation"))
            .filter(e -> e.getMetadata("InfinixMob_SpawnerLocation").get(0).asString().equals(spawnerLocation))
            .count();
    }

    public void saveSpawnerData(Block block, Map<String, String> data) {
        if (block.getState() instanceof TileState) {
            TileState state = (TileState) block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            String json = serializeData(data);
            container.set(SPAWNER_KEY, PersistentDataType.STRING, json);
            state.update();
        }
    }

    public Map<String, String> getSpawnerData(Block block) {
        if (block.getState() instanceof TileState) {
            TileState state = (TileState) block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            if (container.has(SPAWNER_KEY, PersistentDataType.STRING)) {
                return deserializeData(container.get(SPAWNER_KEY, PersistentDataType.STRING));
            }
        }
        return new HashMap<>();
    }
    
    public Map<Location, Map<String, String>> getAllSpawners() {
        Map<Location, Map<String, String>> spawners = new HashMap<>();
        plugin.getServer().getWorlds().forEach(world -> {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (BlockState blockState : chunk.getTileEntities()) {
                    if (blockState instanceof TileState) {
                        TileState tileState = (TileState) blockState;
                        if (tileState.getPersistentDataContainer().has(SPAWNER_KEY, PersistentDataType.STRING)) {
                            spawners.put(tileState.getLocation(), getSpawnerData(tileState.getBlock()));
                        }
                    }
                }
            }
        });
        return spawners;
    }

    public Map<String, Integer> deserializeMobTypes(String json) {
        Type type = new TypeToken<Map<String, Integer>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public String serializeMobTypes(Map<String, Integer> map) {
        return gson.toJson(map);
    }

    private String serializeData(Map<String, String> data) {
        return gson.toJson(data);
    }

    private Map<String, String> deserializeData(String json) {
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        return gson.fromJson(json, type);
    }

    private String chooseRandomMob(Map<String, Integer> mobTypes) {
        int totalWeight = mobTypes.values().stream().mapToInt(Integer::intValue).sum();
        if (totalWeight <= 0) return null;
        int random = ThreadLocalRandom.current().nextInt(totalWeight);
        for (Map.Entry<String, Integer> entry : mobTypes.entrySet()) {
            random -= entry.getValue();
            if (random < 0) return entry.getKey();
        }
        return null;
    }

    private Location findSafeSpawnLocation(Location center, int radius) {
        for (int i = 0; i < 10; i++) {
            int x = center.getBlockX() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            int z = center.getBlockZ() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            Block highestBlock = center.getWorld().getHighestBlockAt(x, z);
            if (highestBlock.getType() != Material.LAVA && highestBlock.getType() != Material.WATER) {
                return highestBlock.getLocation().add(0.5, 1, 0.5);
            }
        }
        return null;
    }
}