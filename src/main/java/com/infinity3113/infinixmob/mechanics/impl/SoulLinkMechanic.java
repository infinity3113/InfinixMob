package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mecánica que vincula las almas de varios objetivos.
 */
public class SoulLinkMechanic implements Mechanic {

    private final InfinixMob plugin;

    public SoulLinkMechanic(InfinixMob plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        // Esta mecánica necesita un selector que devuelva múltiples objetivos.
        // El targeter se define en el YML, aquí solo recibimos el resultado.
        String targeterStr = (String) params.get("targeter");
        if (targeterStr == null) return;

        List<Entity> targets = plugin.getTargeterManager().getTargets(caster, target, targeterStr);
        int duration = ((Number) params.getOrDefault("duration", 10)).intValue();

        List<LivingEntity> livingTargets = targets.stream()
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .collect(Collectors.toList());

        if (livingTargets.size() > 1) {
            plugin.getSoulLinkManager().linkPlayers(livingTargets, duration);
        }
    }
}
