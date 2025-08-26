package com.infinity3113.infinixmob.listeners;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.gui.SpawnerGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class SpawnerListener implements Listener {

    private final InfinixMob plugin;
    private final NamespacedKey spawnerItemKey;

    public SpawnerListener(InfinixMob plugin) {
        this.plugin = plugin;
        this.spawnerItemKey = new NamespacedKey(plugin, "infinix_spawner_item");
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.getType() == Material.BEACON && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(spawnerItemKey, PersistentDataType.STRING)) {
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
                block.getWorld().dropItemNaturally(block.getLocation(), plugin.getItemManager().getItem("SpawnerCore").get().buildItemStack());
            }
        }
    }
}