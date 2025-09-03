package com.infinity3113.infinixmob.utils;

import org.bukkit.configuration.ConfigurationSection;

public class SkillValueCalculator {

    public static double calculate(ConfigurationSection config, int level) {
        if (config == null) {
            return 0.0;
        }
        double base = config.getDouble("base", 0.0);
        double perLevel = config.getDouble("per-level", 0.0);
        double max = config.getDouble("max", Double.MAX_VALUE);
        double min = config.getDouble("min", 0.0);

        // La fórmula clave: valor base + (valor por nivel * (nivel actual - 1))
        double finalValue = base + (perLevel * (level - 1));

        // Asegurarse de que el valor no exceda los límites definidos
        return Math.max(min, Math.min(max, finalValue));
    }
}