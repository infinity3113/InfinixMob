package com.infinity3113.infinixmob.gui.editor;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class SkillModifierGUI extends MenuGUI {

    private final CustomItem customItem;
    private final MenuGUI previousMenu;

    public SkillModifierGUI(InfinixMob plugin, Player player, CustomItem customItem, MenuGUI previousMenu) {
        super(plugin, player);
        this.customItem = customItem;
        this.previousMenu = previousMenu;
    }

    @Override
    public String getMenuName() {
        return "Editando Amplificadores de Habilidad";
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
        if (!customItem.getConfig().isConfigurationSection("skill-modifiers")) {
            customItem.getConfig().createSection("skill-modifiers");
        }

        int slot = 0;
        for (String skillId : plugin.getSkillManager().getLoadedSkillNames()) {
            if (slot >= 45) break;

            String fullKey = "skill-modifiers." + skillId;
            String friendlyName = ((ItemEditorGUI) previousMenu).getFriendlyNameForKey(fullKey);
            double value = customItem.getConfig().getDouble(fullKey, 0.0);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Valor: " + ChatColor.YELLOW + value);
            lore.add("");
            lore.add(ChatColor.AQUA + "Click para editar.");
            
            inventory.setItem(slot++, createGuiItemWithKey(Material.ENCHANTED_BOOK, ChatColor.GREEN + friendlyName, fullKey, lore.toArray(new String[0])));
        }

        inventory.setItem(49, createGuiItem(Material.BARRIER, ChatColor.RED + "Volver al Editor"));
        fillEmptySlots();
    }
}