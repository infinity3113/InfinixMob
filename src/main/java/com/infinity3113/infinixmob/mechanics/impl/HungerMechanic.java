package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import java.util.Map;

/**
 * Mecánica que reduce el nivel de hambre de un jugador.
 */
public class HungerMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target instanceof Player) {
            Player player = (Player) target;
            int amount = ((Number) params.getOrDefault("amount", 2)).intValue(); // 2 = 1 muslo de hambre
            int newFoodLevel = Math.max(0, player.getFoodLevel() - amount);
            player.setFoodLevel(newFoodLevel);
        }
    }
}