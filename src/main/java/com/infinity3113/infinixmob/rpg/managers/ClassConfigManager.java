package com.infinity3113.infinixmob.rpg.managers;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClassConfigManager {

    private final InfinixMob plugin;
    private final Map<String, FileConfiguration> classConfigs = new HashMap<>();

    public ClassConfigManager(InfinixMob plugin) {
        this.plugin = plugin;
    }

    public void loadClasses() {
        File classFolder = new File(plugin.getDataFolder(), "rpg/classes");
        if (!classFolder.exists()) {
            classFolder.mkdirs();
            plugin.saveResource("rpg/classes/mago.yml", false);
        }

        File[] classFiles = classFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (classFiles == null) {
            plugin.getLogger().warning("No se encontraron archivos de clase en la carpeta 'rpg/classes'.");
            return;
        }

        for (File classFile : classFiles) {
            String className = classFile.getName().replace(".yml", "");
            FileConfiguration config = YamlConfiguration.loadConfiguration(classFile);
            classConfigs.put(className.toLowerCase(), config);
            plugin.getLogger().info("Clase '" + className + "' cargada correctamente.");
        }
    }

    public FileConfiguration getClassConfig(String className) {
        return classConfigs.get(className.toLowerCase());
    }

    public Set<String> getAvailableClasses() {
        return classConfigs.keySet();
    }
    
    public String getInternalClassName(String displayName) {
        for (Map.Entry<String, FileConfiguration> entry : classConfigs.entrySet()) {
            String configName = entry.getValue().getString("name", "");
            if (configName.equalsIgnoreCase(displayName)) {
                return entry.getKey();
            }
        }
        return null;
    }
}