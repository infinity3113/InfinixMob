package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import java.util.Map;

public class LaunchMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        double power = ((Number) params.getOrDefault("power", 1.5)).doubleValue();
        Vector velocity = target.getVelocity();
        velocity.setY(power);
        target.setVelocity(velocity);
    }
}