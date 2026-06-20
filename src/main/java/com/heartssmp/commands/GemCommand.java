package com.heartssmp.commands;

import com.heartssmp.HeartsSMPPlugin;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GemCommand implements CommandExecutor {
    private final HeartsSMPPlugin plugin;

    public GemCommand(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this.");
            return true;
        }
        player.sendMessage("§8§m-----------------------------------");
        player.sendMessage("§d§l✦ Your Gem");
        player.sendMessage(plugin.getGemManager().getGemInfo(player));
        player.sendMessage("§8§m-----------------------------------");
        return true;
    }
}
