package com.infinity3113.infinixmob.listeners;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.gui.SpawnerGUI;
import com.infinity3113.infinixmob.items.CustomItem; // Asegúrate de tener esta importación
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer; // Asegúrate de tener esta importación
import org.bukkit.persistence.PersistentDataType; // Asegúrate de tener esta importación

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SpawnerListener implements Listener {

    private final InfinixMob plugin;

    public SpawnerListener(InfinixMob plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        // --- LÓGICA DEFINITIVA A PRUEBA DE CAMBIOS ---
        // 1. Comprobar el material del ítem
        if (item.getType() != Material.DRAGON_EGG || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        // 2. Comprobar si el ítem tiene la etiqueta PDC de CustomItem
        if (container.has(CustomItem.CUSTOM_TAG_KEY, PersistentDataType.STRING)) {
            // 3. Comprobar que el valor de la etiqueta es exactamente "SpawnerCore"
            String itemId = container.get(CustomItem.CUSTOM_TAG_KEY, PersistentDataType.STRING);
            if (Objects.equals(itemId, "SpawnerCore")) {
                Block block = event.getBlockPlaced();
                Map<String, String> defaultData = new HashMap<>();
                defaultData.put("interval", "10");
                defaultData.put("activationRange", "64");
                defaultData.put("maxMobs", "5");
                defaultData.put("radius", "10");
                defaultData.put("mobTypes", "{}");
                defaultData.put("lastSpawn", "0");
                plugin.getSpawnerManager().saveSpawnerData(block, defaultData);
                event.getPlayer().sendMessage(ChatColor.GREEN + "Has colocado un Spawner de InfinixMob. Haz click derecho para configurarlo.");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();
            if (block.getState() instanceof TileState) {
                if (((TileState) block.getState()).getPersistentDataContainer().has(plugin.getSpawnerManager().SPAWNER_KEY, PersistentDataType.STRING)) {
                    event.setCancelled(true);
                    new SpawnerGUI(plugin, event.getPlayer(), block).open();
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getState() instanceof TileState) {
            if (((TileState) block.getState()).getPersistentDataContainer().has(plugin.getSpawnerManager().SPAWNER_KEY, PersistentDataType.STRING)) {
                if(!event.isCancelled()) {
                    event.setDropItems(false); 
                    block.getWorld().dropItemNaturally(block.getLocation(), plugin.getItemManager().getItem("SpawnerCore").get().buildItemStack());
                }
            }
        }
    }
}