package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import com.infinity3113.infinixmob.utils.SkillValueCalculator;
import com.google.gson.reflect.TypeToken;
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

public class AreaDamageMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        double damage;
        Object damageObj = params.get("damage");
        String skillId = (String) params.get("skillId");
        InfinixMob plugin = InfinixMob.getPlugin();

        // 1. Calcular el daño base de la habilidad (con niveles)
        if (damageObj instanceof Map) {
            ConfigurationSection damageSection;
            if (damageObj instanceof ConfigurationSection) {
                damageSection = (ConfigurationSection) damageObj;
            } else {
                FileConfiguration tempConfig = new YamlConfiguration();
                damageSection = tempConfig.createSection("temp", (Map<?, ?>) damageObj);
            }

            int skillLevel = 1;
            if (caster instanceof Player && playerData != null && skillId != null) {
                skillLevel = playerData.getSkillLevel(skillId);
            }
            damage = SkillValueCalculator.calculate(damageSection, skillLevel);
        } else {
            damage = ((Number) params.getOrDefault("damage", 5.0)).doubleValue();
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
            damage += bonusDamage;
        }

        double radius = ((Number) params.getOrDefault("radius", 5.0)).doubleValue();

        // 3. Marcar el daño como basado en habilidad y aplicarlo a las entidades en el radio
        final double finalDamage = damage; // Usar una variable final para la lambda
        try {
            caster.setMetadata("infinix:skill_damage", new FixedMetadataValue(plugin, true));
            target.getWorld().getNearbyEntities(target.getLocation(), radius, radius, radius).stream()
                .filter(e -> e instanceof LivingEntity && !e.equals(caster))
                .forEach(e -> {
                    LivingEntity victim = (LivingEntity) e;
                    victim.damage(finalDamage, caster);
                });
        } finally {
            // Asegurarse de que los metadatos se eliminen incluso si hay un error
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