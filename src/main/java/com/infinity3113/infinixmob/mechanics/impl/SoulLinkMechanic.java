package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SoulLinkMechanic implements Mechanic {

    private final InfinixMob plugin;

    public SoulLinkMechanic(InfinixMob plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
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
