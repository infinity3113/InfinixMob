package com.infinity3113.infinixmob.gui.editor;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public abstract class MenuGUI implements InventoryHolder {

    protected final InfinixMob plugin;
    protected final Player player;
    protected Inventory inventory;

    public static final NamespacedKey GUI_ITEM_KEY = new NamespacedKey(InfinixMob.getPlugin(), "gui_item_key");

    public MenuGUI(InfinixMob plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public abstract String getMenuName();
    public abstract int getSlots();
    public abstract void handleClick(InventoryClickEvent event);
    public abstract void setItems();

    public void open() {
        inventory = Bukkit.createInventory(this, getSlots(), getMenuName());
        this.setItems();
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    protected ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    // --- CORRECCIÓN: MÉTODO RENOMBRADO PARA EVITAR AMBIGÜEDAD ---
    protected ItemStack createGuiItemWithKey(final Material material, final String name, final String key, final String... lore) {
        final ItemStack item = createGuiItem(material, name, lore);
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(GUI_ITEM_KEY, PersistentDataType.STRING, key);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    protected void fillEmptySlots() {
        ItemStack filler = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }
}