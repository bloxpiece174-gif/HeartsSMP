package com.heartssmp.commands;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import com.heartssmp.god.GodEntity;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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

        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return true;

        // Check eligibility
        if (data.getGodSummonsRemaining() <= 0) {
            player.sendMessage(plugin.prefix() + "§cYou have no God Summon charges remaining!");
            player.sendMessage(plugin.prefix() + "§7Complete the Divine Trial as the first ever player to receive charges.");
            return true;
        }

        // Check Divine Grace mastery 15
        if (!data.hasSkill("graceful_enlightenment") || data.getSkillMastery("graceful_enlightenment") < 15) {
            player.sendMessage(plugin.prefix() + "§cYou need §eGraceful Enlightenment §cat mastery §e15 §cto summon God!");
            return true;
        }

        // Determine location
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

        // Use a charge
        data.useGodSummon();
        plugin.getDataManager().save(player.getUniqueId());

        // Determine power form based on how many times summoned
        int used = data.getGodSummonsUsed();
        GodEntity.GodForm form = plugin.getGodManager().getNextGodForm(used - 1);

        // Summon God
        plugin.getGodManager().summonGod(player, target, form);

        player.sendMessage(plugin.prefix() + "§6You have §e" + data.getGodSummonsRemaining() + " §6summon charges remaining.");
        return true;
    }
}
