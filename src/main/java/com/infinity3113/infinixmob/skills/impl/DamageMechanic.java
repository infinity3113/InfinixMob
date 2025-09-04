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
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Type;
import java.util.Map;

public class DamageMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        if (!(target instanceof LivingEntity)) {
            return;
        }

        double amount;
        Object amountObj = params.get("amount");
        String skillId = (String) params.get("skillId");

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
            amount = ((Number) params.getOrDefault("amount", 1.0)).doubleValue();
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

        // 3. Aplicar el daño final directamente
        ((LivingEntity) target).damage(amount, caster);
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