package com.infinity3113.infinixmob.core;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SkillManager {

    private final InfinixMob plugin;
    private final Map<String, ConfigurationSection> skillConfigs = new HashMap<>();

    public SkillManager(InfinixMob plugin) {
        this.plugin = plugin;
    }

    public void loadSkills() {
        skillConfigs.clear();
        File skillsFolder = new File(plugin.getDataFolder(), "Skills");
        if (!skillsFolder.exists()) {
            skillsFolder.mkdirs();
        }

        File[] files = skillsFolder.listFiles();
        if (files == null) return;

        for (File skillFile : files) {
            if (skillFile.getName().endsWith(".yml")) {
                FileConfiguration skillConfig = YamlConfiguration.loadConfiguration(skillFile);
                try {
                    for (String skillId : skillConfig.getKeys(false)) {
                        ConfigurationSection skillSection = skillConfig.getConfigurationSection(skillId);
                        if (skillSection == null) continue;

                        if (!skillSection.isList("Mechanics")) {
                            plugin.getLogger().warning("Skill '" + skillId + "' en el archivo '" + skillFile.getName() + "' no tiene una sección de 'Mechanics'. Saltando.");
                            continue;
                        }
                        
                        skillConfigs.put(skillId, skillSection);
                        plugin.getLogger().info("Cargada Skill: " + skillId + " desde " + skillFile.getName());
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("No se pudo cargar el archivo de skill: " + skillFile.getName() + ". Revisa errores de YAML.");
                    e.printStackTrace();
                }
            }
        }
    }

    public Optional<ConfigurationSection> getSkillConfig(String skillId) {
        return Optional.ofNullable(skillConfigs.get(skillId));
    }

    public void executeSkill(String skillId, LivingEntity caster, Entity initialTarget) {
        executeSkill(skillId, skillId, caster, initialTarget);
    }

    public void executeSkill(String skillId, String contextSkillId, LivingEntity caster, Entity initialTarget) {
        ConfigurationSection skillSection = skillConfigs.get(skillId);
        if (skillSection == null) {
            plugin.getLogger().warning("Se intentó ejecutar una skill inexistente: " + skillId);
            return;
        }
        
        List<Map<?, ?>> mechanics = skillSection.getMapList("Mechanics");
        if (mechanics.isEmpty()) return;

        for (Map<?, ?> mechanicData : mechanics) {
            String targeterString = (String) mechanicData.get("targeter");
            List<Entity> targets = plugin.getTargeterManager().getTargets(caster, initialTarget, targeterString);

            for (Entity finalTarget : targets) {
                String mechanicType = (String) mechanicData.get("type");

                Object rawParams = mechanicData.get("parameters");
                Map<String, Object> parametersMap = new HashMap<>();
                if (rawParams instanceof Map) {
                    parametersMap.putAll((Map<String, Object>) rawParams);
                }
                
                parametersMap.put("skillId", contextSkillId);

                plugin.getMechanicManager().executeMechanic(mechanicType, caster, finalTarget, parametersMap);
            }
        }
    }

    public Set<String> getLoadedSkillNames() {
        return skillConfigs.keySet();
    }
}