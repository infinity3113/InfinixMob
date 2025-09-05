package com.infinity3113.infinixmob.targeters.impl;
import com.infinity3113.infinixmob.targeters.Targeter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import java.util.List;
import java.util.stream.Collectors;

public class TargetEntitiesInRadius implements Targeter {
    @Override
    public List<Entity> getTargets(LivingEntity caster, Entity initialTarget, String rawParameters) {
        double radius = 10;
        if (rawParameters != null && rawParameters.toUpperCase().startsWith("R=")) {
            radius = Double.parseDouble(rawParameters.substring(2));
        }
        
        // --- INICIO DE LA CORRECCIÓN ---
        // El centro de la búsqueda debe ser el objetivo inicial (el punto de impacto).
        // Si no hay un objetivo inicial, se usa la ubicación del lanzador como alternativa.
        Entity center = initialTarget != null ? initialTarget : caster;
        // --- FIN DE LA CORRECCIÓN ---

        return center.getNearbyEntities(radius, radius, radius).stream()
                .filter(e -> e instanceof LivingEntity && !e.equals(caster))
                .collect(Collectors.toList());
    }
}