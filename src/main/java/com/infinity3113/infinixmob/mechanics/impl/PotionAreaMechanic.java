package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import com.infinity3113.infinixmob.utils.SkillValueCalculator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Map;

public class PotionAreaMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        double radius = ((Number) params.getOrDefault("radius", 5.0)).doubleValue();
        PotionEffectType effect = PotionEffectType.getByName(((String) params.getOrDefault("effect", "SLOW")).toUpperCase());
        if (effect == null) return;
        
        int skillLevel = 1;
        if(caster instanceof Player && playerData != null) {
            String skillId = (String) params.get("skillId");
            skillLevel = playerData.getSkillLevel(skillId);
        }

        double durationSeconds;
        if (params.get("duration") instanceof ConfigurationSection) {
            durationSeconds = SkillValueCalculator.calculate((ConfigurationSection) params.get("duration"), skillLevel);
        } else {
            durationSeconds = ((Number) params.getOrDefault("duration", 5.0)).doubleValue();
        }

        double amplifierDouble;
        if (params.get("amplifier") instanceof ConfigurationSection) {
            amplifierDouble = SkillValueCalculator.calculate((ConfigurationSection) params.get("amplifier"), skillLevel);
        } else {
            amplifierDouble = ((Number) params.getOrDefault("amplifier", 1.0)).doubleValue();
        }
        
        int durationTicks = (int) (durationSeconds * 20);
        int amplifier = (int) Math.floor(amplifierDouble) - 1;

        target.getWorld().getNearbyEntities(target.getLocation(), radius, radius, radius).stream()
            .filter(e -> e instanceof LivingEntity)
            .forEach(e -> ((LivingEntity) e).addPotionEffect(new PotionEffect(effect, durationTicks, amplifier)));
    }
}