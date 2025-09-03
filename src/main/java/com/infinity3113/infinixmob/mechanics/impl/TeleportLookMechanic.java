package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import com.infinity3113.infinixmob.utils.SkillValueCalculator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Map;
import org.bukkit.block.BlockFace;

public class TeleportLookMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        double maxDistance;
        if (params.get("max_distance") instanceof ConfigurationSection) {
            int skillLevel = 1;
            if (caster instanceof Player && playerData != null) {
                String skillId = (String) params.get("skillId");
                skillLevel = playerData.getSkillLevel(skillId);
            }
            maxDistance = SkillValueCalculator.calculate((ConfigurationSection) params.get("max_distance"), skillLevel);
        } else {
            maxDistance = ((Number) params.getOrDefault("max_distance", 4.0)).doubleValue();
        }

        if (caster != null) {
            Block targetBlock = caster.getTargetBlock(null, (int) Math.round(maxDistance));

            if (targetBlock != null && targetBlock.getType().isSolid()) {
                Location teleportLocation = targetBlock.getRelative(BlockFace.UP).getLocation().add(0.5, 0, 0.5);

                if (teleportLocation.getBlock().getType() == Material.AIR &&
                    teleportLocation.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {

                    teleportLocation.setDirection(caster.getLocation().getDirection());
                    caster.teleport(teleportLocation);
                }
            }
        }
    }
}