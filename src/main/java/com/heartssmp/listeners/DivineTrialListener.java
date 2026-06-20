package com.heartssmp.listeners;

import com.heartssmp.HeartsSMPPlugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

public class DivineTrialListener implements Listener {
    private final HeartsSMPPlugin plugin;

    public DivineTrialListener(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Material type = block.getType();

        // Only diamond blocks and torch/lantern types are relevant to the altar check —
        // skip the distance/stage check entirely otherwise for performance.
        if (type != Material.DIAMOND_BLOCK && type != Material.LANTERN
                && type != Material.SOUL_LANTERN && type != Material.TORCH) {
            return;
        }

        plugin.getDivineTrialManager().onPossibleAltarBlockPlace(event.getPlayer(), block.getLocation());
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Block block = event.getBlock();
        if (!(block.getState() instanceof Sign sign)) return;

        String[] lines = new String[4];
        for (int i = 0; i < 4; i++) {
            lines[i] = event.getLine(i) == null ? "" : event.getLine(i);
        }

        // Defer one tick so the sign's text is actually committed to the block state before we read it back
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTask(plugin, () ->
                plugin.getDivineTrialManager().onSignPlace(player, sign, lines));
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getIgnitingEntity() == null) return;
        if (!(event.getIgnitingEntity() instanceof Player player)) return;
        if (event.getBlock().getType() != Material.CAMPFIRE) return;

        com.heartssmp.quest.TrialTasks.onBrazierLit(plugin, plugin.getDivineTrialManager(), player, event.getBlock().getLocation());
    }
}
