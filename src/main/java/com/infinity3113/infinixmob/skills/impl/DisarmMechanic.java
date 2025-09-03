package com.infinity3113.infinixmob.mechanics.impl;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Map;
public class DisarmMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        if (target instanceof Player) {
            Player player = (Player) target;
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (mainHand != null && mainHand.getType() != org.bukkit.Material.AIR) {
                player.getInventory().setItemInMainHand(null);
                player.getWorld().dropItemNaturally(player.getLocation(), mainHand);
            }
        }
    }
}