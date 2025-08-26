package com.infinity3113.infinixmob.spawners;

import org.bukkit.Location;
import java.util.Map;

public class Spawner {
    private final String id;
    private final Location center;
    private final int radius;
    private final int activationRange;
    private final int maxMobs;
    private final int spawnInterval;
    private final Map<String, Integer> mobTypes;
    private long lastSpawnAttempt;

    public Spawner(String id, Location center, int radius, int activationRange, int maxMobs, int spawnInterval, Map<String, Integer> mobTypes) {
        this.id = id;
        this.center = center;
        this.radius = radius;
        this.activationRange = activationRange;
        this.maxMobs = maxMobs;
        this.spawnInterval = spawnInterval;
        this.mobTypes = mobTypes;
        this.lastSpawnAttempt = 0;
    }

    public String getId() { return id; }
    public Location getCenter() { return center; }
    public int getRadius() { return radius; }
    public int getActivationRange() { return activationRange; }
    public int getMaxMobs() { return maxMobs; }
    public int getSpawnInterval() { return spawnInterval; }
    public Map<String, Integer> getMobTypes() { return mobTypes; }
    public long getLastSpawnAttempt() { return lastSpawnAttempt; }
    public void setLastSpawnAttempt(long lastSpawnAttempt) { this.lastSpawnAttempt = lastSpawnAttempt; }
}