package com.infinity3113.infinixmob.gui.editor;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
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

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        if (event.getSlot() == 48 && page > 0) {
            new ItemBrowserGUI(plugin, player, itemType, itemTypeName, previousMenu, page - 1).open();
        } else if (event.getSlot() == 50) {
            // Se necesita recalcular si la página siguiente existe, ya que la lista puede cambiar.
            List<CustomItem> itemsToShow = getItemsForType();
            if ((page + 1) * ITEMS_PER_PAGE < itemsToShow.size()) {
                new ItemBrowserGUI(plugin, player, itemType, itemTypeName, previousMenu, page + 1).open();
            }
        } else if (event.getSlot() == 49) {
            previousMenu.open();
        } 
        // --- NUEVA LÓGICA PARA EL BOTÓN DE CREAR ---
        else if (event.getSlot() == 53 && displayName.equals("Crear Nuevo Ítem")) {
            handleCreateNewItem();
        } 
        // --- FIN DE LA NUEVA LÓGICA ---
        else if (event.getSlot() < ITEMS_PER_PAGE) {
            plugin.getItemManager().getItem(displayName).ifPresent(customItem -> {
                new ItemEditorGUI(plugin, player, customItem, this).open();
            });
        }
    }

    @Override
    public void setItems() {
        List<CustomItem> itemsToShow = getItemsForType();

        int startIndex = page * ITEMS_PER_PAGE;
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex < itemsToShow.size()) {
                CustomItem customItem = itemsToShow.get(itemIndex);
                ItemStack displayItem = customItem.buildItemStack();
                ItemMeta meta = displayItem.getItemMeta();
                if(meta != null){
                    // Mostramos el ID original para evitar confusiones con nombres duplicados
                    meta.setDisplayName(ChatColor.RESET + customItem.getId()); 
                    displayItem.setItemMeta(meta);
                }
                inventory.setItem(i, displayItem);
            } else {
                break;
            }
        }

        if (page > 0) {
            inventory.setItem(48, createGuiItem(Material.ARROW, ChatColor.GREEN + "Página Anterior"));
        }
        inventory.setItem(49, createGuiItem(Material.BARRIER, ChatColor.RED + "Volver"));
        if ((page + 1) * ITEMS_PER_PAGE < itemsToShow.size()) {
            inventory.setItem(50, createGuiItem(Material.ARROW, ChatColor.GREEN + "Página Siguiente"));
        }
        
        // --- AÑADIMOS EL BOTÓN DE CREAR ---
        inventory.setItem(53, createGuiItem(Material.NETHER_STAR, ChatColor.GREEN + "Crear Nuevo Ítem", ChatColor.GRAY + "Click para crear un nuevo", ChatColor.GRAY + "ítem en esta categoría."));
        
        fillEmptySlots();
    }

    private List<CustomItem> getItemsForType() {
        return plugin.getItemManager().getLoadedItemIds().stream()
                .map(id -> plugin.getItemManager().getItem(id).orElse(null))
                .filter(item -> item != null && item.getConfig().getString("type", "MISC").equalsIgnoreCase(itemType))
                .collect(Collectors.toList());
    }
    
    private void handleCreateNewItem() {
        player.closeInventory();
        player.sendMessage(ChatColor.GOLD + "Escribe en el chat un ID único para el nuevo ítem (sin espacios, ej: EspadaLegendaria). Escribe 'cancelar' para abortar.");

        plugin.getChatInputManager().requestInput(player, inputId -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (inputId.equalsIgnoreCase("cancelar") || inputId.contains(" ") || inputId.isEmpty()) {
                        player.sendMessage(ChatColor.RED + "Operación cancelada o ID inválido (no debe contener espacios).");
                        open(); // Reabrir el navegador
                        return;
                    }

                    if (plugin.getItemManager().getItem(inputId).isPresent()) {
                        player.sendMessage(ChatColor.RED + "Ya existe un ítem con ese ID. Por favor, elige otro.");
                        open();
                        return;
                    }

                    // Crear una configuración por defecto en memoria
                    ConfigurationSection newConfig = new YamlConfiguration().createSection(inputId);
                    newConfig.set("id", "STONE");
                    newConfig.set("display-name", "&f" + inputId);
                    newConfig.set("type", itemType); // Usa el tipo de la categoría actual
                    newConfig.set("rarity", "COMMON");
                    newConfig.set("revision-id", 1);
                    newConfig.createSection("stats");
                    newConfig.set("lore", Collections.singletonList("&7Un nuevo ítem increíble."));

                    CustomItem newItem = new CustomItem(plugin, inputId, newConfig);
                    plugin.getItemManager().saveItem(newItem);

                    player.sendMessage(ChatColor.GREEN + "¡Ítem '" + inputId + "' creado! Ahora puedes editarlo.");
                    
                    // Abrir el editor para el nuevo ítem
                    new ItemEditorGUI(plugin, player, newItem, ItemBrowserGUI.this).open();
                }
            }.runTask(plugin);
        });
    }
}