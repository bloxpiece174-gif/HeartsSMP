package com.heartssmp.listeners;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

public class PlayerJoinListener implements Listener {
    private final HeartsSMPPlugin plugin;

    public PlayerJoinListener(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getDataManager().getOrCreate(player.getUniqueId(), player.getName());
        data.setPlayerName(player.getName());

        // Apply correct max health
        plugin.getHeartManager().applyMaxHealth(player, data);

        // Assign starter gem if first join
        if (data.getGemId() == null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                plugin.getGemManager().assignStarterGem(player), 40L);
        }

        // Warn if eliminated somehow rejoined (shouldn't happen but safety)
        if (data.isEliminated()) {
            player.sendMessage(plugin.prefix() + "§cYou are marked as eliminated. Contact an admin.");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getDataManager().unload(event.getPlayer().getUniqueId());
    }
}
