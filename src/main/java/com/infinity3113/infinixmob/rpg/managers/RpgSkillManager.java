package com.infinity3113.infinixmob.rpg.managers;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RpgSkillManager {

    private final InfinixMob plugin;
    private final Map<String, ConfigurationSection> skillConfigs = new HashMap<>();

    public RpgSkillManager(InfinixMob plugin) {
        this.plugin = plugin;
    }

    public void loadSkills() {
        skillConfigs.clear();
        File skillsFolder = new File(plugin.getDataFolder(), "rpg/skills");
        if (!skillsFolder.exists()) {
            skillsFolder.mkdirs();
            plugin.saveResource("rpg/skills/mago-skills.yml", false);
        }

        File[] files = skillsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File skillFile : files) {
            FileConfiguration skillConfig = YamlConfiguration.loadConfiguration(skillFile);
            for (String skillId : skillConfig.getKeys(false)) {
                ConfigurationSection skillSection = skillConfig.getConfigurationSection(skillId);
                if (skillSection != null) {
                    skillConfigs.put(skillId.toLowerCase(), skillSection);
                    plugin.getLogger().info("Cargada habilidad de RPG: " + skillId + " desde " + skillFile.getName());
                }
            }
        }
    }

    public Optional<ConfigurationSection> getSkillConfig(String skillId) {
        return Optional.ofNullable(skillConfigs.get(skillId.toLowerCase()));
    }

    public double getSkillStat(String skillId, int level, String stat) {
        return getSkillConfig(skillId)
                .map(config -> {
                    int safeLevel = Math.max(1, level);
                    return config.getDouble("levels." + safeLevel + "." + stat, 0.0);
                })
                .orElse(0.0);
    }
}