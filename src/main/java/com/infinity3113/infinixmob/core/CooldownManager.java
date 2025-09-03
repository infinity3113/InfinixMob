package com.infinity3113.infinixmob.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CooldownManager {

    // UUID del Jugador -> (ID de la Skill -> Tiempo en que termina el cooldown)
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public void setCooldown(UUID player, String skillId, int seconds) {
        if (seconds <= 0) return;
        long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(seconds);
        cooldowns.computeIfAbsent(player, k -> new HashMap<>()).put(skillId, endTime);
    }

    public long getRemainingCooldown(UUID player, String skillId) {
        Map<String, Long> playerCooldowns = cooldowns.get(player);
        if (playerCooldowns == null || !playerCooldowns.containsKey(skillId)) {
            return 0;
        }

        long endTime = playerCooldowns.get(skillId);
        if (System.currentTimeMillis() >= endTime) {
            playerCooldowns.remove(skillId); // Limpiar cooldown expirado
            return 0;
        }

        return endTime - System.currentTimeMillis();
    }

    public boolean isOnCooldown(UUID player, String skillId) {
        return getRemainingCooldown(player, skillId) > 0;
    }
}