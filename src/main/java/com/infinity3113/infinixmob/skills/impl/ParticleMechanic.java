package com.infinity3113.infinixmob.mechanics.impl;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import java.util.Map;
public class ParticleMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        try {
            Particle particle = Particle.valueOf(((String) params.getOrDefault("particle", "HEART")).toUpperCase());
            int amount = ((Number) params.getOrDefault("amount", 10)).intValue();
            double speed = ((Number) params.getOrDefault("speed", 0.1)).doubleValue();
            target.getWorld().spawnParticle(particle, target.getLocation().add(0, 1, 0), amount, 0.5, 0.5, 0.5, speed);
        } catch (Exception e) {}
    }
}