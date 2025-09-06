package com.infinity3113.infinixmob.rpg.listeners;

import com.infinity3113.infinixmob.rpg.managers.PlayerClassManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinQuitListener implements Listener {

    private final PlayerClassManager playerClassManager;

    public PlayerJoinQuitListener(PlayerClassManager playerClassManager) {
        this.playerClassManager = playerClassManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerClassManager.loadPlayerData(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerClassManager.savePlayerData(player);
        playerClassManager.removePlayerData(player);
    }
}