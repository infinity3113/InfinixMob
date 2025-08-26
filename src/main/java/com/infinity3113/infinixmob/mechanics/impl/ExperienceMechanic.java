package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import java.util.Map;

/**
 * Mecánica que da o quita experiencia a un jugador.
 */
public class ExperienceMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target instanceof Player) {
            Player player = (Player) target;
            int amount = ((Number) params.getOrDefault("amount", 10)).intValue();
            player.giveExp(amount); // Puede ser negativo para quitar experiencia
        }
    }
}