package com.infinity3113.infinixmob.gui.editor;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ItemEditorGUI extends MenuGUI {

    private final CustomItem customItem;
    private final MenuGUI previousMenu;

    public ItemEditorGUI(InfinixMob plugin, Player player, CustomItem customItem, MenuGUI previousMenu) {
        super(plugin, player);
        this.customItem = customItem;
        this.previousMenu = previousMenu;
    }

    @Override
    public String getMenuName() {
        return "Editando: " + customItem.getId();
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta() || clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        if (displayName.equals("¡Obtén el ítem!")) {
            player.getInventory().addItem(customItem.buildItemStack());
            player.sendMessage(ChatColor.GREEN + "Has recibido una copia de " + customItem.getId() + ".");
            return;
        }
        
        if (displayName.equals("Guardar Cambios")) {
            plugin.getItemManager().saveItem(customItem);
            player.sendMessage(ChatColor.GREEN + "¡Ítem guardado exitosamente!");
            new ItemEditorGUI(plugin, player, customItem, previousMenu).open();
            return;
        }

        if (displayName.equals("Volver a la Lista")) {
            previousMenu.open();
            return;
        }
        
        if (displayName.equals("Editar Lore")) {
            new ItemLoreEditorGUI(plugin, player, customItem, this).open();
            return;
        }

        // --- NUEVA LÓGICA PARA EL ESTILO DE MANO ---
        if (displayName.equals("Estilo de Mano")) {
            String currentStyle = customItem.getConfig().getString("hand-style", "ONE_HANDED");
            String newStyle = currentStyle.equalsIgnoreCase("ONE_HANDED") ? "TWO_HANDED" : "ONE_HANDED";
            customItem.getConfig().set("hand-style", newStyle);
            player.sendMessage(ChatColor.YELLOW + "Estilo de mano cambiado a: " + newStyle);
            open(); // Refrescar la GUI
            return;
        }

        String key = clickedItem.getItemMeta().getPersistentDataContainer().get(GUI_ITEM_KEY, PersistentDataType.STRING);
        if (key == null) return;

        Object currentValue = customItem.getConfig().get(key);

        if (currentValue instanceof Number || (currentValue == null && (key.startsWith("stats.") || key.startsWith("elemental-damage.")))) {
             editDoubleValue(key, displayName);
        } else {
            editStringValue(key, displayName);
        }
    }

    @Override
    public void setItems() {
        ItemStack previewItem = customItem.buildItemStack();
        ItemMeta previewMeta = previewItem.getItemMeta();
        if (previewMeta != null) {
            previewMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            previewItem.setItemMeta(previewMeta);
        }
        inventory.setItem(6, previewItem);
        inventory.setItem(2, createGuiItem(Material.CHEST, ChatColor.GREEN + "¡Obtén el ítem!", ChatColor.GRAY + "Click para recibir una copia"));

        int slot = 9;
        List<String> keysToShow = new ArrayList<>();
        
        for (String key : customItem.getConfig().getKeys(false)) {
            if (!customItem.getConfig().isConfigurationSection(key) && !key.equalsIgnoreCase("lore")) {
                keysToShow.add(key);
            }
        }
        
        if (customItem.getConfig().isConfigurationSection("stats")) {
            for (String key : customItem.getConfig().getConfigurationSection("stats").getKeys(false)) {
                keysToShow.add("stats." + key);
            }
        }
        
        if (!customItem.getConfig().isConfigurationSection("elemental-damage")) {
            customItem.getConfig().createSection("elemental-damage");
        }
        ConfigurationSection elementsConfig = plugin.getItemManager().getElementsConfig();
        if (elementsConfig != null) {
            for (String key : elementsConfig.getKeys(false)) {
                keysToShow.add("elemental-damage." + key);
            }
        }

        for (String key : keysToShow) {
            if (slot >= 45) break;

            Material icon = getIconForKey(key);
            String friendlyName = getFriendlyNameForKey(key);
            Object value = customItem.getConfig().get(key, 0.0);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Valor: " + ChatColor.YELLOW + value.toString());
            lore.add("");
            lore.add(ChatColor.AQUA + "Click para editar.");
            
            inventory.setItem(slot++, createGuiItemWithKey(icon, ChatColor.GREEN + friendlyName, key, lore.toArray(new String[0])));
        }
        
        // --- AÑADIR NUEVO BOTÓN PARA ESTILO DE MANO ---
        String handStyle = customItem.getConfig().getString("hand-style", "ONE_HANDED");
        String handStyleName = handStyle.equalsIgnoreCase("ONE_HANDED") ? "Una Mano" : "Dos Manos";
        inventory.setItem(23, createGuiItem(Material.LEATHER_HORSE_ARMOR, ChatColor.AQUA + "Estilo de Mano", ChatColor.GRAY + "Valor: " + ChatColor.YELLOW + handStyleName, "", ChatColor.AQUA + "Click para cambiar"));

        inventory.setItem(22, createGuiItem(Material.BOOK, ChatColor.AQUA + "Editar Lore", ChatColor.GRAY + "Click para añadir/quitar lore."));
        inventory.setItem(45, createGuiItem(Material.BARRIER, ChatColor.RED + "Volver a la Lista"));
        inventory.setItem(53, createGuiItem(Material.LIME_DYE, ChatColor.GREEN + "Guardar Cambios"));

        fillEmptySlots();
    }
    
    // ... (El resto de los métodos de la clase permanecen igual) ...
    private void editStringValue(String key, String friendlyName) {
        player.closeInventory();
        player.sendMessage(ChatColor.GOLD + "Escribe el nuevo valor para '" + friendlyName + "'. Escribe 'cancelar' para abortar.");
        plugin.getChatInputManager().requestInput(player, input -> handleInput(key, friendlyName, input, false));
    }

    private void editDoubleValue(String key, String friendlyName) {
        player.closeInventory();
        player.sendMessage(ChatColor.GOLD + "Escribe el nuevo valor numérico para '" + friendlyName + "'. Escribe 'cancelar' para abortar.");
        plugin.getChatInputManager().requestInput(player, input -> handleInput(key, friendlyName, input, true));
    }

    private void handleInput(String key, String friendlyName, String input, boolean isNumeric) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (input.equalsIgnoreCase("cancelar")) {
                    player.sendMessage(ChatColor.RED + "Operación cancelada.");
                    open();
                    return;
                }

                if (isNumeric) {
                    try {
                        double newValue = Double.parseDouble(input);
                        if (newValue == 0.0) {
                            customItem.getConfig().set(key, null);
                            player.sendMessage(ChatColor.YELLOW + "¡" + friendlyName + " eliminado! No olvides guardar.");
                        } else {
                            customItem.getConfig().set(key, newValue);
                            player.sendMessage(ChatColor.GREEN + "¡" + friendlyName + " actualizado! No olvides guardar.");
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Entrada inválida. Por favor, introduce un número.");
                    }
                } else {
                    customItem.getConfig().set(key, input);
                    player.sendMessage(ChatColor.GREEN + "¡" + friendlyName + " actualizado! No olvides guardar.");
                }
                open();
            }
        }.runTask(plugin);
    }

    private String getFriendlyNameForKey(String key) {
        String[] parts = key.split("\\.");
        String lastPart = parts[parts.length - 1].replace("-", " ");
        return Character.toUpperCase(lastPart.charAt(0)) + lastPart.substring(1);
    }

    private Material getIconForKey(String key) {
        String simpleKey = key.contains(".") ? key.substring(key.lastIndexOf('.') + 1) : key;
        switch (simpleKey.toLowerCase()) {
            case "display-name": return Material.NAME_TAG;
            case "type": return Material.CRAFTING_TABLE;
            case "lore": return Material.BOOK;
            case "rarity": return Material.EMERALD;
            case "revision-id": return Material.COMPASS;
            case "damage": return Material.DIAMOND_SWORD;
            case "crit-chance": return Material.SPYGLASS;
            case "crit-damage": return Material.ANVIL;
            case "max-health": return Material.GOLDEN_APPLE;
            case "armor": return Material.DIAMOND_CHESTPLATE;
            case "movement-speed": return Material.SUGAR;
            case "fire": return Material.BLAZE_POWDER;
            case "water": return Material.WATER_BUCKET;
            case "earth": return Material.DIRT;
            case "wind": return Material.FEATHER;
            case "light": case "holy": return Material.GLOWSTONE_DUST;
            case "dark": case "shadow": return Material.COAL;
            case "poison": return Material.SPIDER_EYE;
            case "lightning": return Material.LIGHTNING_ROD;
            case "ice": return Material.ICE;
            case "nature": return Material.OAK_SAPLING;
            case "arcane": return Material.ENCHANTING_TABLE;
            default: return Material.PAPER;
        }
    }
}