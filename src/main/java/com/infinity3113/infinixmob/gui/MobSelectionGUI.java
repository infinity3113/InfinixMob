package com.infinity3113.infinixmob.gui;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class MobSelectionGUI implements InventoryHolder {

    private final InfinixMob plugin;
    private final Player player;
    private final Block spawnerBlock;
    private final Inventory inventory;

    public MobSelectionGUI(InfinixMob plugin, Player player, Block spawnerBlock) {
        this.plugin = plugin;
        this.player = player;
        this.spawnerBlock = spawnerBlock;
        this.inventory = Bukkit.createInventory(this, 54, "Seleccionar Mob para Añadir");
        initializeItems();
    }

    private void initializeItems() {
        int slot = 0;
        for (String mobId : plugin.getMobManager().getLoadedMobIds()) {
            if(slot >= 54) break;
            ItemStack mobItem = new ItemStack(Material.SPAWNER);
            ItemMeta meta = mobItem.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + mobId);
            mobItem.setItemMeta(meta);
            inventory.setItem(slot++, mobItem);
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
            String mobId = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
            
            Map<String, String> data = plugin.getSpawnerManager().getSpawnerData(spawnerBlock);
            Map<String, Integer> mobTypes = plugin.getSpawnerManager().deserializeMobTypes(data.get("mobTypes"));
            
            mobTypes.putIfAbsent(mobId, 1);
            
            data.put("mobTypes", plugin.getSpawnerManager().serializeMobTypes(mobTypes));
            plugin.getSpawnerManager().saveSpawnerData(spawnerBlock, data);
            
            new SpawnerGUI(plugin, player, spawnerBlock).open();
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}