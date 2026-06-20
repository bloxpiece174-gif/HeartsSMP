package com.heartssmp.commands;

import com.heartssmp.HeartsSMPPlugin;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MasteryCommand implements CommandExecutor {
    private final HeartsSMPPlugin plugin;

    public MasteryCommand(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(plugin.prefix() + "§cUsage: /mastery <skill_id>");
            player.sendMessage("§7Use §e/skills §7to see your skill IDs.");
            return true;
        }
        String skillId = args[0].toLowerCase();
        boolean upgraded = plugin.getSkillManager().upgradeMastery(player, skillId);
        if (!upgraded) {
            player.sendMessage(plugin.prefix() + "§cCould not upgrade mastery. Check /skills for valid skill IDs.");
        }
        return true;
    }
}
