package com.infinity3113.infinixmob.rpg.util;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.rpg.managers.RpgSkillManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class SkillCaster {

    public static void executeSkill(Player player, String skillId, int skillLevel, InfinixMob plugin, RpgSkillManager skillManager) {
        Location playerLoc = player.getEyeLocation();
        World world = player.getWorld();
        Projectile projectile = null;

        switch (skillId.toLowerCase()) {
            case "fireball":
                projectile = player.launchProjectile(SmallFireball.class);
                world.playSound(playerLoc, Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
                break;
            case "ice_shard":
                projectile = player.launchProjectile(Snowball.class);
                world.playSound(playerLoc, Sound.ENTITY_PLAYER_HURT_FREEZE, 1.0f, 1.0f);
                break;
            case "lightning_bolt":
                Location targetLocation = player.getTargetBlock(null, 100).getLocation();
                world.strikeLightning(targetLocation);

                double damage = skillManager.getSkillStat(skillId, skillLevel, "damage");
                for (LivingEntity entity : world.getLivingEntities()) {
                    if (entity.getLocation().distance(targetLocation) <= 3 && !entity.equals(player)) {
                        entity.damage(damage, player);
                    }
                }
                break;
            case "arcane_missile":
                projectile = player.launchProjectile(SpectralArrow.class);
                if (projectile instanceof AbstractArrow) {
                    ((AbstractArrow) projectile).setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                }
                world.playSound(playerLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
                break;
        }

        if (projectile != null) {
            projectile.setMetadata("skillKey", new FixedMetadataValue(plugin, skillId));
            projectile.setMetadata("skillLevel", new FixedMetadataValue(plugin, skillLevel));
            projectile.setMetadata("ownerUUID", new FixedMetadataValue(plugin, player.getUniqueId().toString()));

            if (projectile instanceof AbstractArrow) {
                removeArrowAfterDelay((AbstractArrow) projectile, 100L, plugin);
            }
        }
    }

    private static void removeArrowAfterDelay(final AbstractArrow arrow, long ticks, InfinixMob plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (arrow != null && !arrow.isDead()) {
                    arrow.remove();
                }
            }
        }.runTaskLater(plugin, ticks);
    }
}