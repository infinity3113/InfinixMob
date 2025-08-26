package com.infinity3113.infinixmob.core;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestiona la mecánica de Soul Link (Vínculo de Alma).
 */
public class SoulLinkManager implements Listener {

    private final InfinixMob plugin;
    private final Map<UUID, UUID> linkedPlayers = new ConcurrentHashMap<>(); // Jugador -> Grupo
    private final Map<UUID, Long> groupExpiry = new ConcurrentHashMap<>(); // Grupo -> Tiempo de expiración

    public SoulLinkManager(InfinixMob plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startLinkVisualizer();
    }

    /**
     * Vincula a un grupo de jugadores por una duración determinada.
     * @param targets La lista de jugadores a vincular.
     * @param duration La duración del vínculo en segundos.
     */
    public void linkPlayers(List<LivingEntity> targets, int duration) {
        UUID groupId = UUID.randomUUID();
        long expiryTime = System.currentTimeMillis() + duration * 1000L;

        for (LivingEntity target : targets) {
            if (target instanceof Player) {
                linkedPlayers.put(target.getUniqueId(), groupId);
            }
        }
        groupExpiry.put(groupId, expiryTime);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player damagedPlayer = (Player) event.getEntity();
        UUID groupId = linkedPlayers.get(damagedPlayer.getUniqueId());

        if (groupId != null) {
            // Comprueba si el vínculo ha expirado.
            if (System.currentTimeMillis() > groupExpiry.getOrDefault(groupId, 0L)) {
                linkedPlayers.remove(damagedPlayer.getUniqueId());
                return;
            }

            // Reparte el daño entre todos los miembros del grupo.
            List<Player> groupMembers = new ArrayList<>();
            for (Map.Entry<UUID, UUID> entry : linkedPlayers.entrySet()) {
                if (entry.getValue().equals(groupId)) {
                    Player p = plugin.getServer().getPlayer(entry.getKey());
                    if (p != null && p.isOnline() && !p.isDead()) {
                        groupMembers.add(p);
                    }
                }
            }

            if (groupMembers.size() > 1) {
                double sharedDamage = event.getDamage() / groupMembers.size();
                event.setDamage(sharedDamage);

                for (Player member : groupMembers) {
                    if (!member.equals(damagedPlayer)) {
                        // Aplica el daño a los otros miembros, evitando un bucle infinito.
                        member.damage(sharedDamage);
                    }
                }
            }
        }
    }

    /**
     * Inicia una tarea que crea partículas entre los jugadores vinculados.
     */
    private void startLinkVisualizer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Limpia grupos expirados
                long now = System.currentTimeMillis();
                groupExpiry.entrySet().removeIf(entry -> now > entry.getValue());
                linkedPlayers.entrySet().removeIf(entry -> !groupExpiry.containsKey(entry.getValue()));

                // Dibuja las partículas
                for (UUID groupId : groupExpiry.keySet()) {
                    List<Player> members = new ArrayList<>();
                    for (Map.Entry<UUID, UUID> entry : linkedPlayers.entrySet()) {
                        if (entry.getValue().equals(groupId)) {
                            Player p = plugin.getServer().getPlayer(entry.getKey());
                            if (p != null) members.add(p);
                        }
                    }

                    if (members.size() > 1) {
                        for (int i = 0; i < members.size(); i++) {
                            for (int j = i + 1; j < members.size(); j++) {
                                drawParticleLine(members.get(i), members.get(j));
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L); // Cada medio segundo
    }

    private void drawParticleLine(Player p1, Player p2) {
        // Dibuja una línea de partículas entre dos jugadores.
    }
}
