package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Map;

public class ItemCooldownMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        if (target instanceof Player) {
            Player player = (Player) target;
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand != null && itemInHand.getType() != Material.AIR) {
                int duration = ((Number) params.getOrDefault("duration", 100)).intValue();
                player.setCooldown(itemInHand.getType(), duration);
            }
        }
    }
}