package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;

public class VampirismMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        if (target instanceof LivingEntity) {
            double damage = ((Number) params.getOrDefault("damage", 5.0)).doubleValue();
            double healRatio = ((Number) params.getOrDefault("heal_ratio", 0.5)).doubleValue();

            InfinixMob plugin = InfinixMob.getPlugin();
            try {
                caster.setMetadata("infinix:skill_damage", new FixedMetadataValue(plugin, true));
                ((LivingEntity) target).damage(damage, caster);
            } finally {
                caster.removeMetadata("infinix:skill_damage", plugin);
            }
            
            double healthToHeal = damage * healRatio;
            double maxHealth = caster.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            caster.setHealth(Math.min(maxHealth, caster.getHealth() + healthToHeal));
        }
    }
}