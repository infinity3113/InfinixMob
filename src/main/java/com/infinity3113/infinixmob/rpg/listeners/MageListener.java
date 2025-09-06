package com.infinity3113.infinixmob.rpg.listeners;

import com.infinity3113.infinixmob.rpg.managers.ClassConfigManager;
import com.infinity3113.infinixmob.rpg.managers.PlayerClassManager;
import com.infinity3113.infinixmob.rpg.managers.PlayerClassManager.PlayerData;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class MageListener implements Listener {

    private final PlayerClassManager playerClassManager;
    private final ClassConfigManager classConfigManager;
    private final Plugin plugin;

    public MageListener(PlayerClassManager playerClassManager, ClassConfigManager classConfigManager, Plugin plugin) {
        this.playerClassManager = playerClassManager;
        this.classConfigManager = classConfigManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        PlayerData playerData = playerClassManager.getPlayerData(player);

        if (playerData == null || !playerData.getClassName().equalsIgnoreCase("mago")) {
            return;
        }

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand.getType() == Material.AIR) return;

            FileConfiguration mageConfig = classConfigManager.getClassConfig("mago");
            if (mageConfig == null) return;

            ConfigurationSection skillsSection = mageConfig.getConfigurationSection("skills");
            if (skillsSection == null) return;

            for (String skillKey : skillsSection.getKeys(false)) {
                String itemMaterialName = skillsSection.getString(skillKey + ".item");
                if (itemMaterialName != null && itemInHand.getType() == Material.matchMaterial(itemMaterialName)) {

                    double manaCost = skillsSection.getDouble(skillKey + ".mana_cost");
                    if (!playerData.hasEnoughMana(manaCost)) {
                        player.sendMessage(ChatColor.BLUE + "¡No tienes suficiente maná!");
                        return;
                    }

                    int cooldown = skillsSection.getInt(skillKey + ".cooldown");
                    if (playerClassManager.isOnCooldown(player, skillKey)) {
                        player.sendMessage(ChatColor.RED + "¡Esa habilidad aún se está recargando!");
                        return;
                    }

                    playerClassManager.setCooldown(player, skillKey, cooldown);
                    playerData.removeMana(manaCost);

                    executeSkill(player, skillKey);

                    String skillName = skillsSection.getString(skillKey + ".name", skillKey);
                    player.sendMessage(ChatColor.AQUA + "Has lanzado " + skillName + "!");

                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    private void executeSkill(Player player, String skillKey) {
        Location playerLoc = player.getEyeLocation();
        World world = player.getWorld();

        Projectile projectile = null;

        switch (skillKey.toLowerCase()) {
            case "fireball":
                projectile = player.launchProjectile(SmallFireball.class);
                world.playSound(playerLoc, Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
                break;
            case "ice_shard":
                projectile = player.launchProjectile(Snowball.class);
                world.playSound(playerLoc, Sound.ENTITY_PLAYER_HURT_FREEZE, 1.0f, 1.0f);
                break;
            case "lightning_bolt":
                world.strikeLightning(player.getTargetBlock(null, 100).getLocation());
                break;
            case "arcane_missile":
                projectile = player.launchProjectile(SpectralArrow.class);
                world.playSound(playerLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
                break;
        }

        if (projectile != null) {
            projectile.setMetadata("skillKey", new FixedMetadataValue(plugin, skillKey));
            projectile.setMetadata("ownerUUID", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        }
    }
}