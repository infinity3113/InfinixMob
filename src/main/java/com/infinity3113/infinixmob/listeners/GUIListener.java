package com.infinity3113.infinixmob.listeners;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.gui.MobSelectionGUI;
import com.infinity3113.infinixmob.gui.SpawnerGUI;
import com.infinity3113.infinixmob.gui.SpawnerListGui;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class GUIListener implements Listener {

    private final InfinixMob plugin;

    public GUIListener(InfinixMob plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof SpawnerGUI) {
            event.setCancelled(true);
            ((SpawnerGUI) holder).handleClick(event);
        } else if (holder instanceof MobSelectionGUI) {
            event.setCancelled(true);
            ((MobSelectionGUI) holder).handleClick(event);
        } else if (holder instanceof SpawnerListGui) {
            event.setCancelled(true);
            ((SpawnerListGui) holder).handleClick(event);
        }
    }
}