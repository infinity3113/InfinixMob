package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.utils.SkillValueCalculator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class PotionMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target instanceof LivingEntity) {
            PotionEffectType effect = PotionEffectType.getByName(((String) params.getOrDefault("effect", "SLOW")).toUpperCase());
            if (effect == null) return;

            int skillLevel = 1;
            
            double durationSeconds;
            Object durationObj = params.get("duration");
            if (durationObj instanceof Map) {
                ConfigurationSection durationSection;
                if(durationObj instanceof ConfigurationSection){
                    durationSection = (ConfigurationSection) durationObj;
                } else {
                    FileConfiguration tempConfig = new YamlConfiguration();
                    durationSection = tempConfig.createSection("temp", (Map<?,?>) durationObj);
                }
                durationSeconds = SkillValueCalculator.calculate(durationSection, skillLevel);
            } else {
                durationSeconds = ((Number) params.getOrDefault("duration", 3.0)).doubleValue();
            }

            double amplifierDouble;
            Object amplifierObj = params.get("amplifier");
            if (amplifierObj instanceof Map) {
                ConfigurationSection amplifierSection;
                if(amplifierObj instanceof ConfigurationSection){
                    amplifierSection = (ConfigurationSection) amplifierObj;
                } else {
                    FileConfiguration tempConfig = new YamlConfiguration();
                    amplifierSection = tempConfig.createSection("temp", (Map<?,?>) amplifierObj);
                }
                amplifierDouble = SkillValueCalculator.calculate(amplifierSection, skillLevel);
            } else {
                amplifierDouble = ((Number) params.getOrDefault("amplifier", 1.0)).doubleValue();
            }
            
            int durationTicks = (int) (durationSeconds * 20);
            int amplifier = (int) Math.floor(amplifierDouble) - 1;

            ((LivingEntity) target).addPotionEffect(new PotionEffect(effect, durationTicks, amplifier));
        }
    }
}