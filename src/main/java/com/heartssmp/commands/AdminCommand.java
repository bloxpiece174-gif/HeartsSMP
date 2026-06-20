package com.heartssmp.commands;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminCommand implements CommandExecutor {
    private final HeartsSMPPlugin plugin;
    private final String type; // "hearts", "lives", "gem", "skill", "unban"

    public AdminCommand(HeartsSMPPlugin plugin, String type) {
        this.plugin = plugin;
        this.type = type;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("heartssmp.admin")) {
            sender.sendMessage(plugin.prefix() + "§cNo permission.");
            return true;
        }

        switch (type) {
            case "hearts" -> handleHearts(sender, args);
            case "lives" -> handleLives(sender, args);
            case "gem" -> handleGem(sender, args);
            case "skill" -> handleSkill(sender, args);
            case "item" -> handleItem(sender, args);
            case "unban" -> handleUnban(sender, args);
        }
        return true;
    }

    private void handleHearts(CommandSender sender, String[] args) {
        if (args.length < 3) { sender.sendMessage("Usage: /adminhearts <player> <set|add|remove> <amount>"); return; }
        Player target = getTarget(sender, args[0]);
        if (target == null) return;
        int amount = parseInt(sender, args[2]);
        if (amount < 0) return;

        switch (args[1].toLowerCase()) {
            case "set" -> { plugin.getHeartManager().setHearts(target, amount); sender.sendMessage(plugin.prefix() + "§aSet " + target.getName() + "'s hearts to " + amount); }
            case "add" -> { plugin.getHeartManager().addHearts(target, amount); sender.sendMessage(plugin.prefix() + "§aAdded " + amount + " hearts to " + target.getName()); }
            case "remove" -> { plugin.getHeartManager().removeHearts(target, amount); sender.sendMessage(plugin.prefix() + "§aRemoved " + amount + " hearts from " + target.getName()); }
            default -> sender.sendMessage("Usage: /adminhearts <player> <set|add|remove> <amount>");
        }
    }

    private void handleLives(CommandSender sender, String[] args) {
        if (args.length < 3) { sender.sendMessage("Usage: /adminlives <player> <set|add|remove> <amount>"); return; }
        Player target = getTarget(sender, args[0]);
        if (target == null) return;
        int amount = parseInt(sender, args[2]);
        if (amount < 0) return;

        switch (args[1].toLowerCase()) {
            case "set" -> { plugin.getLivesManager().setLives(target, amount); sender.sendMessage(plugin.prefix() + "§aSet " + target.getName() + "'s lives to " + amount); }
            case "add" -> { plugin.getLivesManager().addLives(target, amount); sender.sendMessage(plugin.prefix() + "§aAdded " + amount + " lives to " + target.getName()); }
            case "remove" -> { plugin.getLivesManager().removeLives(target, amount); sender.sendMessage(plugin.prefix() + "§aRemoved " + amount + " lives from " + target.getName()); }
            default -> sender.sendMessage("Usage: /adminlives <player> <set|add|remove> <amount>");
        }
    }

    private void handleGem(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage("Usage: /admingem <player> <gem_id>"); return; }
        Player target = getTarget(sender, args[0]);
        if (target == null) return;
        String gemId = args[1].toUpperCase();
        if (plugin.getGemManager().getGem(gemId) == null) {
            sender.sendMessage(plugin.prefix() + "§cUnknown gem ID: " + gemId);
            listGems(sender);
            return;
        }
        PlayerData data = plugin.getDataManager().get(target.getUniqueId());
        if (data != null) {
            data.setGemId(gemId);
            data.setGemMastery(1);
            plugin.getDataManager().save(target.getUniqueId());
            sender.sendMessage(plugin.prefix() + "§aSet " + target.getName() + "'s gem to " + gemId);
            target.sendMessage(plugin.prefix() + "§aYour gem was changed to: §e" + gemId);
        }
    }

    private void handleSkill(CommandSender sender, String[] args) {
        if (args.length < 3) { sender.sendMessage("Usage: /adminskill <player> <give|remove> <skill_id>"); return; }
        Player target = getTarget(sender, args[0]);
        if (target == null) return;
        String skillId = args[2].toLowerCase();
        PlayerData data = plugin.getDataManager().get(target.getUniqueId());
        if (data == null) return;

        switch (args[1].toLowerCase()) {
            case "give" -> {
                if (plugin.getSkillManager().getSkill(skillId) == null) {
                    sender.sendMessage(plugin.prefix() + "§cUnknown skill ID: " + skillId);
                    return;
                }
                if (skillId.equals("graceful_enlightenment")) {
                    plugin.getSkillManager().grantDivineSkill(target);
                } else {
                    data.addSkill(skillId);
                    plugin.getSkillManager().getSkill(skillId).onUnlock(target);
                    plugin.getDivineTrialManager().checkMythicalCompletion(target, data);
                }
                plugin.getDataManager().save(target.getUniqueId());
                sender.sendMessage(plugin.prefix() + "§aGave skill §e" + skillId + " §ato " + target.getName());
            }
            case "remove" -> {
                data.removeSkill(skillId);
                plugin.getDataManager().save(target.getUniqueId());
                sender.sendMessage(plugin.prefix() + "§aRemoved skill §e" + skillId + " §afrom " + target.getName());
            }
            default -> sender.sendMessage("Usage: /adminskill <player> <give|remove> <skill_id>");
        }
    }

    private void handleItem(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage("Usage: /adminitem <player> <item_id> [amount]"); return; }
        Player target = getTarget(sender, args[0]);
        if (target == null) return;
        String itemId = args[1].toLowerCase();
        int amount = args.length >= 3 ? parseInt(args[2], 1) : 1;

        if (plugin.getItemManager().createItem(itemId) == null) {
            sender.sendMessage(plugin.prefix() + "§cUnknown item ID: " + itemId);
            return;
        }
        for (int i = 0; i < amount; i++) {
            plugin.getItemManager().giveItem(target, itemId);
        }
        sender.sendMessage(plugin.prefix() + "§aGave §e" + amount + "x " + itemId + " §ato " + target.getName());
    }

    private int parseInt(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
    }

    private void handleUnban(CommandSender sender, String[] args) {
        if (args.length < 1) { sender.sendMessage("Usage: /adminunban <player>"); return; }
        String name = args[0];
        plugin.getServer().getBanList(BanList.Type.NAME).pardon(name);
        // Try to clear elimination status if player is online
        Player online = plugin.getServer().getPlayer(name);
        if (online != null) {
            PlayerData data = plugin.getDataManager().get(online.getUniqueId());
            if (data != null) {
                data.setEliminated(false);
                data.setHearts(plugin.getConfig().getInt("hearts.starting", 10));
                data.setLives(1);
                plugin.getDataManager().save(online.getUniqueId());
            }
        }
        sender.sendMessage(plugin.prefix() + "§aUnbanned §e" + name + " §afrom HeartsSMP ban list.");
    }

    private Player getTarget(CommandSender sender, String name) {
        Player target = plugin.getServer().getPlayer(name);
        if (target == null) {
            sender.sendMessage(plugin.prefix() + "§cPlayer not online: " + name);
        }
        return target;
    }

    private int parseInt(CommandSender sender, String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.prefix() + "§cInvalid number: " + s);
            return -1;
        }
    }

    private void listGems(CommandSender sender) {
        sender.sendMessage("§7Valid gem IDs:");
        plugin.getGemManager().getAllGems().forEach(g -> sender.sendMessage("§7- §e" + g.getId()));
    }
}
