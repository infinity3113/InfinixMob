package com.infinity3113.infinixmob.rpg.guis;

import com.infinity3113.infinixmob.rpg.managers.ClassConfigManager;
import com.infinity3113.infinixmob.rpg.managers.PlayerClassManager;
import com.infinity3113.infinixmob.rpg.managers.PlayerClassManager.PlayerData;
import com.infinity3113.infinixmob.rpg.managers.RpgSkillManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SkillsGUI implements Listener {

    private final PlayerClassManager playerManager;
    private final ClassConfigManager classManager;
    private final RpgSkillManager skillManager;
    private final String guiTitle = "Habilidades y Asignaciones";

    private final Map<UUID, String> playerHoldingSkill = new HashMap<>();

    public SkillsGUI(PlayerClassManager playerManager, ClassConfigManager classManager, RpgSkillManager skillManager) {
        this.playerManager = playerManager;
        this.classManager = classManager;
        this.skillManager = skillManager;
    }

    public void open(Player player) {
        PlayerData data = playerManager.getPlayerData(player);
        if (data == null) return;

        Inventory gui = Bukkit.createInventory(null, 54, guiTitle);
        FileConfiguration classConfig = classManager.getClassConfig(data.getClassName());

        for (String skillId : classConfig.getStringList("available_skills")) {
            if(data.getSkillLevel(skillId) > 0) {
                gui.addItem(createSkillItem(skillId, data));
            }
        }

        ItemStack placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = placeholder.getItemMeta();
        meta.setDisplayName(" ");
        placeholder.setItemMeta(meta);
        for (int i = 36; i < 45; i++) {
            gui.setItem(i, placeholder);
        }

        for (int i = 0; i < 4; i++) {
            String boundSkillId = data.getSkillBind(i);
            if(boundSkillId != null) {
                gui.setItem(45 + i, createSkillItem(boundSkillId, data));
            } else {
                ItemStack bindSlotItem = new ItemStack(Material.GRAY_DYE);
                ItemMeta bindMeta = bindSlotItem.getItemMeta();
                bindMeta.setDisplayName(ChatColor.GOLD + "Slot de Habilidad " + (i + 1));
                bindMeta.setLore(Arrays.asList(
                        ChatColor.GRAY + "Arrastra una habilidad aquí",
                        ChatColor.GRAY + "para asignarla a esta tecla."
                ));
                bindSlotItem.setItemMeta(bindMeta);
                gui.setItem(45 + i, bindSlotItem);
            }
        }
        
        ItemStack skillPointsItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta spMeta = skillPointsItem.getItemMeta();
        spMeta.setDisplayName(ChatColor.AQUA + "Puntos de Habilidad");
        spMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Tienes: " + ChatColor.GOLD + data.getSkillPoints()));
        skillPointsItem.setItemMeta(spMeta);
        gui.setItem(53, skillPointsItem);

        player.openInventory(gui);
    }

    private ItemStack createSkillItem(String skillId, PlayerData data) {
        Optional<ConfigurationSection> skillConfigOpt = skillManager.getSkillConfig(skillId);
        if (!skillConfigOpt.isPresent()) return new ItemStack(Material.STONE);
        ConfigurationSection skillConfig = skillConfigOpt.get();

        int currentLevel = data.getSkillLevel(skillId);
        int skillPoints = data.getSkillPoints();

        ItemStack skillItem = new ItemStack(Material.matchMaterial(skillConfig.getString("gui_icon", "STONE")));
        ItemMeta meta = skillItem.getItemMeta();
        int maxLevel = skillConfig.getInt("max_level", 1);

        meta.setDisplayName(ChatColor.GOLD + skillConfig.getString("display_name"));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Nivel: " + ChatColor.AQUA + currentLevel + "/" + maxLevel);
        lore.add("");
        lore.add(ChatColor.YELLOW + "Maná: " + ChatColor.WHITE + skillManager.getSkillStat(skillId, currentLevel, "mana_cost"));
        lore.add(ChatColor.YELLOW + "Daño: " + ChatColor.WHITE + skillManager.getSkillStat(skillId, currentLevel, "damage"));
        lore.add(ChatColor.YELLOW + "Enfriamiento: " + ChatColor.WHITE + skillManager.getSkillStat(skillId, currentLevel, "cooldown") + "s");
        lore.add("");

        if (currentLevel < maxLevel) {
            if (skillPoints > 0) {
                lore.add(ChatColor.GREEN + "¡Shift + Clic para mejorar!");
                lore.add(ChatColor.GOLD + "Costo: 1 Punto de Habilidad");
            } else {
                lore.add(ChatColor.RED + "¡No tienes puntos para mejorar!");
            }
        } else {
            lore.add(ChatColor.GREEN + "¡NIVEL MÁXIMO ALCANZADO!");
        }
        lore.add("");
        lore.add(ChatColor.AQUA + "Clic para arrastrar y asignar.");
        lore.add(ChatColor.AQUA + "Clic derecho en un slot para desasignar.");

        meta.setLore(lore);
        skillItem.setItemMeta(meta);
        return skillItem;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(guiTitle)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        PlayerData data = playerManager.getPlayerData(player);
        if (data == null) return;

        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        if (cursorItem != null && cursorItem.getType() != Material.AIR) {
            String heldSkillId = playerHoldingSkill.get(player.getUniqueId());
            if (heldSkillId == null) return;

            if (event.getRawSlot() >= 45 && event.getRawSlot() <= 48) {
                int bindSlot = event.getRawSlot() - 45;
                data.setSkillBind(bindSlot, heldSkillId);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }

            playerHoldingSkill.remove(player.getUniqueId());
            player.setItemOnCursor(null);
            open(player);
            return;
        }

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        String skillId = findSkillIdByName(data, displayName);

        if (event.getRawSlot() >= 45 && event.getRawSlot() <= 48) {
            if (event.getClick() == ClickType.RIGHT) {
                int bindSlot = event.getRawSlot() - 45;
                data.clearSkillBind(bindSlot);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.8f);
                open(player);
            }
            return;
        }
        
        if (skillId != null && event.getRawSlot() < 36) {
            if (event.getClick() == ClickType.SHIFT_LEFT) {
                playerManager.upgradeSkill(player, skillId);
                open(player);
            }
            else if (event.getClick() == ClickType.LEFT) {
                playerHoldingSkill.put(player.getUniqueId(), skillId);
                player.setItemOnCursor(clickedItem.clone());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (playerHoldingSkill.containsKey(event.getPlayer().getUniqueId())) {
            playerHoldingSkill.remove(event.getPlayer().getUniqueId());
            event.getPlayer().setItemOnCursor(null);
        }
    }

    private String findSkillIdByName(PlayerData data, String displayName) {
        FileConfiguration classConfig = classManager.getClassConfig(data.getClassName());
        for (String skillId : classConfig.getStringList("available_skills")) {
            Optional<ConfigurationSection> skillConfigOpt = skillManager.getSkillConfig(skillId);
            if (skillConfigOpt.isPresent() && skillConfigOpt.get().getString("display_name").equalsIgnoreCase(displayName)) {
                return skillId;
            }
        }
        return null;
    }
}