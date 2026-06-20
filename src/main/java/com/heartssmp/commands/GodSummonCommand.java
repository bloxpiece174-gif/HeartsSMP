package com.heartssmp.commands;

import com.heartssmp.HeartsSMPPlugin;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * /godsummon
 *   - no args: summons God at your own location
 *   - <x> <y> <z>: summons God at the given coordinates in your current world
 *
 * Restricted entirely by DivineTrialManager#summonGod: only the first-ever
 * trial completer, with Divine Grace at mastery 15, and only 3 times total.
 */
public class GodSummonCommand implements CommandExecutor {
    private final HeartsSMPPlugin plugin;

    public GodSummonCommand(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                              @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Location target;
        if (args.length >= 3) {
            try {
                double x = Double.parseDouble(args[0]);
                double y = Double.parseDouble(args[1]);
                double z = Double.parseDouble(args[2]);
                target = new Location(player.getWorld(), x, y, z);
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.prefix() + "§cUsage: /godsummon [x y z]");
                return true;
            }
        } else {
            target = player.getLocation();
        }

        plugin.getDivineTrialManager().summonGod(player, target);
        return true;
    }
}
