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

/**
 * Mecánica que desordena el inventario de un jugador.
 */
public class ScrambleInventoryMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target instanceof Player) {
            Player player = (Player) target;
            PlayerInventory inv = player.getInventory();
            List<ItemStack> items = new ArrayList<>();

            // Recolecta solo los items del inventario principal (excluyendo armadura y hotbar)
            for (int i = 9; i <= 35; i++) {
                if (inv.getItem(i) != null) {
                    items.add(inv.getItem(i));
                    inv.setItem(i, null);
                }
            }

            // Baraja la lista de items
            Collections.shuffle(items);

            // Vuelve a colocar los items en el inventario
            for (int i = 0; i < items.size(); i++) {
                inv.setItem(9 + i, items.get(i));
            }
            player.updateInventory();
        }
    }
}