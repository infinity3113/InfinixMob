package com.infinity3113.infinixmob.mechanics.impl;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import java.util.Map;
public class PullMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        Location from = target.getLocation();
        Location to = caster.getLocation();
        double strength = ((Number) params.getOrDefault("strength", 1.0)).doubleValue();
        Vector vector = to.toVector().subtract(from.toVector()).normalize().multiply(strength);
        target.setVelocity(vector);
    }
}