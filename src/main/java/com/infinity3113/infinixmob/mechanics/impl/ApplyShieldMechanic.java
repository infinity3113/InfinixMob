package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

/**
 * Mecánica que aplica un escudo de absorción (corazones amarillos) a la entidad.
 */
public class ApplyShieldMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target instanceof LivingEntity) {
            double amount = ((Number) params.getOrDefault("amount", 10.0)).doubleValue();
            // El efecto de absorción se maneja con un PotionEffect.
            // El amplificador determina la cantidad de "escudo". Nivel 0 = 4 de vida (2 corazones).
            // Por lo tanto, necesitamos calcular el amplificador correcto.
            int amplifier = (int) Math.ceil(amount / 4.0) - 1;
            if (amplifier < 0) amplifier = 0;

            ((LivingEntity) target).addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 300, amplifier, true, true)); // 5 minutos de duración o hasta que se rompa
        }
    }
}