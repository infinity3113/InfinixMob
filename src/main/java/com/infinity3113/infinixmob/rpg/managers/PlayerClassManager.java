package com.infinity3113.infinixmob.rpg.managers;

import com.infinity3113.infinixmob.InfinixMob;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerClassManager {

    private final InfinixMob plugin;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public PlayerClassManager(InfinixMob plugin) {
        this.plugin = plugin;
        startManaRegenTask();
    }

    public void savePlayerData(Player player) {
        PlayerData data = getPlayerData(player);
        if (data == null) return;
        File playerFile = new File(plugin.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);

        playerConfig.set("class", data.getClassName());
        playerConfig.set("level", data.getLevel());
        playerConfig.set("experience", data.getExperience());
        playerConfig.set("skillPoints", data.getSkillPoints());

        for (Map.Entry<String, Integer> entry : data.getSkillLevels().entrySet()) {
            playerConfig.set("skills." + entry.getKey(), entry.getValue());
        }

        playerConfig.set("skill_binds", null);
        for(Map.Entry<Integer, String> entry : data.getSkillBinds().entrySet()){
            playerConfig.set("skill_binds." + entry.getKey(), entry.getValue());
        }

        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar datos para " + player.getName(), e);
        }
    }

    public void loadPlayerData(Player player) {
        File playerFile = new File(plugin.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        if (!playerFile.exists()) return;
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        String className = playerConfig.getString("class");
        if (className == null) return;

        int level = playerConfig.getInt("level", 1);
        double experience = playerConfig.getDouble("experience", 0);
        int skillPoints = playerConfig.getInt("skillPoints", 1);

        FileConfiguration classConfig = plugin.getClassConfigManager().getClassConfig(className);
        if (classConfig == null) return;

        double maxHealth = classConfig.getDouble("stats.base_health", 20.0) + (classConfig.getDouble("stats_per_level.add_health", 0) * (level - 1));
        double maxMana = classConfig.getDouble("stats.base_mana", 100.0) + (classConfig.getDouble("stats_per_level.add_mana", 0) * (level - 1));

        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        player.setHealth(maxHealth);

        PlayerData data = new PlayerData(className, level, experience, maxMana, skillPoints);
        if (playerConfig.isConfigurationSection("skills")) {
            for (String skillId : playerConfig.getConfigurationSection("skills").getKeys(false)) {
                data.setSkillLevel(skillId, playerConfig.getInt("skills." + skillId));
            }
        }

        if(playerConfig.isConfigurationSection("skill_binds")){
            ConfigurationSection bindsSection = playerConfig.getConfigurationSection("skill_binds");
            for(String key : bindsSection.getKeys(false)){
                try {
                    int slot = Integer.parseInt(key);
                    String skillId = bindsSection.getString(key);
                    data.setSkillBind(slot, skillId);
                } catch (NumberFormatException ignored) {}
            }
        }

        playerDataMap.put(player.getUniqueId(), data);
    }

    public void removePlayerData(Player player) {
        playerDataMap.remove(player.getUniqueId());
    }

    public void addExperience(Player player, double amount) {
        PlayerData data = getPlayerData(player);
        if (data == null) return;
        data.addExperience(amount);
        player.sendMessage(ChatColor.YELLOW + "+ " + String.format("%.0f", amount) + " XP");

        FileConfiguration classConfig = plugin.getClassConfigManager().getClassConfig(data.getClassName());
        if(classConfig == null) return;

        double xpNeeded = classConfig.getDouble("leveling.base_xp_needed", 100) + (data.getLevel() - 1) * classConfig.getDouble("leveling.xp_increase_per_level", 50);

        while (data.getExperience() >= xpNeeded) {
            data.removeExperience(xpNeeded);
            data.levelUp();
            data.addSkillPoint(classConfig.getInt("stats_per_level.skill_points_per_level", 1));

            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "¡Has subido de nivel! Ahora eres nivel " + data.getLevel() + ".");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            double addHealth = classConfig.getDouble("stats_per_level.add_health", 0);
            double addMana = classConfig.getDouble("stats_per_level.add_mana", 0);
            data.setMaxMana(data.getMaxMana() + addMana);
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() + addHealth);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

            xpNeeded = classConfig.getDouble("leveling.base_xp_needed", 100) + (data.getLevel() - 1) * classConfig.getDouble("leveling.xp_increase_per_level", 50);
        }
    }

    public void upgradeSkill(Player player, String skillId) {
        PlayerData data = getPlayerData(player);
        if (data == null) return;

        int currentLevel = data.getSkillLevel(skillId);
        int maxLevel = plugin.getRpgSkillManager().getSkillConfig(skillId).get().getInt("max_level", 1);
        if(currentLevel >= maxLevel){
            player.sendMessage(ChatColor.RED + "¡Ya tienes esta habilidad al nivel máximo!");
            return;
        }

        if (data.getSkillPoints() > 0) {
            data.setSkillLevel(skillId, currentLevel + 1);
            data.setSkillPoints(data.getSkillPoints() - 1);
            player.sendMessage(ChatColor.GREEN + "¡Has mejorado la habilidad " + skillId + "!");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.5f);
        } else {
            player.sendMessage(ChatColor.RED + "¡No tienes puntos de habilidad!");
        }
    }

    public void setPlayerClass(Player player, String className, FileConfiguration config) {
        double maxMana = config.getDouble("stats.base_mana", 100.0);
        double maxHealth = config.getDouble("stats.base_health", 20.0);
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        player.setHealth(maxHealth);
        PlayerData data = new PlayerData(className, 1, 0, maxMana, 1);
        playerDataMap.put(player.getUniqueId(), data);
        savePlayerData(player);
    }

    public PlayerData getPlayerData(Player player) { return playerDataMap.get(player.getUniqueId()); }
    public String getPlayerClass(Player player) { PlayerData data = getPlayerData(player); return (data != null) ? data.getClassName() : null; }
    public boolean hasClass(Player player) { return playerDataMap.containsKey(player.getUniqueId()); }

    public void setCooldown(Player player, String abilityName, int seconds) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(abilityName, System.currentTimeMillis() + (seconds * 1000L));
    }
    public boolean isOnCooldown(Player player, String abilityName) {
        return cooldowns.getOrDefault(player.getUniqueId(), new HashMap<>()).getOrDefault(abilityName, 0L) > System.currentTimeMillis();
    }
    private void startManaRegenTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    PlayerData data = getPlayerData(player);
                    if (data != null) {
                        FileConfiguration classConfig = plugin.getClassConfigManager().getClassConfig(data.getClassName());
                        if (classConfig != null) {
                            double manaRegen = classConfig.getDouble("stats.mana_regen", 2.5);
                            data.addMana(manaRegen);
                            if(!data.isInCastingMode()){
                                String manaBar = ChatColor.BLUE + "Maná: " + ChatColor.AQUA + String.format("%.0f", data.getCurrentMana()) + "/" + String.format("%.0f", data.getMaxMana());
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(manaBar));
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public static class PlayerData {
        private String className;
        private int level;
        private double experience;
        private double maxMana;
        private double currentMana;
        private int skillPoints;
        private final Map<String, Integer> skillLevels = new HashMap<>();
        private boolean inCastingMode = false;
        private final Map<Integer, String> skillBinds = new HashMap<>();


        public PlayerData(String className, int level, double experience, double maxMana, int skillPoints) {
            this.className = className;
            this.level = level;
            this.experience = experience;
            this.maxMana = maxMana;
            this.currentMana = maxMana;
            this.skillPoints = skillPoints;
        }

        public String getClassName() { return className; }
        public int getLevel() { return level; }
        public double getExperience() { return experience; }
        public double getMaxMana() { return maxMana; }
        public double getCurrentMana() { return currentMana; }
        public int getSkillPoints() { return skillPoints; }
        public int getSkillLevel(String skillId) { return skillLevels.getOrDefault(skillId, 0); }

        public void setMaxMana(double maxMana) { this.maxMana = maxMana; }
        public void levelUp() { this.level++; }
        public void addExperience(double amount) { this.experience += amount; }
        public void removeExperience(double amount) { this.experience -= amount; }
        public void addMana(double amount) { this.currentMana = Math.min(this.maxMana, this.currentMana + amount); }
        public void removeMana(double amount) { this.currentMana = Math.max(0, this.currentMana - amount); }
        public boolean hasEnoughMana(double amount) { return this.currentMana >= amount; }
        public void setSkillPoints(int skillPoints) { this.skillPoints = skillPoints; }
        public void setSkillLevel(String skillId, int level) { this.skillLevels.put(skillId, level); }
        public void addSkillPoint(int amount) { this.skillPoints += amount; }
        public void setLevel(int level) { this.level = level; }
        public void setExperience(double experience) { this.experience = experience; }
        public boolean isInCastingMode() { return inCastingMode; }
        public void setCastingMode(boolean inCastingMode) { this.inCastingMode = inCastingMode; }
        public Map<Integer, String> getSkillBinds() { return skillBinds; }
        public String getSkillBind(int slot) { return skillBinds.get(slot); }
        public void setSkillBind(int slot, String skillId) { skillBinds.put(slot, skillId); }
        public void clearSkillBind(int slot) { skillBinds.remove(slot); }
        public Map<String, Integer> getSkillLevels() {
            return skillLevels;
        }
    }
}