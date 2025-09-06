package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Map;

public class FreezeMechanic implements Mechanic {

    private final InfinixMob plugin;

    public FreezeMechanic(InfinixMob plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target instanceof LivingEntity) {
            int duration = ((Number) params.getOrDefault("duration", 5)).intValue() * 20;
            int amplifier = ((Number) params.getOrDefault("amplifier", 7)).intValue();
            
            ((LivingEntity) target).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, amplifier, true, false));
            target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0);
        }
    }
}