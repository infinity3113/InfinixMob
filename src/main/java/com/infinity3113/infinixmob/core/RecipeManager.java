package com.infinity3113.infinixmob.core;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Optional;

public class RecipeManager {

    private final InfinixMob plugin;

    public RecipeManager(InfinixMob plugin) {
        this.plugin = plugin;
    }

    public void registerCustomRecipes() {
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getLogger().info("Iniciando reemplazo de recetas vanilla...");

                // Materiales comunes
                RecipeChoice.MaterialChoice wood = new RecipeChoice.MaterialChoice(Material.OAK_PLANKS, Material.SPRUCE_PLANKS, Material.BIRCH_PLANKS, Material.JUNGLE_PLANKS, Material.ACACIA_PLANKS, Material.DARK_OAK_PLANKS, Material.MANGROVE_PLANKS, Material.CHERRY_PLANKS, Material.BAMBOO_PLANKS);
                RecipeChoice.MaterialChoice stone = new RecipeChoice.MaterialChoice(Material.COBBLESTONE, Material.BLACKSTONE, Material.COBBLED_DEEPSLATE);
                RecipeChoice.MaterialChoice stick = new RecipeChoice.MaterialChoice(Material.STICK);
                
                // Espadas
                replaceRecipe("wooden_sword", "ESPADAMADERA", new String[]{"P", "P", "S"}, wood, stick);
                replaceRecipe("stone_sword", "ESPADAPIEDRA", new String[]{"P", "P", "S"}, stone, stick);
                replaceRecipe("golden_sword", "ESPADAORO", new String[]{"P", "P", "S"}, new RecipeChoice.MaterialChoice(Material.GOLD_INGOT), stick);
                replaceRecipe("iron_sword", "ESPADAHIERRO", new String[]{"P", "P", "S"}, new RecipeChoice.MaterialChoice(Material.IRON_INGOT), stick);
                replaceRecipe("diamond_sword", "ESPADADIAMANTE", new String[]{"P", "P", "S"}, new RecipeChoice.MaterialChoice(Material.DIAMOND), stick);
                
                // Hachas
                replaceRecipe("wooden_axe", "HACHAMADERA", new String[]{"PP", "PS", " S"}, wood, stick);
                replaceRecipe("stone_axe", "HACHAPIEDRA", new String[]{"PP", "PS", " S"}, stone, stick);
                replaceRecipe("golden_axe", "HACHAORO", new String[]{"PP", "PS", " S"}, new RecipeChoice.MaterialChoice(Material.GOLD_INGOT), stick);
                replaceRecipe("iron_axe", "HACHAHIERRO", new String[]{"PP", "PS", " S"}, new RecipeChoice.MaterialChoice(Material.IRON_INGOT), stick);
                replaceRecipe("diamond_axe", "HACHADIAMANTE", new String[]{"PP", "PS", " S"}, new RecipeChoice.MaterialChoice(Material.DIAMOND), stick);

                // Armas y Herramientas Varias
                replaceRecipe("shield", "ESCUDO", new String[]{"W I", "WWW", " W "}, new RecipeChoice.MaterialChoice(Material.IRON_INGOT), wood);
                replaceRecipe("bow", "ARCO", new String[]{" SX", "S X", " SX"}, new RecipeChoice.MaterialChoice(Material.STICK), new RecipeChoice.MaterialChoice(Material.STRING));
                // Nota: El tridente no tiene receta de crafteo en vanilla, por lo que no se reemplaza. Se obtendrá por otros medios.
                
                // --- RECETAS DE ARMADURA AÑADIDAS ---
                replaceArmorRecipes("LEATHER", new RecipeChoice.MaterialChoice(Material.LEATHER));
                replaceArmorRecipes("CHAINMAIL", null); // No crafteable en vanilla
                replaceArmorRecipes("IRON", new RecipeChoice.MaterialChoice(Material.IRON_INGOT));
                replaceArmorRecipes("GOLDEN", new RecipeChoice.MaterialChoice(Material.GOLD_INGOT));
                replaceArmorRecipes("DIAMOND", new RecipeChoice.MaterialChoice(Material.DIAMOND));
                
                // Los ítems de Netherite son mejoras, no crafteos, por lo que no se reemplazan aquí.
                removeVanillaRecipe("netherite_sword");
                removeVanillaRecipe("netherite_axe");
                removeVanillaRecipe("netherite_helmet");
                removeVanillaRecipe("netherite_chestplate");
                removeVanillaRecipe("netherite_leggings");
                removeVanillaRecipe("netherite_boots");

                plugin.getLogger().info("Reemplazo de recetas vanilla completado.");
            }
        }.runTaskLater(plugin, 5L);
    }

    private void replaceArmorRecipes(String materialType, RecipeChoice.MaterialChoice material) {
        if (material == null) return; // Si no tiene material de crafteo, no se hace nada
        
        // Casco
        replaceRecipe(materialType.toLowerCase() + "_helmet", "CASCO" + materialType.toUpperCase(), new String[]{"MMM", "M M"}, material, null);
        // Pechera
        replaceRecipe(materialType.toLowerCase() + "_chestplate", "PECHERA" + materialType.toUpperCase(), new String[]{"M M", "MMM", "MMM"}, material, null);
        // Pantalones
        replaceRecipe(materialType.toLowerCase() + "_leggings", "PANTALON" + materialType.toUpperCase(), new String[]{"MMM", "M M", "M M"}, material, null);
        // Botas
        replaceRecipe(materialType.toLowerCase() + "_boots", "BOTAS" + materialType.toUpperCase(), new String[]{"M M", "M M"}, material, null);
    }

    private void removeVanillaRecipe(String itemName) {
        if (Bukkit.removeRecipe(NamespacedKey.minecraft(itemName))) {
            plugin.getLogger().info("Receta vanilla '" + itemName + "' eliminada.");
        }
    }
    
    private void replaceRecipe(String vanillaKey, String customItemId, String[] shape, RecipeChoice material1, RecipeChoice material2) {
        removeVanillaRecipe(vanillaKey);

        if (shape == null) return;

        Optional<CustomItem> customItemOpt = plugin.getItemManager().getItem(customItemId);
        if (!customItemOpt.isPresent()) {
            plugin.getLogger().warning("No se pudo crear la receta para '" + customItemId + "' porque el ítem no fue encontrado.");
            return;
        }

        ItemStack result = customItemOpt.get().buildItemStack();
        NamespacedKey key = new NamespacedKey(plugin, "infinix_" + customItemId.toLowerCase());

        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(shape);
        
        // Asigna los ingredientes según la forma. Las armaduras usan 'M', las herramientas 'P' y 'S'.
        if (Arrays.asList(shape).stream().anyMatch(s -> s.contains("M"))) {
             recipe.setIngredient('M', material1);
        } else {
             recipe.setIngredient('P', material1);
             if (material2 != null) recipe.setIngredient('S', material2);
        }
        
        // El escudo es un caso especial
        if(customItemId.equals("ESCUDO")) {
            recipe.setIngredient('W', material2); // wood
            recipe.setIngredient('I', material1); // iron
        }
        // El arco es otro caso especial
        if(customItemId.equals("ARCO")) {
            recipe.setIngredient('S', material1); // stick
            recipe.setIngredient('X', material2); // string
        }

        Bukkit.addRecipe(recipe);
    }
}