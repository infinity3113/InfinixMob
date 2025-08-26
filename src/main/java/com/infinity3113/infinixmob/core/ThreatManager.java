package com.infinity3113.infinixmob.core;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestiona el sistema de amenaza (aggro) para los mobs.
 * La amenaza determina a qué jugador atacará un mob.
 */
public class ThreatManager {

    // Mapa principal que asocia el UUID de un mob con su tabla de amenaza.
    // La tabla de amenaza interna asocia el UUID de un jugador con su nivel de amenaza.
    private final Map<UUID, Map<UUID, Double>> threatTables = new ConcurrentHashMap<>();

    /**
     * Añade una cantidad específica de amenaza de un jugador hacia un mob.
     * @param mob El mob que recibe la amenaza.
     * @param player El jugador que genera la amenaza.
     * @param amount La cantidad de amenaza a añadir.
     */
    public void addThreat(LivingEntity mob, Player player, double amount) {
        threatTables.computeIfAbsent(mob.getUniqueId(), k -> new ConcurrentHashMap<>())
                    .merge(player.getUniqueId(), amount, Double::sum);
    }

    /**
     * Obtiene el jugador con el nivel de amenaza más alto para un mob específico.
     * @param mob El mob para el cual se busca el objetivo.
     * @return El jugador con la mayor amenaza, o null si no hay ninguno.
     */
    public Player getHighestThreatTarget(LivingEntity mob) {
        Map<UUID, Double> mobThreats = threatTables.get(mob.getUniqueId());
        if (mobThreats == null || mobThreats.isEmpty()) {
            return null;
        }

        // Encuentra la entrada (jugador) con el valor de amenaza más alto.
        return mobThreats.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(entry -> mob.getServer().getPlayer(entry.getKey()))
                .filter(player -> player != null && player.isOnline() && !player.isDead())
                .orElse(null);
    }

    /**
     * Limpia toda la tabla de amenaza para un mob específico (por ejemplo, al morir).
     * @param mobUuid El UUID del mob cuya amenaza se limpiará.
     */
    public void clearThreat(UUID mobUuid) {
        threatTables.remove(mobUuid);
    }
}
