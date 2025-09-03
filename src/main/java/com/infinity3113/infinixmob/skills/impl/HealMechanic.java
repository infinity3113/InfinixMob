package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import com.infinity3113.infinixmob.utils.SkillValueCalculator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Map;

public class HealMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        if (target instanceof LivingEntity) {
            LivingEntity healTarget = (LivingEntity) target;
            double amount;
            if (params.get("amount") instanceof ConfigurationSection) {
                int skillLevel = 1;
                if (caster instanceof Player && playerData != null) {
                    String skillId = (String) params.get("skillId");
                    skillLevel = playerData.getSkillLevel(skillId);
                }
                amount = SkillValueCalculator.calculate((ConfigurationSection) params.get("amount"), skillLevel);
            } else {
                amount = ((Number) params.getOrDefault("amount", 1.0)).doubleValue();
            }
            double maxHealth = healTarget.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            healTarget.setHealth(Math.min(maxHealth, healTarget.getHealth() + amount));
        }
    }
}