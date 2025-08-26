package com.infinity3113.infinixmob.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PlaceholderParser {

    private static final Pattern RANDOM_NUMBER_PATTERN = Pattern.compile("<math\\.random_(-?\\d+)_(-?\\d+)>");

    public static String parse(String text, LivingEntity caster, Entity target) {
        if (text == null) return "";

        // --- Placeholders existentes ---
        if (caster != null) {
            text = text.replace("<caster.name>", caster.getName());
            text = text.replace("<caster.uuid>", caster.getUniqueId().toString());
            text = text.replace("<caster.health>", String.format("%.0f", caster.getHealth()));
            text = text.replace("<caster.max_health>", String.format("%.0f", caster.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()));
            text = text.replace("<caster.world>", caster.getWorld().getName());
            text = text.replace("<caster.x>", String.format("%.1f", caster.getLocation().getX()));
            text = text.replace("<caster.y>", String.format("%.1f", caster.getLocation().getY()));
            text = text.replace("<caster.z>", String.format("%.1f", caster.getLocation().getZ()));
            text = text.replace("<caster.biome>", caster.getLocation().getBlock().getBiome().name());
        }

        if (target != null) {
            text = text.replace("<target.name>", target.getName());
            text = text.replace("<target.uuid>", target.getUniqueId().toString());
            text = text.replace("<target.world>", target.getWorld().getName());
            text = text.replace("<target.x>", String.format("%.1f", target.getLocation().getX()));
            text = text.replace("<target.y>", String.format("%.1f", target.getLocation().getY()));
            text = text.replace("<target.z>", String.format("%.1f", target.getLocation().getZ()));
            text = text.replace("<target.type>", target.getType().name());
            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;
                text = text.replace("<target.health>", String.format("%.0f", livingTarget.getHealth()));
                text = text.replace("<target.max_health>", String.format("%.0f", livingTarget.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()));
            }
        }

        // --- Nuevos Placeholders ---

        if (caster != null) {
            if (caster.hasMetadata("InfinixMob_Faction")) {
                text = text.replace("<caster.faction>", caster.getMetadata("InfinixMob_Faction").get(0).asString());
            }
            if (caster instanceof org.bukkit.entity.Creature) {
                LivingEntity casterTarget = ((org.bukkit.entity.Creature) caster).getTarget();
                if (casterTarget != null) {
                    text = text.replace("<caster.target.name>", casterTarget.getName());
                }
            }
            text = text.replace("<caster.location>", String.format("%d, %d, %d", caster.getLocation().getBlockX(), caster.getLocation().getBlockY(), caster.getLocation().getBlockZ()));
            text = text.replace("<caster.time_alive>", String.valueOf(caster.getTicksLived() / 20));
        }

        if (target != null) {
            text = text.replace("<target.location>", String.format("%d, %d, %d", target.getLocation().getBlockX(), target.getLocation().getBlockY(), target.getLocation().getBlockZ()));
            if (caster != null) {
                 text = text.replace("<target.distance>", String.format("%.1f", caster.getLocation().distance(target.getLocation())));
            }
            if (target instanceof Player) {
                Player pTarget = (Player) target;
                text = text.replace("<target.gamemode>", pTarget.getGameMode().toString());
                text = text.replace("<target.ping>", String.valueOf(pTarget.getPing()));
                text = text.replace("<item.in_hand.name>", pTarget.getInventory().getItemInMainHand().getType().toString());
            }
        }
        
        // Placeholders del servidor
        text = text.replace("<server.online_players>", String.valueOf(Bukkit.getOnlinePlayers().size()));
        text = text.replace("<server.difficulty>", Bukkit.getWorlds().get(0).getDifficulty().toString());
        // El placeholder de TPS se ha eliminado para asegurar la compatibilidad
        text = text.replace("<server.motd>", Bukkit.getMotd());
        text = text.replace("<server.player_list>", Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.joining(", ")));

        // Placeholders del mundo
        if(caster != null) {
            long time = caster.getWorld().getTime();
            long hours = (time / 1000 + 6) % 24;
            long minutes = (time % 1000) * 60 / 1000;
            text = text.replace("<server.time_24h>", String.format("%02d:%02d", hours, minutes));

            String weather = caster.getWorld().hasStorm() ? (caster.getWorld().isThundering() ? "THUNDER" : "RAIN") : "CLEAR";
            text = text.replace("<world.weather>", weather);
            text = text.replace("<world.name>", caster.getWorld().getName());

            Player nearestPlayer = caster.getWorld().getPlayers().stream()
                    .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(caster.getLocation())))
                    .orElse(null);
            if(nearestPlayer != null){
                text = text.replace("<nearest_player.name>", nearestPlayer.getName());
                text = text.replace("<nearest_player.distance>", String.format("%.1f", caster.getLocation().distance(nearestPlayer.getLocation())));
            }
        }
        
        // Placeholder de número aleatorio
        Matcher matcher = RANDOM_NUMBER_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            int min = Integer.parseInt(matcher.group(1));
            int max = Integer.parseInt(matcher.group(2));
            int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
            matcher.appendReplacement(sb, String.valueOf(randomNum));
        }
        matcher.appendTail(sb);
        text = sb.toString();

        return ChatColor.translateAlternateColorCodes('&', text);
    }
}