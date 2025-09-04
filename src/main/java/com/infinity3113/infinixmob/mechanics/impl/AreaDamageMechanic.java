package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic; // <-- ESTA LÍNEA FALTABA
import com.infinity3113.infinixmob.playerclass.PlayerData;
import com.infinity3113.infinixmob.utils.SkillValueCalculator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Map;

public class AreaDamageMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        double damage;
        Object damageObj = params.get("damage");

        if (damageObj instanceof Map) {
            ConfigurationSection damageSection;
            // Convertir el Map a ConfigurationSection si es necesario
            if (damageObj instanceof ConfigurationSection) {
                damageSection = (ConfigurationSection) damageObj;
            } else {
                FileConfiguration tempConfig = new YamlConfiguration();
                damageSection = tempConfig.createSection("temp", (Map<?, ?>) damageObj);
            }

            int skillLevel = 1;
            if (caster instanceof Player && playerData != null) {
                String skillId = (String) params.get("skillId");
                if (skillId != null) {
                    skillLevel = playerData.getSkillLevel(skillId);
                }
            }
            damage = SkillValueCalculator.calculate(damageSection, skillLevel);
        } else {
            // Si es un número simple, lo obtenemos
            damage = ((Number) params.getOrDefault("damage", 5.0)).doubleValue();
        }

        double radius = ((Number) params.getOrDefault("radius", 5.0)).doubleValue();

        // Se obtiene una lista de entidades y se itera sobre ellas para aplicar el daño real.
        target.getWorld().getNearbyEntities(target.getLocation(), radius, radius, radius).stream()
            .filter(e -> e instanceof LivingEntity && !e.equals(caster))
            .forEach(e -> {
                LivingEntity victim = (LivingEntity) e;
                // Usamos el método damage() para que se apliquen todas las mecánicas de daño de Minecraft.
                victim.damage(damage, caster);
            });
    }
}