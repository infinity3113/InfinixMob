package com.infinity3113.infinixmob.gui.editors;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
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

public class ElementalDamageGUI implements InventoryHolder {

    private final InfinixMob plugin;
    private final Player player;
    private final ItemBuilder builder;
    private final Inventory inventory;

    public ElementalDamageGUI(InfinixMob plugin, Player player, ItemBuilder builder) {
        this.plugin = plugin;
        this.player = player;
        this.builder = builder;
        this.inventory = Bukkit.createInventory(this, 54, "Editor de Daño Elemental");
        initializeItems();
    }

    private void initializeItems() {
        inventory.clear();
        Map<String, Double> elementalDamage = builder.getElementalDamage();
        FileConfiguration elementsConfig = plugin.getItemManager().getElementsConfig();
        int slot = 0;
        for (Map.Entry<String, Double> entry : elementalDamage.entrySet()) {
            if (slot >= 45) break;
            String elementKey = entry.getKey();
            String elementName = elementsConfig.getString(elementKey.toLowerCase(), elementKey);
            ItemStack elementItem = createButton(Material.BLAZE_POWDER, elementName, "&fValor actual: &c" + entry.getValue(), "", "&aClick Izquierdo para editar", "&cClick Derecho para eliminar");
            inventory.setItem(slot++, elementItem);
        }

        inventory.setItem(45, createButton(Material.LIME_STAINED_GLASS_PANE, "&aAñadir Elemento", "&7Agrega un nuevo tipo de daño elemental."));
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

        if (slot == 45) { // Añadir
            player.closeInventory();
            player.sendMessage(ChatColor.GOLD + "Escribe el ID del elemento a añadir (ej: fire):");
            plugin.getChatInputManager().requestInput(player, elementKey -> {
                player.sendMessage(ChatColor.GOLD + "Ahora escribe el valor numérico del daño:");
                plugin.getChatInputManager().requestInput(player, elementValue -> {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                double value = Double.parseDouble(elementValue);
                                builder.set("elemental-damage." + elementKey, value);
                            } catch (NumberFormatException e) {
                                player.sendMessage(ChatColor.RED + "Valor inválido.");
                            }
                            new ElementalDamageGUI(plugin, player, builder).open();
                        }
                    }.runTask(plugin);
                });
            });
        } else if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BLAZE_POWDER) {
            String elementName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
            String elementKey = getKeyFromName(elementName);

            if (event.getClick() == ClickType.RIGHT) { // Eliminar
                builder.set("elemental-damage." + elementKey, null);
                new ElementalDamageGUI(plugin, player, builder).open();
            } else { // Editar
                player.closeInventory();
                player.sendMessage(ChatColor.GOLD + "Escribe el nuevo valor para '" + elementName + "':");
                plugin.getChatInputManager().requestInput(player, input -> {
                     new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                double value = Double.parseDouble(input);
                                builder.set("elemental-damage." + elementKey, value);
                            } catch (NumberFormatException e) {
                                player.sendMessage(ChatColor.RED + "Valor inválido.");
                            }
                            new ElementalDamageGUI(plugin, player, builder).open();
                        }
                    }.runTask(plugin);
                });
            }
        }
    }

    private String getKeyFromName(String name) {
        FileConfiguration elementsConfig = plugin.getItemManager().getElementsConfig();
        for (String key : elementsConfig.getKeys(false)) {
            if (ChatColor.stripColor(elementsConfig.getString(key)).equalsIgnoreCase(name)) {
                return key;
            }
        }
        return name.toLowerCase().replace(" ", "-");
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