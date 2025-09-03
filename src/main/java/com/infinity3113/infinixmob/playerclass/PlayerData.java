package com.infinity3113.infinixmob.playerclass;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerData {

    private final UUID uuid;
    private PlayerClass playerClass;
    private int level;
    private double experience;
    private double currentResource;
    private int skillPoints;
    private Map<String, Integer> skillLevels = new HashMap<>();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.level = 1;
        this.experience = 0;
        this.playerClass = null;
        this.currentResource = 0;
        this.skillPoints = 1; // Empieza con 1 punto de habilidad
    }

    // Constructor para cargar datos
    public PlayerData(UUID uuid, PlayerClass playerClass, int level, double experience, double currentResource, int skillPoints, Map<String, Integer> skillLevels) {
        this.uuid = uuid;
        this.playerClass = playerClass;
        this.level = level;
        this.experience = experience;
        this.currentResource = currentResource;
        this.skillPoints = skillPoints;
        this.skillLevels = skillLevels;
    }

    public void saveToConfig(ConfigurationSection config) {
        config.set("class", playerClass != null ? playerClass.getId() : null);
        config.set("level", level);
        config.set("experience", experience);
        config.set("current-resource", currentResource);
        config.set("skill-points", skillPoints);
        // Guardar los niveles de las habilidades
        config.set("skill-levels", this.skillLevels.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }


    public UUID getUuid() {
        return uuid;
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public void setPlayerClass(PlayerClass playerClass) {
        this.playerClass = playerClass;
        if (playerClass != null) {
            this.currentResource = playerClass.getMaxResource();
        } else {
            this.currentResource = 0;
        }
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getExperience() {
        return experience;
    }

    public void setExperience(double experience) {
        this.experience = experience;
    }

    public double getCurrentResource() {
        return currentResource;
    }

    public void setCurrentResource(double currentResource) {
        this.currentResource = currentResource;
    }
    
    public int getSkillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    public int getSkillLevel(String skillId) {
        return skillLevels.getOrDefault(skillId.toLowerCase(), 0);
    }
    
    public void setSkillLevel(String skillId, int level) {
        skillLevels.put(skillId.toLowerCase(), level);
    }


    public double getNextLevelExp() {
        return 100 * Math.pow(1.5, level - 1);
    }

    public void addExperience(double amount) {
        if (this.playerClass == null) return;
        this.experience += amount;
    }

    public boolean canLevelUp() {
        return this.playerClass != null && this.experience >= getNextLevelExp();
    }

    public void levelUp() {
        if (canLevelUp()) {
            this.experience -= getNextLevelExp();
            this.level++;
            this.skillPoints++; // Gana un punto de habilidad al subir de nivel
        }
    }
}