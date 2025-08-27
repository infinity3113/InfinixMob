package com.infinity3113.infinixmob.gui.editors;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemListGUI implements InventoryHolder {

    private final InfinixMob plugin;
    private final Player player;
    private final String itemType;
    private final Inventory inventory;

    public ItemListGUI(InfinixMob plugin, Player player, String itemType) {
        this.plugin = plugin;
        this.player = player;
        this.itemType = itemType;
        this.inventory = Bukkit.createInventory(this, 54, "Ítems - " + itemType);
        initializeItems();
    }

    private void initializeItems() {
        File itemFile = new File(plugin.getDataFolder(), "items/" + itemType + ".yml");
        if (itemFile.exists()) {
            YamlConfiguration itemConfig = YamlConfiguration.loadConfiguration(itemFile);
            int slot = 0;
            for (String itemId : itemConfig.getKeys(false)) {
                if (slot >= 45) break; 
                Optional<CustomItem> customItemOpt = plugin.getItemManager().getItem(itemId);
                if (customItemOpt.isPresent()) {
                    inventory.setItem(slot, customItemOpt.get().buildItemStack());
                    slot++;
                }
            }
        }

        inventory.setItem(45, createButton(Material.LIME_STAINED_GLASS_PANE, "&aCrear Nuevo Ítem", "&7Crea un nuevo ítem de tipo '" + itemType + "'"));
        inventory.setItem(53, createButton(Material.RED_STAINED_GLASS_PANE, "&cVolver al Menú", "&7Regresa a la selección de tipos."));
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        if (slot == 45) { // Crear Nuevo Ítem
            player.closeInventory();
            player.sendMessage(ChatColor.GOLD + "Escribe el ID para tu nuevo ítem en el chat. Escribe 'cancelar' para abortar.");
            plugin.getChatInputManager().requestInput(player, input -> {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (input.equalsIgnoreCase("cancelar")) {
                            player.sendMessage(ChatColor.RED + "Creación cancelada.");
                            new ItemListGUI(plugin, player, itemType).open();
                            return;
                        }
                        ItemBuilder builder = new ItemBuilder(plugin, input, itemType);
                        new ItemEditorGUI(plugin, player, builder).open();
                    }
                }.runTask(plugin);
            });
        } else if (slot == 53) { // Volver
            new EditorMainMenuGUI(plugin, player).open();
        } else if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
            // Editar ítem existente
            String itemId = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(CustomItem.CUSTOM_TAG_KEY, PersistentDataType.STRING);
            if (itemId != null) {
                Optional<CustomItem> customItemOpt = plugin.getItemManager().getItem(itemId);
                customItemOpt.ifPresent(customItem -> {
                    ItemBuilder builder = new ItemBuilder(plugin, customItem);
                    new ItemEditorGUI(plugin, player, builder).open();
                });
            }
        }
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