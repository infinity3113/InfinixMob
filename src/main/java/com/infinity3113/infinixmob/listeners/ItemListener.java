package com.infinity3113.infinixmob.listeners;

import com.google.gson.reflect.TypeToken;
import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class ItemListener implements Listener {

    private final InfinixMob plugin;
    private final String ARROW_ITEM_ID_KEY = "infinix_arrow_item_id";

    public ItemListener(InfinixMob plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;

        // Caso 1: Daño cuerpo a cuerpo de un jugador
        if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
            Player player = (Player) event.getDamager();
            ItemStack weapon = player.getInventory().getItemInMainHand();

            if (isCustomItem(weapon)) {
                if (weapon.getType() == Material.BOW || weapon.getType() == Material.CROSSBOW) {
                    event.setDamage(0.1); // Daño mínimo si golpean con el arco en la mano
                } else {
                    event.setDamage(calculateDamage(weapon.getItemMeta().getPersistentDataContainer(), (LivingEntity) event.getEntity()));
                }
            }
        }

        // Caso 2: Daño por una flecha disparada por un jugador
        if (event.getDamager() instanceof Arrow && event.getEntity() instanceof LivingEntity) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.hasMetadata(ARROW_ITEM_ID_KEY)) {
                String itemId = arrow.getMetadata(ARROW_ITEM_ID_KEY).get(0).asString();
                Optional<CustomItem> customItemOpt = plugin.getItemManager().getItem(itemId);

                if (customItemOpt.isPresent()) {
                    ItemStack bow = customItemOpt.get().buildItemStack(); // Construimos un item temporal para leer sus datos
                    event.setDamage(calculateDamage(bow.getItemMeta().getPersistentDataContainer(), (LivingEntity) event.getEntity()));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        ItemStack bow = event.getBow();
        if (isCustomItem(bow)) {
            String itemId = bow.getItemMeta().getPersistentDataContainer().get(CustomItem.CUSTOM_TAG_KEY, PersistentDataType.STRING);
            event.getProjectile().setMetadata(ARROW_ITEM_ID_KEY, new FixedMetadataValue(plugin, itemId));
        }
    }

    private boolean isCustomItem(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(CustomItem.CUSTOM_TAG_KEY, PersistentDataType.STRING);
    }
    
    private double calculateDamage(PersistentDataContainer container, LivingEntity victim) {
        double baseDamage = container.getOrDefault(CustomItem.DAMAGE_KEY, PersistentDataType.DOUBLE, 1.0);
        double critChance = container.getOrDefault(CustomItem.CRIT_CHANCE_KEY, PersistentDataType.DOUBLE, 0.0) / 100.0;
        double critDamageMultiplier = 1.0 + (container.getOrDefault(CustomItem.CRIT_DAMAGE_KEY, PersistentDataType.DOUBLE, 0.0) / 100.0);

        double finalDamage = baseDamage;
        if (ThreadLocalRandom.current().nextDouble() < critChance) {
            finalDamage *= critDamageMultiplier;
        }

        if (container.has(CustomItem.ELEMENTAL_DAMAGE_KEY, PersistentDataType.STRING)) {
            String elementalJson = container.get(CustomItem.ELEMENTAL_DAMAGE_KEY, PersistentDataType.STRING);
            Type type = new TypeToken<Map<String, Double>>(){}.getType();
            Map<String, Double> elementalDamages = plugin.getGson().fromJson(elementalJson, type);

            PersistentDataContainer victimContainer = victim.getPersistentDataContainer();
            Map<String, Double> weaknesses = new HashMap<>();
            if (victimContainer.has(CustomItem.WEAKNESSES_KEY, PersistentDataType.STRING)) {
                String weaknessesJson = victimContainer.get(CustomItem.WEAKNESSES_KEY, PersistentDataType.STRING);
                weaknesses = plugin.getGson().fromJson(weaknessesJson, type);
            }

            for (Map.Entry<String, Double> entry : elementalDamages.entrySet()) {
                String element = entry.getKey().toLowerCase();
                double damage = entry.getValue();
                double multiplier = weaknesses.getOrDefault(element, 1.0);
                finalDamage += (damage * multiplier);
            }
        }
        
        return finalDamage;
    }
}