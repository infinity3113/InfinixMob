package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import com.infinity3113.infinixmob.utils.SkillValueCalculator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Map;

public class DamageMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        if (target instanceof LivingEntity) {
            double amount;
            Object amountObj = params.get("amount");

            if (amountObj instanceof Map) {
                ConfigurationSection amountSection;
                if (amountObj instanceof ConfigurationSection) {
                    amountSection = (ConfigurationSection) amountObj;
                } else {
                    FileConfiguration tempConfig = new YamlConfiguration();
                    amountSection = tempConfig.createSection("temp", (Map<?, ?>) amountObj);
                }
                
                int skillLevel = 1;
                if (caster instanceof Player && playerData != null) {
                    String skillId = (String) params.get("skillId");
                    if (skillId != null) {
                        skillLevel = playerData.getSkillLevel(skillId);
                    }
                }
                amount = SkillValueCalculator.calculate(amountSection, skillLevel);
            } else {
                amount = ((Number) params.getOrDefault("amount", 1.0)).doubleValue();
            }
            ((LivingEntity) target).damage(amount, caster);
        }
    }
}