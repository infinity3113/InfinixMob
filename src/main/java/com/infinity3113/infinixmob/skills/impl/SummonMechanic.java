package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SummonMechanic implements Mechanic {
    private final InfinixMob plugin;
    public SummonMechanic(InfinixMob plugin) { this.plugin = plugin; }

    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        String mobId = (String) params.get("mobId");
        int amount = ((Number) params.getOrDefault("amount", 1)).intValue();
        int radius = ((Number) params.getOrDefault("radius", 0)).intValue(); 
        Location center = (target != null) ? target.getLocation() : caster.getLocation();

        for (int i = 0; i < amount; i++) {
            Location spawnLocation = findSafeSpawnLocation(center, radius);
            if (spawnLocation == null) {
                spawnLocation = center;
            }

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
    
    private Location findSafeSpawnLocation(Location center, int radius) {
        if (radius <= 0) {
            return center.clone().add(0,1,0);
        }
        for (int i = 0; i < 10; i++) { 
            int x = center.getBlockX() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            int z = center.getBlockZ() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            Block highestBlock = center.getWorld().getHighestBlockAt(x, z);
            
            if (highestBlock.getType().isSolid() && highestBlock.getType() != Material.LAVA) {
                 Location potentialSpawn = highestBlock.getLocation().add(0.5, 1, 0.5);
                 if (potentialSpawn.getBlock().getType() == Material.AIR && potentialSpawn.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {
                     return potentialSpawn;
                 }
            }
        }
        return null;
    }
}