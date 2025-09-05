package com.infinity3113.infinixmob.listeners;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.gui.editor.MenuGUI; // NUEVA IMPORTACIÓN
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
        if (holder == null) return;

        // REFACTOR: Ahora detecta cualquier GUI que extienda nuestra clase base MenuGUI.
        // Esto hace que el listener sea universal para todas las GUIs del editor.
        if (holder instanceof MenuGUI) {
            ((MenuGUI) holder).handleClick(event);
        }
    }
}