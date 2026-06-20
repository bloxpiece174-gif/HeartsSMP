package com.heartssmp.commands;

import com.heartssmp.HeartsSMPPlugin;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SacrificeCommand implements CommandExecutor {
    private final HeartsSMPPlugin plugin;

    public SacrificeCommand(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        plugin.getLivesManager().sacrifice(player);
        return true;
    }
}
