package com.infinity3113.infinixmob.playerclass;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerClass {

    private final String id;
    private final ConfigurationSection config;

    public PlayerClass(String id, ConfigurationSection config) {
        this.id = id;
        this.config = config;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return config.getString("display-name", "Clase Desconocida");
    }

    public String getResourceType() {
        return config.getString("resource-type", "MANA");
    }

    public double getMaxResource() {
        return config.getDouble("max-resource", 100.0);
    }

    public double getResourceRegen() {
        return config.getDouble("resource-regen", 1.0);
    }

    public Map<Integer, String> getSkills() {
        ConfigurationSection skillsSection = config.getConfigurationSection("skills");
        if (skillsSection == null) {
            return Collections.emptyMap();
        }
        // Tenemos que convertir las keys a Integer
        return skillsSection.getValues(false).entrySet().stream()
                .collect(Collectors.toMap(entry -> Integer.parseInt(entry.getKey()), entry -> (String) entry.getValue()));
    }

    public String getPassiveSkill() {
        return config.getString("passive-skill");
    }

    public List<String> getLevelUpCommands(int level) {
        return config.getStringList("level-up-rewards." + level);
    }
}