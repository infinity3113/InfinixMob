package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import com.infinity3113.infinixmob.utils.SkillValueCalculator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Map;

public class DamageMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        if (target instanceof LivingEntity) {
            double amount;
            // Comprueba si 'amount' es una sección de configuración para el escalado
            if (params.get("amount") instanceof ConfigurationSection) {
                int skillLevel = 1; // Nivel por defecto si no hay datos del jugador
                if (caster instanceof Player && playerData != null) {
                    String skillId = (String) params.get("skillId");
                    skillLevel = playerData.getSkillLevel(skillId);
                }
                amount = SkillValueCalculator.calculate((ConfigurationSection) params.get("amount"), skillLevel);
            } else {
                // Mantiene la funcionalidad antigua si no es una sección
                amount = ((Number) params.getOrDefault("amount", 1.0)).doubleValue();
            }
            ((LivingEntity) target).damage(amount, caster);
        }
    }
}