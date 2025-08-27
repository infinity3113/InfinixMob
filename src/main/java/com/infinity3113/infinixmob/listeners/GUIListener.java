package com.infinity3113.infinixmob.listeners;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.gui.MobSelectionGUI;
import com.infinity3113.infinixmob.gui.SpawnerGUI;
import com.infinity3113.infinixmob.gui.SpawnerListGui;
import com.infinity3113.infinixmob.gui.editors.*;
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

        if (holder instanceof SpawnerGUI || holder instanceof MobSelectionGUI || holder instanceof SpawnerListGui ||
            holder instanceof ItemEditorGUI || holder instanceof EditorMainMenuGUI || holder instanceof ItemListGUI ||
            holder instanceof GeneralPropertiesGUI || holder instanceof CombatStatsGUI || holder instanceof BaseStatsGUI ||
            holder instanceof ElementalDamageGUI || holder instanceof LoreEditorGUI) {
            
            event.setCancelled(true);

            if (holder instanceof SpawnerGUI) ((SpawnerGUI) holder).handleClick(event);
            else if (holder instanceof MobSelectionGUI) ((MobSelectionGUI) holder).handleClick(event);
            else if (holder instanceof SpawnerListGui) ((SpawnerListGui) holder).handleClick(event);
            else if (holder instanceof ItemEditorGUI) ((ItemEditorGUI) holder).handleClick(event);
            else if (holder instanceof EditorMainMenuGUI) ((EditorMainMenuGUI) holder).handleClick(event);
            else if (holder instanceof ItemListGUI) ((ItemListGUI) holder).handleClick(event);
            else if (holder instanceof GeneralPropertiesGUI) ((GeneralPropertiesGUI) holder).handleClick(event);
            else if (holder instanceof CombatStatsGUI) ((CombatStatsGUI) holder).handleClick(event);
            else if (holder instanceof BaseStatsGUI) ((BaseStatsGUI) holder).handleClick(event);
            else if (holder instanceof ElementalDamageGUI) ((ElementalDamageGUI) holder).handleClick(event);
            else if (holder instanceof LoreEditorGUI) ((LoreEditorGUI) holder).handleClick(event);
        }
    }
}