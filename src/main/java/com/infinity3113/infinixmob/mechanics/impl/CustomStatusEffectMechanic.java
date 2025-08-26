package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import java.util.Map;

public class CustomStatusEffectMechanic implements Mechanic {
    private final InfinixMob plugin;
    public CustomStatusEffectMechanic(InfinixMob plugin) { this.plugin = plugin; }

    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target instanceof LivingEntity) {
            String effect = (String) params.get("effect");
            int duration = ((Number) params.getOrDefault("duration", 10)).intValue();
            if (effect != null) {
                plugin.getStatusEffectManager().applyEffect((LivingEntity) target, effect, duration);
            }
        }
    }
}