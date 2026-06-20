package com.heartssmp.listeners;

import com.heartssmp.HeartsSMPPlugin;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathListener implements Listener {
    private final HeartsSMPPlugin plugin;

    public EntityDeathListener(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) return; // handled by PlayerDeathListener

        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        plugin.getHeartManager().onKillMob(killer);
    }
}
