package com.infinity3113.infinixmob.mobs;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CustomMob {

    private final InfinixMob plugin;
    private final String internalName;
    private final ConfigurationSection config;

    public CustomMob(InfinixMob plugin, String internalName, ConfigurationSection config) {
        this.plugin = plugin;
        this.internalName = internalName;
        this.config = config;
    }

    public void applyToEntity(LivingEntity entity) {
        if (config.contains("display-name")) {
            entity.setCustomName(ChatColor.translateAlternateColorCodes('&', config.getString("display-name")));
            entity.setCustomNameVisible(true);
        }

        if (config.contains("health")) {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(config.getDouble("health", 20.0));
            entity.setHealth(config.getDouble("health", 20.0));
        }

        if (config.contains("damage")) {
            entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(config.getDouble("damage"));
        }

        if (config.isConfigurationSection("equipment")) {
            EntityEquipment equipment = entity.getEquipment();
            if (equipment != null) {
                ConfigurationSection equipConfig = config.getConfigurationSection("equipment");
                getItemStackFromString(equipConfig.getString("main-hand")).ifPresent(equipment::setItemInMainHand);
                getItemStackFromString(equipConfig.getString("off-hand")).ifPresent(equipment::setItemInOffHand);
                getItemStackFromString(equipConfig.getString("helmet")).ifPresent(equipment::setHelmet);
                getItemStackFromString(equipConfig.getString("chestplate")).ifPresent(equipment::setChestplate);
                getItemStackFromString(equipConfig.getString("leggings")).ifPresent(equipment::setLeggings);
                getItemStackFromString(equipConfig.getString("boots")).ifPresent(equipment::setBoots);
            }
        }

        if (config.isConfigurationSection("Options")) {
            ConfigurationSection options = config.getConfigurationSection("Options");

            if (options.contains("MovementSpeed")) {
                entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(options.getDouble("MovementSpeed"));
            }
            if (options.contains("KnockbackResistance")) {
                entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(options.getDouble("KnockbackResistance"));
            }
            if (options.contains("FollowRange")) {
                entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(options.getDouble("FollowRange"));
            }

            entity.setGlowing(options.getBoolean("Glowing", false));
            entity.setSilent(options.getBoolean("Silent", false));
            entity.setCanPickupItems(!options.getBoolean("PreventItemPickup", false));
            entity.setRemoveWhenFarAway(options.getBoolean("Despawn", true));

            if (options.getBoolean("LavaImmunity", false)) {
                entity.setMetadata("InfinixMob_LavaImmunity", new FixedMetadataValue(plugin, true));
            }

            if (options.getBoolean("PreventOtherDrops", false)) {
                entity.setMetadata("InfinixMob_PreventDrops", new FixedMetadataValue(plugin, true));
            }

            if (entity instanceof ArmorStand) {
                ArmorStand as = (ArmorStand) entity;
                as.setVisible(!options.getBoolean("Invisible", false));
                as.setGravity(!options.getBoolean("NoGravity", false));
                as.setMarker(options.getBoolean("IsMarker", false));
            }
        }

        if (config.isConfigurationSection("weaknesses")) {
            Map<String, Double> weaknesses = new HashMap<>();
            ConfigurationSection weaknessesSection = config.getConfigurationSection("weaknesses");
            for (String element : weaknessesSection.getKeys(false)) {
                weaknesses.put(element.toLowerCase(), weaknessesSection.getDouble(element));
            }
            String weaknessesJson = plugin.getGson().toJson(weaknesses);
            entity.getPersistentDataContainer().set(CustomItem.WEAKNESSES_KEY, PersistentDataType.STRING, weaknessesJson);
        }

        entity.setMetadata("InfinixMobID", new FixedMetadataValue(plugin, internalName));
        if (config.contains("faction")) {
            entity.setMetadata("InfinixMob_Faction", new FixedMetadataValue(plugin, config.getString("faction")));
        }
    }

    private Optional<ItemStack> getItemStackFromString(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return Optional.empty();
        }
        return plugin.getItemManager().getItem(identifier)
                .map(customItem -> customItem.buildItemStack())
                .or(() -> {
                    Material material = Material.matchMaterial(identifier.toUpperCase());
                    return material != null ? Optional.of(new ItemStack(material)) : Optional.empty();
                });
    }

    public String getInternalName() { return internalName; }
    public ConfigurationSection getConfig() { return config; }
}