package com.heartssmp.commands;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import com.heartssmp.skills.Skill;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SkillInfoCommand implements CommandExecutor {
    private final HeartsSMPPlugin plugin;

    public SkillInfoCommand(HeartsSMPPlugin plugin) {
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
            player.sendMessage(plugin.prefix() + "§cUsage: /skillinfo <skill_id>");
            return true;
        }

        String skillId = args[0].toLowerCase();
        Skill skill = plugin.getSkillManager().getSkill(skillId);
        if (skill == null) {
            player.sendMessage(plugin.prefix() + "§cSkill not found: §e" + skillId);
            return true;
        }

        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        int mastery = data != null ? data.getSkillMastery(skillId) : 0;

        player.sendMessage("§8§m-----------------------------------");
        player.sendMessage(skill.getFormattedName() + " §8[" + skill.getRarity().getDisplayName() + "]");
        player.sendMessage("§7" + skill.getDescription());
        player.sendMessage("§7Your Mastery: §e" + mastery + "/15");
        player.sendMessage("");
        player.sendMessage(skill.getMovesDescription(mastery));
        player.sendMessage("§8§m-----------------------------------");
        return true;
    }
}
