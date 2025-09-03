package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ScrambleInventoryMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        if (target instanceof Player) {
            Player player = (Player) target;
            PlayerInventory inv = player.getInventory();
            List<ItemStack> items = new ArrayList<>();

            for (int i = 0; i <= 35; i++) {
                if (inv.getItem(i) != null) {
                    items.add(inv.getItem(i));
                }
            }
            
            for (int i = 0; i <= 35; i++) {
                 inv.setItem(i, null);
            }

            Collections.shuffle(items);

            for (int i = 0; i < items.size(); i++) {
                inv.setItem(i, items.get(i));
            }
            player.updateInventory();
        }
    }
}