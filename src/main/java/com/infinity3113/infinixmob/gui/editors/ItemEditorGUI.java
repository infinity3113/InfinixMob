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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ItemEditorGUI implements InventoryHolder {

    private final InfinixMob plugin;
    private final Player player;
    private final ItemBuilder builder;
    private final String returnItemType;
    private final Inventory inventory;

    public ItemEditorGUI(InfinixMob plugin, Player player, ItemBuilder builder) {
        this.plugin = plugin;
        this.player = player;
        this.builder = builder;
        this.returnItemType = builder.getString("type").toLowerCase();
        this.inventory = Bukkit.createInventory(this, 54, "Editor: " + builder.getId());
        initializeItems();
    }

    private void initializeItems() {
        inventory.clear();
        inventory.setItem(13, builder.buildPreview());

        inventory.setItem(29, createButton(Material.NAME_TAG, "&aPropiedades Generales", "&7Click para editar Nombre, ID, Tipo, etc."));
        inventory.setItem(30, createButton(Material.DIAMOND_SWORD, "&cEstadísticas de Combate", "&7Click para editar Daño, Críticos, etc."));
        inventory.setItem(31, createButton(Material.GOLDEN_APPLE, "&eEstadísticas de Base", "&7Click para editar Vida, Armadura, etc."));
        inventory.setItem(32, createButton(Material.FIRE_CHARGE, "&6Daño Elemental", "&7Click para editar los elementos."));
        inventory.setItem(33, createButton(Material.BOOK, "&bEditor de Lore", "&7Click para editar la descripción."));

        inventory.setItem(49, createButton(Material.LIME_DYE, "&a&lGuardar y Volver", "&7Guarda los cambios y vuelve a la lista."));
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        switch (slot) {
            case 29: new GeneralPropertiesGUI(plugin, player, builder).open(); break;
            case 30: new CombatStatsGUI(plugin, player, builder).open(); break;
            case 31: new BaseStatsGUI(plugin, player, builder).open(); break;
            case 32: new ElementalDamageGUI(plugin, player, builder).open(); break;
            case 33: new LoreEditorGUI(plugin, player, builder).open(); break;
            case 49:
                try {
                    builder.save();
                    plugin.getItemManager().loadItems();
                    player.sendMessage(ChatColor.GREEN + "¡Ítem '" + builder.getId() + "' guardado con éxito!");
                    new ItemListGUI(plugin, player, returnItemType).open();
                } catch (IOException e) {
                    player.sendMessage(ChatColor.RED + "Error: No se pudo guardar el ítem.");
                    e.printStackTrace();
                }
                break;
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