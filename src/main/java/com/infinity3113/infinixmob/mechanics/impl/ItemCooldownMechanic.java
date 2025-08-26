package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Map;

/**
 * Mecánica que aplica un cooldown al item que un jugador tiene en la mano.
 */
public class ItemCooldownMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target instanceof Player) {
            Player player = (Player) target;
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand != null && itemInHand.getType() != Material.AIR) {
                int duration = ((Number) params.getOrDefault("duration", 100)).intValue(); // Duración en ticks
                player.setCooldown(itemInHand.getType(), duration);
            }
        }
    }
}