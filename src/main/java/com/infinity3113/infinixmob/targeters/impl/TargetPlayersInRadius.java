package com.infinity3113.infinixmob.targeters.impl;
import com.infinity3113.infinixmob.targeters.Targeter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.stream.Collectors;
public class TargetPlayersInRadius implements Targeter {
    @Override
    public List<Entity> getTargets(LivingEntity caster, Entity initialTarget, String rawParameters) {
        double radius = 10;
        if (rawParameters != null && rawParameters.toUpperCase().startsWith("R=")) {
            radius = Double.parseDouble(rawParameters.substring(2));
        }
        double finalRadius = radius;
        return caster.getNearbyEntities(finalRadius, finalRadius, finalRadius).stream()
                .filter(e -> e instanceof Player)
                .collect(Collectors.toList());
    }
}