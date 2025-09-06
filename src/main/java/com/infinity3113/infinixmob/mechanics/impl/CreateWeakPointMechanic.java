package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.Map;

public class CreateWeakPointMechanic implements Mechanic {

    private final InfinixMob plugin;

    public CreateWeakPointMechanic(InfinixMob plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target instanceof LivingEntity) {
            double damageMultiplier = ((Number) params.getOrDefault("damage_multiplier", 1.5)).doubleValue();
            String skillOnDamage = (String) params.get("skill_on_damage");
            double offsetX = ((Number) params.getOrDefault("offset_x", 0.0)).doubleValue();
            double offsetY = ((Number) params.getOrDefault("offset_y", 1.0)).doubleValue();
            double offsetZ = ((Number) params.getOrDefault("offset_z", 0.0)).doubleValue();
            Vector offset = new Vector(offsetX, offsetY, offsetZ);

            plugin.getWeakPointManager().createWeakPoint((LivingEntity) target, damageMultiplier, skillOnDamage, offset);
        }
    }
}