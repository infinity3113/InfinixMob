package com.infinity3113.infinixmob.targeters.impl;
import com.infinity3113.infinixmob.targeters.Targeter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import java.util.List;
import java.util.stream.Collectors;
public class TargetMobsInRadius implements Targeter {
    @Override
    public List<Entity> getTargets(LivingEntity caster, Entity initialTarget, String rawParameters) {
        double radius = 10;
        String type = null;
        if (rawParameters != null) {
            String[] params = rawParameters.split(";");
            for (String param : params) {
                String[] parts = param.split("=");
                if (parts[0].equalsIgnoreCase("R")) radius = Double.parseDouble(parts[1]);
                if (parts[0].equalsIgnoreCase("TYPE")) type = parts[1];
            }
        }
        String finalType = type;
        return caster.getNearbyEntities(radius, radius, radius).stream()
                .filter(e -> e instanceof LivingEntity && !e.equals(caster))
                .filter(e -> finalType == null || (e.hasMetadata("InfinixMobID") && e.getMetadata("InfinixMobID").get(0).asString().equalsIgnoreCase(finalType)))
                .collect(Collectors.toList());
    }
}