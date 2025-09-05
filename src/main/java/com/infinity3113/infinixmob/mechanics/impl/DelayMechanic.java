package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DelayMechanic implements Mechanic {

    private final InfinixMob plugin;

    public DelayMechanic(InfinixMob plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        int duration = ((Number) params.getOrDefault("duration", 1)).intValue();
        List<Map<?, ?>> mechanics = (List<Map<?, ?>>) params.get("mechanics");
        String skillId = (String) params.get("skillId");

        if (mechanics == null || mechanics.isEmpty()) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map<?, ?> mechanicData : mechanics) {
                    String targeterString = (String) mechanicData.get("targeter");
                    List<Entity> targets = plugin.getTargeterManager().getTargets(caster, target, targeterString);

                    for (Entity finalTarget : targets) {
                        String mechanicType = (String) mechanicData.get("type");
                        Map<String, Object> parametersMap = (Map<String, Object>) mechanicData.get("parameters");
                        if (parametersMap == null) {
                            parametersMap = new HashMap<>();
                        }
                        parametersMap.put("skillId", skillId);
                        plugin.getMechanicManager().executeMechanic(mechanicType, caster, finalTarget, parametersMap, playerData);
                    }
                }
            }
        }.runTaskLater(plugin, duration * 20L);
    }
}