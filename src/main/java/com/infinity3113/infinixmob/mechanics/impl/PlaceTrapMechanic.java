package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Map;

/**
 * Mecánica que coloca una trampa invisible en una ubicación.
 */
public class PlaceTrapMechanic implements Mechanic {

    private final InfinixMob plugin;

    public PlaceTrapMechanic(InfinixMob plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target == null) return;

        String skillOnTrigger = (String) params.get("skill_on_trigger");
        if (skillOnTrigger == null) return;

        int duration = ((Number) params.getOrDefault("duration", 10)).intValue();

        plugin.getBlockManager().placeTrap(target.getLocation().getBlock().getLocation(), skillOnTrigger, duration);
    }
}