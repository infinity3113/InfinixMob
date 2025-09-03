package com.infinity3113.infinixmob.listeners;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class ClassListener implements Listener {

    private final InfinixMob plugin;

    public ClassListener(InfinixMob plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            PlayerData data = plugin.getPlayerClassManager().getPlayerData(player);

            if(data.getPlayerClass() != null) {
                 // Aquí puedes añadir una lógica más compleja para dar experiencia
                 // por ejemplo, leyendo un valor desde el YML del mob.
                double expToGive = 10.0; // Experiencia base
                 data.addExperience(expToGive);
                
                 if(data.canLevelUp()){
                     data.levelUp();
                     // Aquí podrías ejecutar comandos o mostrar efectos por subir de nivel.
                 }
            }
        }
    }
}