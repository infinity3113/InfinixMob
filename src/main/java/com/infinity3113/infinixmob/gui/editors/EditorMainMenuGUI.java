package com.infinity3113.infinixmob.gui.editors;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditorMainMenuGUI implements InventoryHolder {

    private final InfinixMob plugin;
    private final Player player;
    private final Inventory inventory;

    public EditorMainMenuGUI(InfinixMob plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, "Editor de Ítems - Tipos");
        initializeItems();
    }

    private void initializeItems() {
        File itemsFolder = new File(plugin.getDataFolder(), "items");
        File[] itemFiles = itemsFolder.listFiles((dir, name) ->
                name.endsWith(".yml") &&
                !name.equalsIgnoreCase("elements.yml") &&
                !name.equalsIgnoreCase("rarities.yml") &&
                !name.equalsIgnoreCase("lore-formats.yml") &&
                !name.equalsIgnoreCase("stats.yml")
        );

        if (itemFiles == null) return;

        int slot = 0;
        for (File itemFile : itemFiles) {
            if (slot >= 54) break;
            String typeKey = itemFile.getName().replace(".yml", "").toUpperCase();
            FileConfiguration statsConfig = plugin.getItemManager().getStatsConfig();
            String displayName = statsConfig.getString("item-types." + typeKey, typeKey);
            
            ItemStack typeItem = new ItemStack(getMaterialForItemType(typeKey));
            ItemMeta meta = typeItem.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + displayName);
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click para ver/crear ítems de este tipo.");
            lore.add(ChatColor.DARK_GRAY + "ID Interno: " + typeKey);
            meta.setLore(lore);

            typeItem.setItemMeta(meta);
            inventory.setItem(slot++, typeItem);
        }
    }
    
    private Material getMaterialForItemType(String type) {
        switch(type) {
            case "SWORD": return Material.DIAMOND_SWORD;
            case "AXE": return Material.DIAMOND_AXE;
            case "BOW": return Material.BOW;
            case "ARMOR": return Material.DIAMOND_CHESTPLATE;
            case "MISC": return Material.EMERALD;
            default: return Material.CHEST;
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        
        ItemMeta meta = event.getCurrentItem().getItemMeta();
        if (meta == null || !meta.hasLore()) return;

        List<String> lore = meta.getLore();
        if (lore.isEmpty()) return;

        String idLine = lore.get(lore.size() - 1);
        String typeKey = ChatColor.stripColor(idLine).replace("ID Interno: ", "");

        if (!typeKey.isEmpty()) {
            new ItemListGUI(plugin, player, typeKey.toLowerCase()).open();
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}