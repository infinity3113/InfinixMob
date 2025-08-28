package com.infinity3113.infinixmob.listeners;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.Optional;

public class SmithingListener implements Listener {

    private final InfinixMob plugin;

    // Un mapa para relacionar el ID del item de diamante con su versión de netherite
    private final Map<String, String> netheriteUpgrades = Map.of(
            "ESPADADIAMANTE", "ESPADANETHERITE",
            "HACHADIAMANTE", "HACHANETHERITE",
            "CASCODIAMANTE", "CASCONETHERITE",
            "PECHERADIAMANTE", "PECHERANETHERITE",
            "PANTALONDIAMANTE", "PANTALONNETHERITE",
            "BOTASDIAMANTE", "BOTASNETHERITE"
    );

    public SmithingListener(InfinixMob plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        ItemStack baseItem = event.getInventory().getItem(0);
        ItemStack mineralItem = event.getInventory().getItem(1);

        // Validar que se está usando un lingote de netherite
        if (baseItem == null || mineralItem == null || mineralItem.getType() != Material.NETHERITE_INGOT) {
            return;
        }

        ItemMeta meta = baseItem.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(CustomItem.CUSTOM_TAG_KEY, PersistentDataType.STRING)) {
            return;
        }

        // Obtener el ID del item de diamante
        String baseItemId = meta.getPersistentDataContainer().get(CustomItem.CUSTOM_TAG_KEY, PersistentDataType.STRING);
        if (baseItemId == null) {
            return;
        }

        // Buscar si este item tiene una mejora a netherite definida
        String netheriteItemId = netheriteUpgrades.get(baseItemId.toUpperCase());
        if (netheriteItemId != null) {
            // Obtener el item de netherite personalizado
            Optional<CustomItem> customNetheriteItemOpt = plugin.getItemManager().getItem(netheriteItemId);
            if (customNetheriteItemOpt.isPresent()) {
                // Establecer el resultado de la mejora
                event.setResult(customNetheriteItemOpt.get().buildItemStack());
            }
        }
    }
}