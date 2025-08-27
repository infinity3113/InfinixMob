package com.infinity3113.infinixmob.gui.editors;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.BiConsumer;

public class AnvilInputGUI implements Listener {

    private final Player player;
    private final InfinixMob plugin;
    private final BiConsumer<Player, String> onConfirm;
    private Inventory inventory;

    public AnvilInputGUI(InfinixMob plugin, Player player, String initialText, BiConsumer<Player, String> onConfirm) {
        this.plugin = plugin;
        this.player = player;
        this.onConfirm = onConfirm;
        this.inventory = Bukkit.createInventory(null, org.bukkit.event.inventory.InventoryType.ANVIL, "Introduce un valor:");

        ItemStack inputItem = new ItemStack(Material.PAPER);
        ItemMeta meta = inputItem.getItemMeta();
        meta.setDisplayName(initialText);
        inputItem.setItemMeta(meta);
        inventory.setItem(0, inputItem);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().equals(inventory) && event.getWhoClicked() instanceof Player) {
            Player p = (Player) event.getWhoClicked();
            if (p.equals(player) && event.getSlot() == 2) { // Slot de salida del yunque
                ItemStack result = event.getCurrentItem();
                if (result != null && result.hasItemMeta()) {
                    String newText = result.getItemMeta().getDisplayName();
                    close();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            onConfirm.accept(player, newText);
                        }
                    }.runTask(plugin);
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            close();
        }
    }

    private void close() {
        HandlerList.unregisterAll(this);
        inventory.clear();
    }
}