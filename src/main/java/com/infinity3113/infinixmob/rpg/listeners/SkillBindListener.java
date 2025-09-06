package com.infinity3113.infinixmob.rpg.listeners;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.rpg.managers.PlayerClassManager;
import com.infinity3113.infinixmob.rpg.managers.PlayerClassManager.PlayerData;
import com.infinity3113.infinixmob.rpg.managers.RpgSkillManager;
import com.infinity3113.infinixmob.rpg.util.SkillCaster;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
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

        String message = data.isInCastingMode() ?
                ChatColor.AQUA + "" + ChatColor.BOLD + "Modo Lanzamiento: ACTIVADO" :
                ChatColor.GRAY + "" + ChatColor.BOLD + "Modo Lanzamiento: DESACTIVADO";

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        PlayerData data = playerManager.getPlayerData(player);
        if (data == null || !data.isInCastingMode()) return;

        int slot = event.getNewSlot();

        if (slot > 3) return;

        event.setCancelled(true);

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