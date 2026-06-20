package com.heartssmp.commands;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import com.heartssmp.skills.Skill;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SkillsCommand implements CommandExecutor {
    private final HeartsSMPPlugin plugin;

    public SkillsCommand(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this.");
            return true;
        }

        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return true;

        if (data.getSkills().isEmpty()) {
            player.sendMessage(plugin.prefix() + "§7You have no skills yet. Earn §e250 kills §7to unlock your first skill!");
            return true;
        }

        player.sendMessage("§8§m-----------------------------------");
        player.sendMessage("§6§l⚔ Your Skills §7(" + data.getSkills().size() + "/31)");
        for (String skillId : data.getSkills()) {
            Skill skill = plugin.getSkillManager().getSkill(skillId);
            if (skill == null) continue;
            int mastery = data.getSkillMastery(skillId);
            player.sendMessage("§7- " + skill.getFormattedName()
                    + " §8[" + skill.getRarity().getDisplayName() + "] §7Mastery: §e" + mastery + "/15"
                    + " §8| /skillinfo " + skillId);
        }
        player.sendMessage("§8§m-----------------------------------");
        return true;
    }
}
