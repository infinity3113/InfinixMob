package com.infinity3113.infinixmob.playerclass;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private PlayerClass playerClass;
    private int level;
    private double experience;
    private double currentResource;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.level = 1;
        this.experience = 0;
        this.playerClass = null; // Sin clase al inicio
        this.currentResource = 0;
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

    public double getNextLevelExp() {
        // Fórmula de experiencia simple, puedes ajustarla
        return 100 * Math.pow(1.5, level - 1);
    }

    public void addExperience(double amount) {
        if (this.playerClass == null) return; // No se gana exp sin clase
        this.experience += amount;
    }

    public boolean canLevelUp() {
        return this.playerClass != null && this.experience >= getNextLevelExp();
    }

    public void levelUp() {
        if (canLevelUp()) {
            this.experience -= getNextLevelExp();
            this.level++;
        }
    }
}