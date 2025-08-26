package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import java.util.Map;

public class AreaDamageMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        double damage = ((Number) params.getOrDefault("damage", 5.0)).doubleValue();
        double radius = ((Number) params.getOrDefault("radius", 5.0)).doubleValue();
        
        target.getWorld().getNearbyEntities(target.getLocation(), radius, radius, radius).stream()
            .filter(e -> e instanceof LivingEntity && !e.equals(caster))
            .forEach(e -> ((LivingEntity) e).damage(damage, caster));
    }
}