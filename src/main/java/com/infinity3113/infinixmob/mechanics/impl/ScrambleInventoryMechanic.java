package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
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
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target instanceof Player) {
            Player player = (Player) target;
            PlayerInventory inv = player.getInventory();
            List<ItemStack> items = new ArrayList<>();

            // El bucle ahora va de 0 a 35, incluyendo la hotbar y el inventario principal.
            for (int i = 0; i <= 35; i++) {
                if (inv.getItem(i) != null) {
                    items.add(inv.getItem(i));
                }
            }
            
            // Limpia todos los slots antes de volver a colocar los items.
            for (int i = 0; i <= 35; i++) {
                 inv.setItem(i, null);
            }

            Collections.shuffle(items);

            for (int i = 0; i < items.size(); i++) {
                inv.setItem(i, items.get(i)); // Vuelve a llenar desde el slot 0.
            }
            player.updateInventory();
        }
    }
}