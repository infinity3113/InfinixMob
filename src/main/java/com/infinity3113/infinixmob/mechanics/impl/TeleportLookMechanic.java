package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import java.util.Map;
import java.util.Set;
import org.bukkit.block.BlockFace;


public class TeleportLookMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        int maxDistance = ((Number) params.getOrDefault("max_distance", 4)).intValue();
        
        // El caster es el que se teletransporta
        if (caster != null) {
            // Usamos getTargetBlockExact para ser más precisos
            Block targetBlock = caster.getTargetBlock(null, maxDistance);

            if (targetBlock != null && targetBlock.getType().isSolid()) {
                // El punto seguro está un bloque ENCIMA del bloque al que miras
                Location teleportLocation = targetBlock.getRelative(BlockFace.UP).getLocation().add(0.5, 0, 0.5);

                // CORRECCIÓN: Usamos isAir() en lugar del método incorrecto isPassable()
                // Nos aseguramos de que haya dos bloques de aire para que el jugador quepa.
                if (teleportLocation.getBlock().getType() == Material.AIR &&
                    teleportLocation.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {

                    // Mantenemos la dirección en la que miraba el jugador
                    teleportLocation.setDirection(caster.getLocation().getDirection());
                    caster.teleport(teleportLocation);
                }
            }
        }
    }
}