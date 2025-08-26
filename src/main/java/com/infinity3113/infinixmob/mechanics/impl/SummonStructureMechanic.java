package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.Map;

/**
 * Mecánica que invoca una estructura de bloques.
 */
public class SummonStructureMechanic implements Mechanic {
    
    private final InfinixMob plugin;

    public SummonStructureMechanic(InfinixMob plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target == null) return;

        List<String> structureData = (List<String>) params.get("structure");
        if (structureData == null || structureData.isEmpty()) return;

        int duration = ((Number) params.getOrDefault("duration", -1)).intValue();

        plugin.getBlockManager().buildStructure(target.getLocation(), structureData, duration);
    }
}