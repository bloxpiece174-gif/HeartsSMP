package com.heartssmp.listeners;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {
    private final HeartsSMPPlugin plugin;

    public PlayerDeathListener(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getPlayer();
        PlayerData data = plugin.getDataManager().get(victim.getUniqueId());
        if (data == null) return;

        // Handle killer getting hearts
        Player killer = victim.getKiller();
        if (killer != null && !killer.equals(victim)) {
            plugin.getHeartManager().onKillPlayer(killer);
        }

        // Handle victim losing lives
        plugin.getLivesManager().onPlayerDeath(victim);

        plugin.getDataManager().save(victim.getUniqueId());
    }
}
