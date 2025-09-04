package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import com.infinity3113.infinixmob.utils.SkillValueCalculator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Map;

public class AreaDamageMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        double damage;
        if (params.get("damage") instanceof ConfigurationSection) {
            int skillLevel = 1;
            if (caster instanceof Player && playerData != null) {
                String skillId = (String) params.get("skillId");
                skillLevel = playerData.getSkillLevel(skillId);
            }
            damage = SkillValueCalculator.calculate((ConfigurationSection) params.get("damage"), skillLevel);
        } else {
            damage = ((Number) params.getOrDefault("damage", 5.0)).doubleValue();
        }

        double radius = ((Number) params.getOrDefault("radius", 5.0)).doubleValue();
        
        // --- INICIO DE LA MODIFICACIÓN ---
        // Se obtiene una lista de entidades y se itera sobre ellas para aplicar el daño manualmente.
        target.getWorld().getNearbyEntities(target.getLocation(), radius, radius, radius).stream()
            .filter(e -> e instanceof LivingEntity && !e.equals(caster))
            .forEach(e -> {
                LivingEntity victim = (LivingEntity) e;
                
                // Se calcula la nueva vida restando el daño plano
                double newHealth = victim.getHealth() - damage;
                
                // Se asegura que la vida no sea negativa y se aplica.
                // Esto simula un daño que ignora armadura y protecciones.
                victim.setHealth(Math.max(0, newHealth));
            });
        // --- FIN DE LA MODIFICACIÓN ---
    }
}