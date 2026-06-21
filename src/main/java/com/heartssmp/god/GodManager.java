package com.heartssmp.god;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;

public class GodManager implements Listener {
    private final HeartsSMPPlugin plugin;
    private final Map<UUID, GodEntity> activeGods = new HashMap<>(); // summoner UUID -> GodEntity

    // Currently active guide god (during divine trial)
    private GodEntity guideGod = null;

    public GodManager(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void summonGod(Player summoner, Location loc, GodEntity.GodForm form) {
        // Remove existing god for this summoner
        despawnGod(summoner.getUniqueId());

        GodEntity god = new GodEntity(plugin, loc, form, summoner.getUniqueId());
        god.spawn();
        activeGods.put(summoner.getUniqueId(), god);
    }

    public void summonGuideGod(Location loc, UUID summoner) {
        if (guideGod != null) guideGod.despawn();
        guideGod = new GodEntity(plugin, loc, GodEntity.GodForm.GUIDE, summoner);
        guideGod.spawn();
    }

    public void despawnGuideGod() {
        if (guideGod != null) {
            guideGod.despawn();
            guideGod = null;
        }
    }

    public void despawnGod(UUID summonerUUID) {
        GodEntity existing = activeGods.remove(summonerUUID);
        if (existing != null) existing.despawn();
    }

    public boolean hasActiveGod(UUID summonerUUID) {
        GodEntity g = activeGods.get(summonerUUID);
        return g != null && g.isActive();
    }

    // Route chat to God
    @EventHandler(priority = EventPriority.NORMAL)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String msg = event.getMessage();

        // Check if any active god should respond
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            // Check summoner gods
            for (Map.Entry<UUID, GodEntity> entry : new HashMap<>(activeGods).entrySet()) {
                GodEntity god = entry.getValue();
                if (god.isActive()) {
                    god.handleChatRequest(player, msg);
                    return; // only one god responds
                }
            }
        });
    }

    // Admin summon - specific form
    public void adminSummonGod(Player target, Location loc, int powerPercent) {
        GodEntity.GodForm form = switch (powerPercent) {
            case 25 -> GodEntity.GodForm.POWER_25;
            case 50 -> GodEntity.GodForm.POWER_50;
            case 75 -> GodEntity.GodForm.POWER_75;
            default -> GodEntity.GodForm.GUIDE;
        };
        summonGod(target, loc, form);
    }

    public void despawnAll() {
        activeGods.values().forEach(GodEntity::despawn);
        activeGods.clear();
        if (guideGod != null) { guideGod.despawn(); guideGod = null; }
    }

    // Get God form based on how many times summoned
    public GodEntity.GodForm getNextGodForm(int summonsUsed) {
        return switch (summonsUsed) {
            case 0 -> GodEntity.GodForm.POWER_25;
            case 1 -> GodEntity.GodForm.POWER_50;
            case 2 -> GodEntity.GodForm.POWER_75;
            default -> GodEntity.GodForm.POWER_75;
        };
    }
}
