package com.infinity3113.infinixmob.targeters.impl;
import com.infinity3113.infinixmob.targeters.Targeter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.List;
public class TargetLine implements Targeter {
    @Override
    public List<Entity> getTargets(LivingEntity caster, Entity initialTarget, String rawParameters) {
        double range = 10;
        if (rawParameters != null && rawParameters.toUpperCase().startsWith("RANGE=")) {
            range = Double.parseDouble(rawParameters.substring(6));
        }
        List<Entity> targets = new ArrayList<>();
        Location start = caster.getEyeLocation();
        Vector direction = start.getDirection();
        for (Entity entity : caster.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity && !entity.equals(caster)) {
                Vector toEntity = entity.getLocation().toVector().subtract(start.toVector());
                if (toEntity.normalize().dot(direction) > 0.98) { // Check if entity is in a narrow cone
                    targets.add(entity);
                }
            }
        }
        return targets;
    }
}