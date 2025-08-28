package com.infinity3113.infinixmob.gui.editor;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ItemLoreEditorGUI extends MenuGUI {

    private final CustomItem customItem;
    private final MenuGUI previousMenu;

    public ItemLoreEditorGUI(InfinixMob plugin, Player player, CustomItem customItem, MenuGUI previousMenu) {
        super(plugin, player);
        this.customItem = customItem;
        this.previousMenu = previousMenu;
    }

    @Override
    public String getMenuName() {
        return "Editando Lore: " + customItem.getId();
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

        if (displayName.equals("Añadir Línea")) {
            player.closeInventory();
            player.sendMessage(ChatColor.GOLD + "Escribe en el chat la nueva línea de lore (usa '&' para colores). Escribe 'cancelar' para abortar.");
            plugin.getChatInputManager().requestInput(player, input -> {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (input.equalsIgnoreCase("cancelar")) {
                            player.sendMessage(ChatColor.RED + "Operación cancelada.");
                        } else {
                            List<String> lore = customItem.getConfig().getStringList("lore");
                            lore.add(input);
                            customItem.getConfig().set("lore", lore);
                            player.sendMessage(ChatColor.GREEN + "¡Línea añadida! No olvides guardar el ítem.");
                        }
                        open(); 
                    }
                }.runTask(plugin);
            });
            return;
        }
        
        if (displayName.equals("Volver al Editor")) {
            previousMenu.open();
            return;
        }
        
        if (clickedItem.getType() == Material.PAPER) {
            String lineToRemove = ChatColor.stripColor(clickedItem.getItemMeta().getLore().get(0));
            List<String> lore = customItem.getConfig().getStringList("lore");
            lore.removeIf(line -> ChatColor.stripColor(line).equals(lineToRemove));
            customItem.getConfig().set("lore", lore);
            player.sendMessage(ChatColor.RED + "Línea eliminada.");
            open();
        }
    }

    @Override
    public void setItems() {
        List<String> lore = customItem.getConfig().getStringList("lore");
        for (int i = 0; i < Math.min(lore.size(), 45); i++) {
            String line = lore.get(i);
            // --- CORRECCIÓN: USAR EL MÉTODO SIN AMBIGÜEDAD ---
            inventory.setItem(i, createGuiItem(Material.PAPER, ChatColor.translateAlternateColorCodes('&', line), ChatColor.stripColor(line), ChatColor.RED + "Click para eliminar"));
        }

        inventory.setItem(45, createGuiItem(Material.BARRIER, ChatColor.RED + "Volver al Editor"));
        inventory.setItem(53, createGuiItem(Material.LIME_DYE, ChatColor.GREEN + "Añadir Línea"));
        fillEmptySlots();
    }
}