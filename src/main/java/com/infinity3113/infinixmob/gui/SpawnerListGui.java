package com.infinity3113.infinixmob.gui;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// CORRECCIÓN: El nombre de la clase ahora es "SpawnerListGui" para coincidir con el nombre del archivo.
public class SpawnerListGui implements InventoryHolder {

    private final InfinixMob plugin;
    private final Player player;
    private final Inventory inventory;

    public SpawnerListGui(InfinixMob plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, "Lista de Spawners Activos");
        initializeItems();
    }

    private void initializeItems() {
        Map<Location, Map<String, String>> spawners = plugin.getSpawnerManager().getAllSpawners();
        int slot = 0;
        for (Map.Entry<Location, Map<String, String>> entry : spawners.entrySet()) {
            if (slot >= 54) break;
            Location loc = entry.getKey();
            String name = ChatColor.translateAlternateColorCodes('&', entry.getValue().getOrDefault("name", "Spawner Sin Nombre"));
            
            ItemStack item = new ItemStack(Material.BEACON);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Mundo: " + loc.getWorld().getName());
            lore.add(ChatColor.GRAY + "Coords: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
            lore.add("");
            lore.add(ChatColor.GREEN + "Click para teletransportarte");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(slot++, item);
        }
    }

    public void open() {
        player.openInventory(inventory);
    }
    
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        if (event.getCurrentItem().getType() == Material.BEACON) {
            List<String> lore = event.getCurrentItem().getItemMeta().getLore();
            if (lore == null || lore.size() < 2) {
                player.sendMessage(ChatColor.RED + "Error: Datos del spawner incompletos.");
                return;
            }

            String worldLine = lore.get(0);
            String coordsLine = lore.get(1);

            String worldName = ChatColor.stripColor(worldLine).replace("Mundo: ", "").trim();
            World targetWorld = Bukkit.getWorld(worldName);

            if (targetWorld == null) {
                player.sendMessage(ChatColor.RED + "Error: El mundo '" + worldName + "' no fue encontrado.");
                return;
            }

            String[] coords = ChatColor.stripColor(coordsLine).replace("Coords: ", "").split(", ");
            if (coords.length != 3) {
                player.sendMessage(ChatColor.RED + "Error: Formato de coordenadas inválido.");
                return;
            }

            try {
                int x = Integer.parseInt(coords[0].trim());
                int y = Integer.parseInt(coords[1].trim());
                int z = Integer.parseInt(coords[2].trim());
                player.teleport(new Location(targetWorld, x + 0.5, y + 1, z + 0.5));
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Teletransportado al spawner.");
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Error: Las coordenadas no son números válidos.");
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}