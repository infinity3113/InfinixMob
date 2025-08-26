package com.infinity3113.infinixmob.mechanics.impl;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Map;
public class PotionMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target instanceof LivingEntity) {
            PotionEffectType effect = PotionEffectType.getByName(((String) params.getOrDefault("effect", "SLOW")).toUpperCase());
            if (effect == null) return;
            int duration = ((Number) params.getOrDefault("duration", 3)).intValue() * 20;
            int amplifier = ((Number) params.getOrDefault("amplifier", 1)).intValue() - 1;
            ((LivingEntity) target).addPotionEffect(new PotionEffect(effect, duration, amplifier));
        }
    }
}