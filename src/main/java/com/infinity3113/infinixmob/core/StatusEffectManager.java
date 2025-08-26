package com.infinity3113.infinixmob.core;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestiona los efectos de estado personalizados.
 */
public class StatusEffectManager implements Listener {

    private final InfinixMob plugin;
    private final Map<String, ConfigurationSection> effectConfigs = new HashMap<>();
    private final Map<UUID, Map<String, ActiveEffect>> activeEffects = new ConcurrentHashMap<>();

    public StatusEffectManager(InfinixMob plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Carga las definiciones de los efectos de estado desde effects.yml.
     */
    public void loadStatusEffects() {
        effectConfigs.clear();
        File effectsFile = new File(plugin.getDataFolder(), "StatusEffects/effects.yml");
        if (!effectsFile.exists()) {
            plugin.saveResource("StatusEffects/effects.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(effectsFile);
        for (String key : config.getKeys(false)) {
            effectConfigs.put(key.toLowerCase(), config.getConfigurationSection(key));
        }
    }

    /**
     * Aplica un efecto de estado a una entidad.
     * @param target La entidad que recibe el efecto.
     * @param effectName El nombre del efecto a aplicar.
     * @param duration La duración en segundos.
     */
    public void applyEffect(LivingEntity target, String effectName, int duration) {
        String key = effectName.toLowerCase();
        if (!effectConfigs.containsKey(key)) return;

        ConfigurationSection config = effectConfigs.get(key);
        UUID targetId = target.getUniqueId();
        activeEffects.computeIfAbsent(targetId, k -> new ConcurrentHashMap<>());

        // Si el efecto ya está activo, simplemente reinicia su duración.
        if (activeEffects.get(targetId).containsKey(key)) {
            activeEffects.get(targetId).get(key).setEndTime(System.currentTimeMillis() + duration * 1000L);
            return;
        }

        ActiveEffect effect = new ActiveEffect(config, duration);
        activeEffects.get(targetId).put(key, effect);

        // Aplica modificadores de atributos si existen.
        if (config.getString("type", "").equalsIgnoreCase("ATTRIBUTE_MODIFIER")) {
            try {
                Attribute attribute = Attribute.valueOf(config.getString("options.attribute"));
                AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(config.getString("options.operation"));
                double amount = config.getDouble("options.amount");
                AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "infinix_" + key, amount, operation);
                target.getAttribute(attribute).addModifier(modifier);
                effect.setAttributeModifier(modifier);
            } catch (Exception e) {
                plugin.getLogger().warning("Error al aplicar modificador de atributo para el efecto: " + effectName);
            }
        }
    }

    /**
     * Inicia el ticker que procesa los efectos activos.
     */
    public void startEffectTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                for (UUID entityId : activeEffects.keySet()) {
                    LivingEntity entity = (LivingEntity) plugin.getServer().getEntity(entityId);
                    if (entity == null || entity.isDead()) {
                        activeEffects.remove(entityId);
                        continue;
                    }

                    activeEffects.get(entityId).entrySet().removeIf(entry -> {
                        ActiveEffect effect = entry.getValue();
                        if (now > effect.getEndTime()) {
                            // El efecto ha expirado, lo eliminamos.
                            if (effect.getAttributeModifier() != null) {
                                try {
                                    Attribute attribute = Attribute.valueOf(effect.getConfig().getString("options.attribute"));
                                    entity.getAttribute(attribute).removeModifier(effect.getAttributeModifier());
                                } catch (Exception ignored) {}
                            }
                            return true;
                        } else {
                            // El efecto sigue activo, procesamos su lógica.
                            processTick(entity, effect);
                            return false;
                        }
                    });
                }
            }
        }.runTaskTimer(plugin, 20L, 10L); // Se ejecuta cada medio segundo.
    }

    /**
     * Procesa la lógica de un tick para un efecto específico.
     * @param target La entidad con el efecto.
     * @param effect El efecto activo.
     */
    private void processTick(LivingEntity target, ActiveEffect effect) {
        ConfigurationSection config = effect.getConfig();
        String type = config.getString("type", "").toUpperCase();

        if (type.equals("REPEATING_DAMAGE")) {
            long period = config.getLong("options.period", 20L);
            if (System.currentTimeMillis() - effect.getLastTickTime() >= period * 50) { // Convertir ticks a ms
                double damage = config.getDouble("options.base-damage", 1.0);
                target.damage(damage);
                effect.setLastTickTime(System.currentTimeMillis());
            }
        }
    }

    // Evento para modificar la curación recibida.
    @EventHandler
    public void onEntityHeal(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        UUID id = event.getEntity().getUniqueId();
        if (activeEffects.containsKey(id)) {
            double modifier = 1.0;
            for (ActiveEffect effect : activeEffects.get(id).values()) {
                if (effect.getConfig().getString("type").equalsIgnoreCase("HEALING_MODIFIER")) {
                    modifier += effect.getConfig().getDouble("options.multiplier", 0.0);
                }
            }
            event.setAmount(event.getAmount() * modifier);
        }
    }
    
    /**
     * Limpia todos los efectos de una entidad.
     * @param entityId El UUID de la entidad.
     */
    public void clearEffects(UUID entityId) {
        activeEffects.remove(entityId);
    }


    /**
     * Clase interna para representar un efecto activo en una entidad.
     */
    private static class ActiveEffect {
        private final ConfigurationSection config;
        private long endTime;
        private long lastTickTime;
        private AttributeModifier attributeModifier;

        ActiveEffect(ConfigurationSection config, int duration) {
            this.config = config;
            this.endTime = System.currentTimeMillis() + duration * 1000L;
            this.lastTickTime = System.currentTimeMillis();
        }

        public ConfigurationSection getConfig() { return config; }
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        public long getLastTickTime() { return lastTickTime; }
        public void setLastTickTime(long lastTickTime) { this.lastTickTime = lastTickTime; }
        public AttributeModifier getAttributeModifier() { return attributeModifier; }
        public void setAttributeModifier(AttributeModifier attributeModifier) { this.attributeModifier = attributeModifier; }
    }
}