package com.infinity3113.infinixmob.gui.editors;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BaseStatsGUI implements InventoryHolder {

    private final InfinixMob plugin;
    private final Player player;
    private final ItemBuilder builder;
    private final Inventory inventory;

    public BaseStatsGUI(InfinixMob plugin, Player player, ItemBuilder builder) {
        this.plugin = plugin;
        this.player = player;
        this.builder = builder;
        this.inventory = Bukkit.createInventory(this, 54, "Editor de Estadísticas Base");
        initializeItems();
    }

    private void initializeItems() {
        Map<String, Double> baseStats = builder.getBaseStats();
        FileConfiguration statsConfig = plugin.getItemManager().getStatsConfig();
        int slot = 0;
        for (Map.Entry<String, Double> entry : baseStats.entrySet()) {
            if (slot >= 45) break;
            String statKey = entry.getKey();
            String statName = statsConfig.getString("display-names." + statKey.toLowerCase(), statKey);
            ItemStack statItem = createButton(Material.GOLD_INGOT, "&e" + statName, "&fValor actual: &a" + entry.getValue(), "", "&aClick Izquierdo para editar", "&cClick Derecho para eliminar");
            inventory.setItem(slot++, statItem);
        }

        inventory.setItem(45, createButton(Material.LIME_STAINED_GLASS_PANE, "&aAñadir Estadística", "&7Agrega un nuevo atributo base."));
        inventory.setItem(53, createButton(Material.RED_STAINED_GLASS_PANE, "&cVolver", "&7Regresa al editor principal."));
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        if (slot == 53) {
            new ItemEditorGUI(plugin, player, builder).open();
            return;
        }
        
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "La edición detallada de stats base aún no está implementada.");
        new BukkitRunnable() {
            @Override
            public void run() {
                new ItemEditorGUI(plugin, player, builder).open();
            }
        }.runTask(plugin);
    }

    private ItemStack createButton(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        List<String> coloredLore = Arrays.stream(lore)
                                         .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                                         .collect(Collectors.toList());
        meta.setLore(coloredLore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}