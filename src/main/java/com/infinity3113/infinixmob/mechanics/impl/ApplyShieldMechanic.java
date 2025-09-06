package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class ApplyShieldMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target instanceof LivingEntity) {
            double amount = ((Number) params.getOrDefault("amount", 10.0)).doubleValue();
            int amplifier = (int) Math.ceil(amount / 4.0) - 1;
            if (amplifier < 0) amplifier = 0;

            ((LivingEntity) target).addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 300, amplifier, true, true));
        }
    }
}