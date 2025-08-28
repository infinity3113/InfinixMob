package com.infinity3113.infinixmob.gui.editor;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class ItemBrowserGUI extends MenuGUI {

    private final String itemType;
    private final String itemTypeName;
    private final MenuGUI previousMenu;
    private final int page;
    private static final int ITEMS_PER_PAGE = 45;

    public ItemBrowserGUI(InfinixMob plugin, Player player, String itemType, String itemTypeName, MenuGUI previousMenu) {
        this(plugin, player, itemType, itemTypeName, previousMenu, 0);
    }
    
    public ItemBrowserGUI(InfinixMob plugin, Player player, String itemType, String itemTypeName, MenuGUI previousMenu, int page) {
        super(plugin, player);
        this.itemType = itemType;
        this.itemTypeName = itemTypeName;
        this.previousMenu = previousMenu;
        this.page = page;
    }

    @Override
    public String getMenuName() {
        return "Ítems - " + itemTypeName;
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta() || clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        // Lógica de navegación y selección
        if (event.getSlot() == 48 && page > 0) { // Botón Anterior
            new ItemBrowserGUI(plugin, player, itemType, itemTypeName, previousMenu, page - 1).open();
        } else if (event.getSlot() == 50) { // Botón Siguiente
            new ItemBrowserGUI(plugin, player, itemType, itemTypeName, previousMenu, page + 1).open();
        } else if (event.getSlot() == 49) { // Botón Volver
            previousMenu.open();
        } else if (event.getSlot() < ITEMS_PER_PAGE) { // Clic en un ítem
            plugin.getItemManager().getItem(ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName())).ifPresent(customItem -> {
                // CORRECCIÓN: Se pasa "this" como el cuarto parámetro (previousMenu)
                new ItemEditorGUI(plugin, player, customItem, this).open();
            });
        }
    }

    @Override
    public void setItems() {
        // Filtrar todos los ítems cargados por el tipo seleccionado
        List<CustomItem> itemsToShow = plugin.getItemManager().getLoadedItemIds().stream()
                .map(id -> plugin.getItemManager().getItem(id).orElse(null))
                .filter(item -> item != null && item.getConfig().getString("type", "MISC").equalsIgnoreCase(itemType))
                .collect(Collectors.toList());

        // Paginación
        int startIndex = page * ITEMS_PER_PAGE;
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex < itemsToShow.size()) {
                CustomItem customItem = itemsToShow.get(itemIndex);
                ItemStack displayItem = customItem.buildItemStack();
                ItemMeta meta = displayItem.getItemMeta();
                if(meta != null){
                    meta.setDisplayName(ChatColor.RESET + customItem.getId()); 
                    displayItem.setItemMeta(meta);
                }
                inventory.setItem(i, displayItem);
            } else {
                break;
            }
        }

        // Botones de navegación
        if (page > 0) {
            inventory.setItem(48, createGuiItem(Material.ARROW, ChatColor.GREEN + "Página Anterior"));
        }
        inventory.setItem(49, createGuiItem(Material.BARRIER, ChatColor.RED + "Volver"));
        if ((page + 1) * ITEMS_PER_PAGE < itemsToShow.size()) {
            inventory.setItem(50, createGuiItem(Material.ARROW, ChatColor.GREEN + "Página Siguiente"));
        }
        
        fillEmptySlots();
    }
}