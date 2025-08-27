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

public class GeneralPropertiesGUI implements InventoryHolder {

    private final InfinixMob plugin;
    private final Player player;
    private final ItemBuilder builder;
    private final Inventory inventory;

    public GeneralPropertiesGUI(InfinixMob plugin, Player player, ItemBuilder builder) {
        this.plugin = plugin;
        this.player = player;
        this.builder = builder;
        this.inventory = Bukkit.createInventory(this, 27, "Propiedades Generales");
        initializeItems();
    }

    private void initializeItems() {
        inventory.setItem(10, createButton(Material.NAME_TAG, "&aID del Ítem", builder.getId(), "&7(Nombre interno, sin espacios)"));
        inventory.setItem(11, createButton(Material.PAPER, "&aNombre a Mostrar", builder.getString("display-name"), "&7(El nombre que ven los jugadores)"));
        inventory.setItem(12, createButton(Material.DIAMOND, "&aMaterial (ID)", builder.getString("id"), "&7(El ítem de Minecraft base)"));
        inventory.setItem(13, createButton(Material.CHEST, "&aTipo de Ítem", builder.getString("type"), "&7(SWORD, AXE, ARMOR, etc.)"));
        inventory.setItem(14, createButton(Material.NETHER_STAR, "&aRareza", builder.getString("rarity"), "&7(COMMON, EPIC, etc.)"));
        inventory.setItem(15, createButton(Material.EXPERIENCE_BOTTLE, "&aID de Revisión", String.valueOf(builder.getInt("revision-id")), "&7(Incrementa para forzar actualización)"));
        inventory.setItem(26, createButton(Material.RED_STAINED_GLASS_PANE, "&cVolver", "&7Regresa al editor principal."));
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        String propertyKey = "";
        String currentValue = "";

        switch (slot) {
            case 10: propertyKey = "id"; currentValue = builder.getId(); break;
            case 11: propertyKey = "display-name"; currentValue = builder.getString("display-name"); break;
            case 12: propertyKey = "id"; currentValue = builder.getString("id"); break;
            case 13: propertyKey = "type"; currentValue = builder.getString("type"); break;
            case 14: propertyKey = "rarity"; currentValue = builder.getString("rarity"); break;
            case 15: propertyKey = "revision-id"; currentValue = String.valueOf(builder.getInt("revision-id")); break;
            case 26: new ItemEditorGUI(plugin, player, builder).open(); return;
            default: return;
        }

        final String finalPropertyKey = propertyKey;
        player.closeInventory();
        player.sendMessage(ChatColor.GOLD + "Escribe el nuevo valor para '" + finalPropertyKey + "' en el chat. (Actual: " + currentValue + ")");
        plugin.getChatInputManager().requestInput(player, input -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (finalPropertyKey.equals("id")) {
                        builder.setId(input);
                    } else {
                        builder.set(finalPropertyKey, input);
                    }
                    new GeneralPropertiesGUI(plugin, player, builder).open();
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