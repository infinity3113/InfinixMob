package com.infinity3113.infinixmob.gui.editor;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        List<String> lore = clickedItem.getItemMeta().getLore();

        // Manejar botones de acción que no tienen la clave en el lore
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
        
        if (lore == null || lore.isEmpty()) return;
        String key = lore.get(0); // Usamos la primera línea del lore para guardar la clave YML

        // Lógica de edición dinámica
        Object currentValue = customItem.getConfig().get(key);
        if (currentValue instanceof Number) {
            editDoubleValue(key, displayName);
        } else {
            editStringValue(key, displayName);
        }
    }

    @Override
    public void setItems() {
        inventory.setItem(4, customItem.buildItemStack()); // Previsualización

        int slot = 9;
        List<String> keysToShow = new ArrayList<>();
        
        // Añadir claves del nivel principal
        for (String key : customItem.getConfig().getKeys(false)) {
            if (!customItem.getConfig().isConfigurationSection(key) && !key.equalsIgnoreCase("lore")) {
                keysToShow.add(key);
            }
        }
        // Añadir claves de la sección 'stats'
        if (customItem.getConfig().isConfigurationSection("stats")) {
            for (String key : customItem.getConfig().getConfigurationSection("stats").getKeys(false)) {
                keysToShow.add("stats." + key);
            }
        }
        // Añadir claves de la sección 'elemental-damage'
        if (customItem.getConfig().isConfigurationSection("elemental-damage")) {
            for (String key : customItem.getConfig().getConfigurationSection("elemental-damage").getKeys(false)) {
                keysToShow.add("elemental-damage." + key);
            }
        }

        for (String key : keysToShow) {
            if (slot >= 45) break;

            Material icon = getIconForKey(key);
            String friendlyName = getFriendlyNameForKey(key);
            Object value = customItem.getConfig().get(key, "N/A");

            List<String> lore = new ArrayList<>();
            lore.add(key); // Clave oculta para la lógica
            lore.add(ChatColor.GRAY + "Valor: " + ChatColor.YELLOW + value.toString());
            lore.add("");
            lore.add(ChatColor.AQUA + "Click para editar.");
            
            inventory.setItem(slot++, createGuiItem(icon, ChatColor.GREEN + friendlyName, lore.toArray(new String[0])));
        }
        
        inventory.setItem(22, createGuiItem(Material.BOOK, ChatColor.GREEN + "Editar Lore", ChatColor.GRAY + "Click para añadir/quitar lore."));

        // Botones de acción
        inventory.setItem(45, createGuiItem(Material.BARRIER, ChatColor.RED + "Volver a la Lista"));
        inventory.setItem(53, createGuiItem(Material.LIME_DYE, ChatColor.GREEN + "Guardar Cambios"));

        fillEmptySlots();
    }
    
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
                        customItem.getConfig().set(key, newValue);
                        player.sendMessage(ChatColor.GREEN + "¡" + friendlyName + " actualizado! No olvides guardar.");
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
        if (key.contains("display-name")) return Material.NAME_TAG;
        if (key.contains("type")) return Material.CRAFTING_TABLE;
        if (key.contains("lore")) return Material.BOOK;
        if (key.contains("rarity")) return Material.EMERALD;
        if (key.contains("revision-id")) return Material.COMPASS;
        if (key.contains("damage")) return Material.DIAMOND_SWORD;
        if (key.contains("crit-chance")) return Material.SPYGLASS;
        if (key.contains("crit-damage")) return Material.ANVIL;
        if (key.contains("max-health")) return Material.GOLDEN_APPLE;
        if (key.contains("armor")) return Material.DIAMOND_CHESTPLATE;
        if (key.contains("movement-speed")) return Material.SUGAR;
        if (key.contains("fire")) return Material.BLAZE_POWDER;
        if (key.contains("water")) return Material.WATER_BUCKET;
        if (key.contains("earth")) return Material.DIRT;
        if (key.contains("wind")) return Material.FEATHER;
        if (key.contains("light") || key.contains("holy")) return Material.GLOWSTONE_DUST;
        if (key.contains("dark") || key.contains("shadow")) return Material.COAL;
        return Material.PAPER; // Icono por defecto
    }
}