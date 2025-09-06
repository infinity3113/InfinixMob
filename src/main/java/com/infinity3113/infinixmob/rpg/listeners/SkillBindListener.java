package com.infinity3113.infinixmob.rpg.listeners;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.rpg.managers.PlayerClassManager;
import com.infinity3113.infinixmob.rpg.managers.PlayerClassManager.PlayerData;
import com.infinity3113.infinixmob.rpg.managers.RpgSkillManager;
import com.infinity3113.infinixmob.rpg.util.SkillCaster;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class SkillBindListener implements Listener {

    private final InfinixMob plugin;
    private final PlayerClassManager playerManager;
    private final RpgSkillManager skillManager;

    public SkillBindListener(InfinixMob plugin, PlayerClassManager playerManager, RpgSkillManager skillManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        PlayerData data = playerManager.getPlayerData(player);
        if (data == null) return;

        event.setCancelled(true);
        data.setCastingMode(!data.isInCastingMode());

        if (data.isInCastingMode()) {
            playerManager.startSkillBarTask(player);
        } else {
            playerManager.stopSkillBarTask(player);
        }
    }

    @EventHandler
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;

        Player player = event.getPlayer();
        PlayerData data = playerManager.getPlayerData(player);
        if (data == null || !data.isInCastingMode()) return;

        int slot = player.getInventory().getHeldItemSlot();
        if (slot > 3) return;

        String skillId = data.getSkillBind(slot);
        if (skillId == null) return;

        int skillLevel = data.getSkillLevel(skillId);
        if (skillLevel <= 0) return;

        if (playerManager.isOnCooldown(player, skillId)) {
            player.sendMessage(ChatColor.RED + "Esa habilidad se está recargando.");
            return;
        }

        double manaCost = skillManager.getSkillStat(skillId, skillLevel, "mana_cost");
        if (!data.hasEnoughMana(manaCost)) {
            player.sendMessage(ChatColor.BLUE + "No tienes suficiente maná.");
            return;
        }

        data.removeMana(manaCost);
        double cooldown = skillManager.getSkillStat(skillId, skillLevel, "cooldown");
        playerManager.setCooldown(player, skillId, (int) cooldown);

        SkillCaster.executeSkill(player, skillId, skillLevel, plugin, skillManager);
    }
}