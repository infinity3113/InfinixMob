package com.infinity3113.infinixmob.mechanics.impl;
import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
public class MeteorShowerMechanic implements Mechanic {
    private final InfinixMob plugin;
    public MeteorShowerMechanic(InfinixMob plugin) { this.plugin = plugin; }
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target == null) return;
        int amount = ((Number) params.getOrDefault("amount", 10)).intValue();
        int durationTicks = ((Number) params.getOrDefault("duration", 3)).intValue() * 20;
        double radius = ((Number) params.getOrDefault("radius", 8.0)).doubleValue();
        boolean setFire = (boolean) params.getOrDefault("fire", true);
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
                double y = center.getY() + 20;
                Location spawnLoc = new Location(center.getWorld(), x, y, z);
                Fireball meteor = (Fireball) center.getWorld().spawnEntity(spawnLoc, EntityType.FIREBALL);
                meteor.setDirection(new Vector(0, -1, 0));
                meteor.setIsIncendiary(setFire);
                meteor.setYield(2.0F);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (meteor.isDead() || !meteor.isValid()) {
                            this.cancel();
                            return;
                        }
                        meteor.getWorld().spawnParticle(Particle.FLAME, meteor.getLocation(), 5, 0.1, 0.1, 0.1, 0);
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }
        }.runTaskTimer(plugin, 0L, delay);
    }
}