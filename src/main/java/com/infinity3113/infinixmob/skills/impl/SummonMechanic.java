package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;

public class SummonMechanic implements Mechanic {
    private final InfinixMob plugin;
    public SummonMechanic(InfinixMob plugin) { this.plugin = plugin; }

    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        String mobId = (String) params.get("mobId");
        int amount = ((Number) params.getOrDefault("amount", 1)).intValue();

        Location spawnLocation = (target != null) ? target.getLocation() : caster.getLocation();

        for (int i = 0; i < amount; i++) {
            LivingEntity summoned = null;
            if (mobId != null) {
                summoned = plugin.getMobManager().spawnMob(mobId, spawnLocation);
            } else {
                try {
                    String entityTypeString = ((String) params.getOrDefault("type", "ZOMBIE")).trim().toUpperCase();
                    EntityType type = EntityType.valueOf(entityTypeString);
                    summoned = (LivingEntity) caster.getWorld().spawnEntity(spawnLocation, type);
                } catch (IllegalArgumentException | ClassCastException e) {
                    plugin.getLogger().warning("Tipo de entidad inválido en SummonMechanic: " + params.get("type"));
                }
            }

            if (summoned != null) {
                summoned.setMetadata("InfinixMob_Owner", new FixedMetadataValue(plugin, caster.getUniqueId().toString()));
                
                if (summoned instanceof Creature && target instanceof LivingEntity) {
                    ((Creature) summoned).setTarget((LivingEntity) target);
                }
            }
        }
    }
}