package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import java.util.Map;

public class IgniteMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        int duration = ((Number) params.getOrDefault("duration", 5)).intValue() * 20;
        target.setFireTicks(duration);
    }
}