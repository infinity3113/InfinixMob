package com.infinity3113.infinixmob.gui;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.gui.editor.MenuGUI;
import com.infinity3113.infinixmob.playerclass.PlayerClass;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import com.infinity3113.infinixmob.utils.SkillValueCalculator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkillTreeGUI extends MenuGUI {

    private final PlayerData playerData;
    private final PlayerClass playerClass;

    public SkillTreeGUI(InfinixMob plugin, Player player) {
        super(plugin, player);
        this.playerData = plugin.getPlayerClassManager().getPlayerData(player);
        this.playerClass = playerData.getPlayerClass();
    }

    @Override
    public String getMenuName() {
        return playerClass.getDisplayName() + " - Puntos: " + playerData.getSkillPoints();
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE || clickedItem.getType() == Material.AIR) {
            return;
        }

        String skillId = clickedItem.getItemMeta().getPersistentDataContainer().get(GUI_ITEM_KEY, PersistentDataType.STRING);
        if (skillId == null) return;

        Optional<ConfigurationSection> skillConfigOpt = plugin.getSkillManager().getSkillConfig(skillId);
        if (!skillConfigOpt.isPresent()) return;
        ConfigurationSection skillConfig = skillConfigOpt.get();

        int currentSkillLevel = playerData.getSkillLevel(skillId);
        int maxSkillLevel = skillConfig.getInt("max-level", 1);
        int playerLevelReq = skillConfig.getInt("skill-req", 1);

        if (playerData.getSkillPoints() > 0 && currentSkillLevel < maxSkillLevel && playerData.getLevel() >= playerLevelReq) {
            playerData.setSkillPoints(playerData.getSkillPoints() - 1);
            playerData.setSkillLevel(skillId, currentSkillLevel + 1);
            open(); // Recarga la GUI para mostrar los cambios
        }
    }

    @Override
    public void setItems() {
        setPlayerInfoPanel();

        Map<Integer, String> skills = playerClass.getSkills();

        // MENSAJE DE DEPURACIÓN: Te avisará si no encuentra skills
        if (skills.isEmpty()) {
            player.sendMessage(ChatColor.RED + "[DEBUG] No se encontraron habilidades en la configuración de la clase '" + playerClass.getId() + "'. Revisa el archivo 'plugins/InfinixMob/classes/" + playerClass.getId() + ".yml'.");
        }

        for (Map.Entry<Integer, String> entry : skills.entrySet()) {
            // Lógica de slots mejorada: skill 1 va al slot 10, skill 2 al 11...
            int slot = 9 + entry.getKey();
            if (slot >= getSlots()) continue; // Evita errores si hay más skills que espacio

            String skillId = entry.getValue();
            plugin.getSkillManager().getSkillConfig(skillId).ifPresent(skillConfig -> {
                inventory.setItem(slot, createSkillItem(skillId, skillConfig));
            });
        }
        fillEmptySlots();
    }

    private void setPlayerInfoPanel() {
        ItemStack profile = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta profileMeta = (SkullMeta) profile.getItemMeta();
        if (profileMeta != null) {
            profileMeta.setOwningPlayer(player);
            profileMeta.setDisplayName(ChatColor.GREEN + player.getName());
            profileMeta.setLore(List.of(
                    ChatColor.GRAY + "Clase: " + playerClass.getDisplayName(),
                    ChatColor.GRAY + "Nivel: " + ChatColor.YELLOW + playerData.getLevel()
            ));
            profile.setItemMeta(profileMeta);
        }
        inventory.setItem(3, profile);

        ItemStack expBar = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta expMeta = expBar.getItemMeta();
        expMeta.setDisplayName(ChatColor.GOLD + "Experiencia");
        expMeta.setLore(List.of(
                ChatColor.GRAY + "Progreso: " + ChatColor.YELLOW + (int) playerData.getExperience() + " / " + (int) playerData.getNextLevelExp()
        ));
        expBar.setItemMeta(expMeta);
        inventory.setItem(4, expBar);
        
        ItemStack skillPoints = new ItemStack(Material.NETHER_STAR);
        ItemMeta pointsMeta = skillPoints.getItemMeta();
        pointsMeta.setDisplayName(ChatColor.AQUA + "Puntos de Habilidad");
        pointsMeta.setLore(List.of(
                ChatColor.GRAY + "Disponibles: " + ChatColor.YELLOW + playerData.getSkillPoints()
        ));
        skillPoints.setItemMeta(pointsMeta);
        inventory.setItem(5, skillPoints);
    }

    private ItemStack createSkillItem(String skillId, ConfigurationSection skillConfig) {
        Material icon = Material.matchMaterial(skillConfig.getString("icon", "BARRIER"));
        int currentLevel = playerData.getSkillLevel(skillId);
        int maxLevel = skillConfig.getInt("max-level", 1);
        int playerLevelReq = skillConfig.getInt("skill-req", 1);

        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', skillConfig.getString("display-name", skillId)));
        
        List<String> lore = new ArrayList<>();
        for (String line : skillConfig.getStringList("lore")) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        lore.add("");

        if (playerData.getLevel() < playerLevelReq) {
            lore.add(ChatColor.RED + "Bloqueado (Requiere Nivel " + playerLevelReq + ")");
        } else if (currentLevel == 0) {
            lore.add(ChatColor.GRAY + "Nivel: 0 / " + maxLevel);
             if (playerData.getSkillPoints() > 0) {
                lore.add(ChatColor.YELLOW + "¡Click para aprender!");
            } else {
                lore.add(ChatColor.RED + "Puntos Insuficientes");
            }
        } else if (currentLevel >= maxLevel) {
            lore.add(ChatColor.GOLD + "Nivel: " + currentLevel + " / " + maxLevel + " (MAX)");
        } else {
            lore.add(ChatColor.GRAY + "Nivel: " + currentLevel + " / " + maxLevel);
            if (playerData.getSkillPoints() > 0) {
                lore.add(ChatColor.GREEN + "¡Click para subir de nivel!");
            } else {
                lore.add(ChatColor.RED + "Puntos Insuficientes");
            }
        }

        lore.add("");

        List<String> finalLore = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{([a-zA-Z0-9_.-]+)\\}");

        for (String line : lore) {
            Matcher matcher = pattern.matcher(line);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String key = matcher.group(1);
                ConfigurationSection valueConfig = skillConfig.getConfigurationSection(key);
                if (valueConfig != null) {
                    double currentValue = SkillValueCalculator.calculate(valueConfig, currentLevel > 0 ? currentLevel : 1);
                    double nextValue = SkillValueCalculator.calculate(valueConfig, currentLevel + 1);

                    String replacement = ChatColor.WHITE + String.format("%.1f", currentValue);
                    if (currentLevel < maxLevel && currentLevel > 0) {
                        replacement += ChatColor.GRAY + " -> " + ChatColor.GREEN + String.format("%.1f", nextValue);
                    }
                    matcher.appendReplacement(sb, replacement);
                }
            }
            matcher.appendTail(sb);
            finalLore.add(sb.toString());
        }

        meta.setLore(finalLore);
        meta.getPersistentDataContainer().set(GUI_ITEM_KEY, PersistentDataType.STRING, skillId);
        item.setItemMeta(meta);

        return item;
    }
}