package com.infinity3113.infinixmob.mechanics.impl;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import java.util.Map;
public class PushMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        Location from = caster.getLocation();
        Location to = target.getLocation();
        double strength = ((Number) params.getOrDefault("strength", 1.0)).doubleValue();
        double vertical = ((Number) params.getOrDefault("vertical", 0.5)).doubleValue();
        Vector vector = to.toVector().subtract(from.toVector()).normalize().multiply(strength);
        vector.setY(vertical);
        target.setVelocity(vector);
    }
}