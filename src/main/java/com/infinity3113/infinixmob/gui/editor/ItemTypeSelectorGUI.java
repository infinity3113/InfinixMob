package com.infinity3113.infinixmob.gui.editor;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public class ItemTypeSelectorGUI extends MenuGUI {

    // Usamos un LinkedHashMap para mantener el orden de los tipos en la GUI
    private final Map<String, Map.Entry<String, Material>> itemTypes = new LinkedHashMap<>();

    public ItemTypeSelectorGUI(InfinixMob plugin, Player player) {
        super(plugin, player);
        initializeItemTypes();
    }
    
    private void initializeItemTypes() {
        itemTypes.put("SWORD", Map.entry("Espadas", Material.DIAMOND_SWORD));
        itemTypes.put("AXE", Map.entry("Hachas", Material.DIAMOND_AXE));
        itemTypes.put("BOW", Map.entry("Arcos", Material.BOW));
        itemTypes.put("ARMOR", Map.entry("Armaduras", Material.DIAMOND_CHESTPLATE));
        itemTypes.put("WAND", Map.entry("Varitas", Material.BLAZE_ROD));
        itemTypes.put("MISC", Map.entry("Misceláneo", Material.EMERALD));
    }

    @Override
    public String getMenuName() {
        return "Editor de Ítems - Tipos";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        // Busca el tipo de ítem correspondiente al ícono clickeado
        for (Map.Entry<String, Map.Entry<String, Material>> entry : itemTypes.entrySet()) {
            if (entry.getValue().getValue() == clickedItem.getType()) {
                String typeId = entry.getKey();
                String typeName = entry.getValue().getKey();
                // Abre la siguiente GUI (el navegador de ítems)
                new ItemBrowserGUI(plugin, player, typeId, typeName, this).open();
                return;
            }
        }
    }

    @Override
    public void setItems() {
        int slot = 0;
        for (Map.Entry<String, Map.Entry<String, Material>> entry : itemTypes.entrySet()) {
            String typeName = entry.getValue().getKey();
            Material icon = entry.getValue().getValue();
            inventory.setItem(slot++, createGuiItem(icon, ChatColor.AQUA + typeName));
        }
        fillEmptySlots();
    }
}
