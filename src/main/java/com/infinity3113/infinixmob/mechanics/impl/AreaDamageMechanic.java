package com.infinity3113.infinixmob.mechanics.impl;

import com.google.gson.reflect.TypeToken;
import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import com.infinity3113.infinixmob.utils.SkillValueCalculator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

public class AreaDamageMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        if (!(target instanceof LivingEntity)) {
            return;
        }

        double amount;
        // --- INICIO DE LA CORRECCIÓN ---
        // Este será el ID de la habilidad de CONTEXTO (ej: "BolaDeFuego")
        String skillId = (String) params.get("skillId");
        InfinixMob plugin = InfinixMob.getPlugin();

        // Buscamos la configuración de la habilidad de contexto para obtener sus valores de daño.
        Optional<ConfigurationSection> skillConfigOpt = plugin.getSkillManager().getSkillConfig(skillId);
        if(!skillConfigOpt.isPresent()){
            plugin.getLogger().warning("AreaDamageMechanic no pudo encontrar la configuración para la skillId: " + skillId);
            return;
        }
        ConfigurationSection skillConfig = skillConfigOpt.get();
        // Obtenemos la sección de "damage" de la habilidad principal.
        Object amountObj = skillConfig.get("damage");
        // --- FIN DE LA CORRECCIÓN ---
        
        // 1. Calcular el daño base de la habilidad (con niveles)
        if (amountObj instanceof Map) {
            ConfigurationSection amountSection;
            if (amountObj instanceof ConfigurationSection) {
                amountSection = (ConfigurationSection) amountObj;
            } else {
                FileConfiguration tempConfig = new YamlConfiguration();
                amountSection = tempConfig.createSection("temp", (Map<?, ?>) amountObj);
            }

            int skillLevel = 1;
            if (caster instanceof Player && playerData != null && skillId != null) {
                skillLevel = playerData.getSkillLevel(skillId);
            }
            amount = SkillValueCalculator.calculate(amountSection, skillLevel);
        } else {
            amount = ((Number) params.getOrDefault("damage", params.getOrDefault("amount", 1.0))).doubleValue();
        }

        // 2. Sumar el bonificador de los amplificadores de habilidad de los ítems
        if (caster instanceof Player && skillId != null) {
            Player player = (Player) caster;
            double bonusDamage = 0;
            for (ItemStack item : player.getInventory().getArmorContents()) {
                bonusDamage += getBonusDamageFromItem(item, skillId);
            }
            bonusDamage += getBonusDamageFromItem(player.getInventory().getItemInMainHand(), skillId);
            bonusDamage += getBonusDamageFromItem(player.getInventory().getItemInOffHand(), skillId);
            amount += bonusDamage;
        }

        // 3. Marcar el daño como basado en habilidad y aplicarlo
        try {
            caster.setMetadata("infinix:skill_damage", new FixedMetadataValue(plugin, true));
            ((LivingEntity) target).damage(amount, caster);
        } finally {
            caster.removeMetadata("infinix:skill_damage", plugin);
        }
    }

    private double getBonusDamageFromItem(ItemStack item, String skillId) {
        if (item == null || item.getItemMeta() == null) {
            return 0;
        }
        String json = item.getItemMeta().getPersistentDataContainer().get(CustomItem.SKILL_MODIFIERS_KEY, PersistentDataType.STRING);
        if (json == null) {
            return 0;
        }
        Type type = new TypeToken<Map<String, Double>>(){}.getType();
        Map<String, Double> modifiers = InfinixMob.getPlugin().getGson().fromJson(json, type);
        return modifiers.getOrDefault(skillId.toLowerCase(), 0.0);
    }
}