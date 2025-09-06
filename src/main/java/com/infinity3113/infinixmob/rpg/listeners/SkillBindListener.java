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
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkillBindListener implements Listener {

    private final InfinixMob plugin;
    private final PlayerClassManager playerManager;
    private final RpgSkillManager skillManager;

    // Mapa para gestionar la tarea de casteo de cada jugador
    private final Map<UUID, BukkitTask> castingTasks = new HashMap<>();

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
        boolean newCastingMode = !data.isInCastingMode();
        data.setCastingMode(newCastingMode);

        if (newCastingMode) {
            playerManager.startSkillBarTask(player);
            startCastingTask(player); // Inicia nuestra nueva tarea de monitoreo
        } else {
            playerManager.stopSkillBarTask(player);
            stopCastingTask(player); // Detiene la tarea de monitoreo
        }
    }

    private void startCastingTask(Player player) {
        // Nos aseguramos de que no haya tareas duplicadas
        stopCastingTask(player);

        BukkitTask task = new BukkitRunnable() {
            int previousSlot = player.getInventory().getHeldItemSlot();

            @Override
            public void run() {
                PlayerData data = playerManager.getPlayerData(player);
                // Si el jugador se desconecta o sale del modo casteo, la tarea se cancela
                if (!player.isOnline() || data == null || !data.isInCastingMode()) {
                    this.cancel();
                    castingTasks.remove(player.getUniqueId());
                    return;
                }

                int currentSlot = player.getInventory().getHeldItemSlot();

                // Detectamos si el jugador ha cambiado de slot (ha presionado una tecla numérica)
                if (currentSlot != previousSlot) {
                    // Solo nos interesan los slots 1-4 (índices 0-3)
                    if (currentSlot <= 3) {
                        castSkillFromSlot(player, data, currentSlot);
                    }
                    // Inmediatamente después de detectar el cambio, forzamos al jugador a volver al slot anterior
                    player.getInventory().setHeldItemSlot(previousSlot);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Se ejecuta cada tick para una respuesta inmediata

        castingTasks.put(player.getUniqueId(), task);
    }

    private void stopCastingTask(Player player) {
        BukkitTask task = castingTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    private void castSkillFromSlot(Player player, PlayerData data, int slot) {
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