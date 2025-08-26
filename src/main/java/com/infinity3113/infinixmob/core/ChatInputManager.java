package com.infinity3113.infinixmob.core;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatInputManager {
    private final Map<UUID, Consumer<String>> inputHandlers = new HashMap<>();

    public void requestInput(Player player, Consumer<String> handler) {
        inputHandlers.put(player.getUniqueId(), handler);
    }

    public boolean handleInput(Player player, String message) {
        Consumer<String> handler = inputHandlers.remove(player.getUniqueId());
        if (handler != null) {
            handler.accept(message);
            return true;
        }
        return false;
    }
}