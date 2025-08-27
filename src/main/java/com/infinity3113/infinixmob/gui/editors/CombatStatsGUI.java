package com.infinity3113.infinixmob.gui.editors;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CombatStatsGUI implements InventoryHolder {

    private final InfinixMob plugin;
    private final Player player;
    private final ItemBuilder builder;
    private final Inventory inventory;

    public CombatStatsGUI(InfinixMob plugin, Player player, ItemBuilder builder) {
        this.plugin = plugin;
        this.player = player;
        this.builder = builder;
        this.inventory = Bukkit.createInventory(this, 27, "Estadísticas de Combate");
        initializeItems();
    }

    private void initializeItems() {
        inventory.setItem(10, createButton(Material.IRON_SWORD, "&cDaño", String.valueOf(builder.getDouble("damage"))));
        inventory.setItem(11, createButton(Material.FEATHER, "&fVelocidad de Ataque", String.valueOf(builder.getDouble("attack-speed"))));
        inventory.setItem(12, createButton(Material.BOW, "&aProb. de Crítico", String.valueOf(builder.getDouble("crit-chance")) + "%"));
        inventory.setItem(13, createButton(Material.TNT, "&4Daño Crítico", "+" + String.valueOf(builder.getDouble("crit-damage")) + "%"));
        inventory.setItem(26, createButton(Material.RED_STAINED_GLASS_PANE, "&cVolver", "&7Regresa al editor principal."));
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        String propertyKey = "";

        switch (slot) {
            case 10: propertyKey = "damage"; break;
            case 11: propertyKey = "attack-speed"; break;
            case 12: propertyKey = "crit-chance"; break;
            case 13: propertyKey = "crit-damage"; break;
            case 26: new ItemEditorGUI(plugin, player, builder).open(); return;
            default: return;
        }

        final String finalPropertyKey = propertyKey;
        player.closeInventory();
        player.sendMessage(ChatColor.GOLD + "Escribe el nuevo valor numérico para '" + finalPropertyKey + "' en el chat.");
        plugin.getChatInputManager().requestInput(player, input -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        double value = Double.parseDouble(input);
                        builder.set(finalPropertyKey, value);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Valor inválido. Debes introducir un número.");
                    }
                    new CombatStatsGUI(plugin, player, builder).open();
                }
            }.runTask(plugin);
        });
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