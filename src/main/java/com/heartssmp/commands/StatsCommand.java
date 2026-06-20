package com.heartssmp.commands;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import com.heartssmp.gems.Gem;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StatsCommand implements CommandExecutor {
    private final HeartsSMPPlugin plugin;

    public StatsCommand(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        Player target;
        if (args.length > 0 && sender.hasPermission("heartssmp.admin")) {
            target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(plugin.prefix() + "§cPlayer not found: " + args[0]);
                return true;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage("Usage: /stats [player]");
            return true;
        }

        PlayerData data = plugin.getDataManager().get(target.getUniqueId());
        if (data == null) {
            sender.sendMessage(plugin.prefix() + "§cNo data found.");
            return true;
        }

        int baseKills = plugin.getConfig().getInt("skills.kills-per-skill", 250);
        int totalCombined = data.getTotalCombinedKills();
        int nextSkillAt = (data.getSkills().size() + 1) * baseKills;

        String gemInfo = "§7None";
        if (data.getGemId() != null) {
            Gem gem = plugin.getGemManager().getGem(data.getGemId());
            if (gem != null) {
                gemInfo = gem.getFormattedName() + " §8(Mastery " + data.getGemMastery() + "/3)";
            }
        }

        sender.sendMessage("§8§m-----------------------------------");
        sender.sendMessage("§e§l" + target.getName() + "'s Stats");
        sender.sendMessage("§c❤ Hearts: §f" + data.getHearts() + "/" + plugin.getConfig().getInt("hearts.maximum", 30));
        sender.sendMessage("§a♥ Lives: §f" + data.getLives() + "/" + plugin.getConfig().getInt("lives.maximum", 10));
        sender.sendMessage("§6⚔ Player Kills: §f" + data.getTotalKills());
        sender.sendMessage("§b☠ Mob Kills: §f" + data.getMobKills());
        sender.sendMessage("§7Total Kills: §f" + totalCombined + " §8(Next skill at " + nextSkillAt + " kills)");
        sender.sendMessage("§d✦ Gem: " + gemInfo);
        sender.sendMessage("§6⚔ Skills: §f" + data.getSkills().size() + " §8(use /skills to view)");
        if (data.isEliminated()) {
            sender.sendMessage("§4§l☠ ELIMINATED");
        }
        sender.sendMessage("§8§m-----------------------------------");
        return true;
    }
}
