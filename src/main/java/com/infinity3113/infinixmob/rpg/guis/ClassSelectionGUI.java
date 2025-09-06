package com.infinity3113.infinixmob.rpg.guis;

import com.infinity3113.infinixmob.rpg.managers.ClassConfigManager;
import com.infinity3113.infinixmob.rpg.managers.PlayerClassManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class ClassSelectionGUI implements Listener {

    private final PlayerClassManager playerClassManager;
    private final ClassConfigManager classConfigManager;
    private final String guiTitle = "Elige tu Clase";

    public ClassSelectionGUI(PlayerClassManager playerClassManager, ClassConfigManager classConfigManager) {
        this.playerClassManager = playerClassManager;
        this.classConfigManager = classConfigManager;
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, guiTitle);

        for (String className : classConfigManager.getAvailableClasses()) {
            FileConfiguration classConfig = classConfigManager.getClassConfig(className);
            if (classConfig != null) {
                String displayName = classConfig.getString("name", "Clase Desconocida");
                String iconMaterialName = classConfig.getString("gui_icon", "STONE");
                List<String> loreLines = classConfig.getStringList("description");

                Material iconMaterial = Material.matchMaterial(iconMaterialName);
                if (iconMaterial == null) {
                    iconMaterial = Material.STONE;
                }
                ItemStack classItem = new ItemStack(iconMaterial);
                ItemMeta meta = classItem.getItemMeta();

                if (meta != null) {
                    meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + displayName);

                    List<String> coloredLore = loreLines.stream()
                            .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                            .collect(Collectors.toList());
                    meta.setLore(coloredLore);

                    classItem.setItemMeta(meta);
                }
                gui.addItem(classItem);
            }
        }
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(guiTitle)) {
            return;
        }
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR || !clickedItem.hasItemMeta()) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemMeta meta = clickedItem.getItemMeta();
        String displayName = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();

        String internalClassName = classConfigManager.getInternalClassName(displayName);

        if (internalClassName != null) {
            FileConfiguration classConfig = classConfigManager.getClassConfig(internalClassName);
            if (classConfig != null) {
                playerClassManager.setPlayerClass(player, internalClassName, classConfig);
                player.sendMessage(ChatColor.GREEN + "¡Has elegido la clase " + classConfig.getString("name") + "!");
                player.closeInventory();
            }
        }
    }
}