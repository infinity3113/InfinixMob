package com.infinity3113.infinixmob.mechanics.impl;
import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
public class SpawnFallingBlockMechanic implements Mechanic {
    private final InfinixMob plugin;
    public SpawnFallingBlockMechanic(InfinixMob plugin) { this.plugin = plugin; }
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target == null) return;
        Material material = Material.matchMaterial(((String) params.getOrDefault("block_type", "STONE")).toUpperCase());
        if (material == null) return;
        int amount = ((Number) params.getOrDefault("amount", 10)).intValue();
        int durationTicks = ((Number) params.getOrDefault("duration", 2)).intValue() * 20;
        double radius = ((Number) params.getOrDefault("radius", 5.0)).doubleValue();
        Location center = target.getLocation();
        long delay = durationTicks / amount;
        if (delay < 1) delay = 1;
        new BukkitRunnable() {
            private int count = 0;
            @Override
            public void run() {
                if (count++ >= amount) {
                    this.cancel();
                    return;
                }
                double x = center.getX() + ThreadLocalRandom.current().nextDouble(-radius, radius);
                double z = center.getZ() + ThreadLocalRandom.current().nextDouble(-radius, radius);
                double y = center.getY() + 15;
                Location spawnLoc = new Location(center.getWorld(), x, y, z);
                center.getWorld().spawnFallingBlock(spawnLoc, material.createBlockData());
            }
        }.runTaskTimer(plugin, 0L, delay);
    }
}