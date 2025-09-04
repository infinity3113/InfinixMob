package com.infinity3113.infinixmob.gui.editor;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ElementalDamageGUI extends MenuGUI {

    private final CustomItem customItem;
    private final MenuGUI previousMenu;

    public ElementalDamageGUI(InfinixMob plugin, Player player, CustomItem customItem, MenuGUI previousMenu) {
        super(plugin, player);
        this.customItem = customItem;
        this.previousMenu = previousMenu;
    }

    @Override
    public String getMenuName() {
        return "Editando Daño Elemental";
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

        if (displayName.equals("Volver al Editor")) {
            previousMenu.open();
            return;
        }
        
        String key = clickedItem.getItemMeta().getPersistentDataContainer().get(GUI_ITEM_KEY, PersistentDataType.STRING);
        if (key != null) {
            ((ItemEditorGUI) previousMenu).editDoubleValue(key, displayName);
        }
    }

    @Override
    public void setItems() {
        if (!customItem.getConfig().isConfigurationSection("elemental-damage")) {
            customItem.getConfig().createSection("elemental-damage");
        }
        
        ConfigurationSection elementsConfig = plugin.getItemManager().getElementsConfig();
        if (elementsConfig != null) {
            int slot = 0;
            for (String key : elementsConfig.getKeys(false)) {
                if (slot >= 45) break;
                
                String fullKey = "elemental-damage." + key;
                Material icon = ((ItemEditorGUI) previousMenu).getIconForKey(key);
                String friendlyName = elementsConfig.getString(key, key);
                double value = customItem.getConfig().getDouble(fullKey, 0.0);

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Valor: " + ChatColor.YELLOW + value);
                lore.add("");
                lore.add(ChatColor.AQUA + "Click para editar.");
                
                inventory.setItem(slot++, createGuiItemWithKey(icon, ChatColor.GREEN + ChatColor.stripColor(friendlyName), fullKey, lore.toArray(new String[0])));
            }
        }

        inventory.setItem(49, createGuiItem(Material.BARRIER, ChatColor.RED + "Volver al Editor"));
        fillEmptySlots();
    }
}