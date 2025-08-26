package com.infinity3113.infinixmob.core;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestiona bloques temporales como trampas y estructuras.
 */
public class BlockManager implements Listener {

    private final InfinixMob plugin;
    private final Map<Location, TemporaryBlock> tempBlocks = new ConcurrentHashMap<>();

    public BlockManager(InfinixMob plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Coloca una trampa en una ubicación.
     * @param location La ubicación de la trampa.
     * @param skillOnTrigger La skill que se activa al pisar la trampa.
     * @param duration La duración de la trampa en segundos.
     */
    public void placeTrap(Location location, String skillOnTrigger, int duration) {
        // La trampa es "conceptual", no se coloca un bloque físico visible.
        // Se almacena la ubicación y la skill asociada.
        Trap trap = new Trap(location, skillOnTrigger, duration);
        tempBlocks.put(location, trap);

        // Tarea para eliminar la trampa después de su duración.
        new BukkitRunnable() {
            @Override
            public void run() {
                tempBlocks.remove(location);
            }
        }.runTaskLater(plugin, duration * 20L);
    }
    
    /**
     * Construye una estructura de bloques.
     * @param origin La ubicación de origen de la estructura.
     * @param structureData La lista de bloques a colocar.
     * @param duration La duración de la estructura en segundos (-1 para permanente).
     */
    public void buildStructure(Location origin, java.util.List<String> structureData, int duration) {
        for (String blockInfo : structureData) {
            String[] parts = blockInfo.split(",");
            if (parts.length == 4) {
                try {
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    int z = Integer.parseInt(parts[2]);
                    Material material = Material.valueOf(parts[3].toUpperCase());
                    
                    Block block = origin.clone().add(x, y, z).getBlock();
                    BlockState originalState = block.getState();
                    block.setType(material);

                    if (duration > 0) {
                        StructureBlock sb = new StructureBlock(block.getLocation(), duration, originalState);
                        tempBlocks.put(block.getLocation(), sb);
                        // Tarea para revertir el bloque.
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (tempBlocks.remove(block.getLocation()) != null) {
                                    originalState.update(true, false);
                                }
                            }
                        }.runTaskLater(plugin, duration * 20L);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error al parsear datos de estructura: " + blockInfo);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Optimización: solo comprobar si el jugador se ha movido a un nuevo bloque.
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        Location loc = player.getLocation().getBlock().getLocation();

        // Comprueba si el jugador ha pisado una trampa.
        if (tempBlocks.containsKey(loc) && tempBlocks.get(loc) instanceof Trap) {
            Trap trap = (Trap) tempBlocks.remove(loc); // La trampa se activa una sola vez.
            plugin.getSkillManager().executeSkill(trap.getSkillOnTrigger(), null, player);
        }
    }

    // --- Clases internas para gestionar los tipos de bloques temporales ---

    private interface TemporaryBlock {}

    private static class Trap implements TemporaryBlock {
        private final String skillOnTrigger;
        public Trap(Location loc, String skill, int duration) { this.skillOnTrigger = skill; }
        public String getSkillOnTrigger() { return skillOnTrigger; }
    }

    private static class StructureBlock implements TemporaryBlock {
        private final BlockState originalState;
        public StructureBlock(Location loc, int duration, BlockState state) { this.originalState = state; }
    }
}