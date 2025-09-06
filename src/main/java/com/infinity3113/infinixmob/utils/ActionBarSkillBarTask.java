package com.infinity3113.infinixmob.rpg.util;

import com.infinity3113.infinixmob.rpg.managers.PlayerClassManager;
import com.infinity3113.infinixmob.rpg.managers.RpgSkillManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBarSkillBarTask extends BukkitRunnable {

    private final Player player;
    private final PlayerClassManager playerManager;
    private final RpgSkillManager skillManager;

    public ActionBarSkillBarTask(Player player, PlayerClassManager playerManager, RpgSkillManager skillManager) {
        this.player = player;
        this.playerManager = playerManager;
        this.skillManager = skillManager;
    }

    @Override
    public void run() {
        if (!player.isOnline() || playerManager.getPlayerData(player) == null || !playerManager.getPlayerData(player).isInCastingMode()) {
            this.cancel();
            return;
        }

        StringBuilder skillBar = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            String skillId = playerManager.getPlayerData(player).getSkillBind(i);
            if (skillId != null) {
                boolean onCooldown = playerManager.isOnCooldown(player, skillId);
                ChatColor color = onCooldown ? ChatColor.RED : ChatColor.GREEN;
                String skillName = skillManager.getSkillConfig(skillId)
                        .map(config -> config.getString("display-name", skillId))
                        .orElse(skillId);
                
                // Usamos ChatColor.stripColor para el nombre de la habilidad
                skillBar.append(color).append("[").append(i + 1).append("] ").append(ChatColor.stripColor(skillName)).append(" ");
            }
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(skillBar.toString()));
    }
}