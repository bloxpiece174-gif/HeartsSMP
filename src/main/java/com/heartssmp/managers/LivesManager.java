package com.heartssmp.managers;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.BanList;
import org.bukkit.entity.Player;

import java.util.Date;

public class LivesManager {
    private final HeartsSMPPlugin plugin;

    public LivesManager(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    public int getMaxLives() {
        return plugin.getConfig().getInt("lives.maximum", 10);
    }

    public int getSacrificeHeartCost() {
        return plugin.getConfig().getInt("lives.sacrifice-heart-cost", 2);
    }

    public int getSacrificeMinHearts() {
        return plugin.getConfig().getInt("lives.sacrifice-min-hearts", 5);
    }

    public boolean sacrifice(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return false;

        if (data.getHearts() < getSacrificeMinHearts()) {
            player.sendMessage(plugin.prefix() + "§cYou need at least §e" + getSacrificeMinHearts() + " hearts §cto sacrifice!");
            return false;
        }

        if (data.getLives() >= getMaxLives()) {
            player.sendMessage(plugin.prefix() + "§cYou are already at maximum lives §7(" + getMaxLives() + ")!");
            return false;
        }

        int cost = getSacrificeHeartCost();
        data.setHearts(data.getHearts() - cost);
        data.setLives(data.getLives() + 1);
        plugin.getHeartManager().applyMaxHealth(player, data);
        plugin.getDataManager().save(player.getUniqueId());

        player.sendMessage(plugin.prefix() + "§eSacrificed §c" + cost + " hearts §efor §a+1 Life§e! "
                + "§c❤ " + data.getHearts() + " §7| §a♥ " + data.getLives() + " lives");

        plugin.getGemManager().applyGemEffect(player);
        return true;
    }

    public void onPlayerDeath(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;

        int newLives = data.getLives() - 1;
        data.setLives(Math.max(0, newLives));

        plugin.getSkillManager().onPlayerDeath(player, data);

        if (data.getLives() <= 0) {
            eliminate(player, data);
        } else {
            player.sendMessage(plugin.prefix() + "§cYou lost a life! §7Lives remaining: §a" + data.getLives());
        }

        plugin.getDataManager().save(player.getUniqueId());
    }

    private void eliminate(Player player, PlayerData data) {
        data.setEliminated(true);
        data.setHearts(0);
        data.setLives(0);

        int banWeeks = plugin.getConfig().getInt("elimination.ban-duration-weeks", 2);
        long banMs = (long) banWeeks * 7 * 24 * 60 * 60 * 1000;
        Date banExpiry = new Date(System.currentTimeMillis() + banMs);

        String reason = "§c§lELIMINATED from HeartsSMP!\n§r§7Your lives reached 0.\n§7You are banned for " + banWeeks + " weeks.\n§7Ban expires: " + banExpiry;

        plugin.getServer().getBanList(BanList.Type.NAME).addBan(
                player.getName(), reason, banExpiry, "HeartsSMP"
        );

        player.sendMessage(plugin.prefix() + "§4§lYOU HAVE BEEN ELIMINATED! §cYou are banned for " + banWeeks + " weeks.");

        plugin.getServer().broadcast(
                Component.text("[HeartsSMP] ☠ " + player.getName() + " has been ELIMINATED from the SMP!", NamedTextColor.RED)
        );

        player.kickPlayer("§c§lELIMINATED!\n§rYour lives reached 0.\nBanned for " + banWeeks + " weeks.");
    }

    public boolean addLives(Player player, int amount) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return false;
        int newLives = Math.min(data.getLives() + amount, getMaxLives());
        data.setLives(newLives);
        plugin.getDataManager().save(player.getUniqueId());
        return true;
    }

    public boolean removeLives(Player player, int amount) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return false;
        data.setLives(Math.max(0, data.getLives() - amount));
        plugin.getDataManager().save(player.getUniqueId());
        return true;
    }

    public boolean setLives(Player player, int amount) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return false;
        data.setLives(Math.max(0, Math.min(amount, getMaxLives())));
        plugin.getDataManager().save(player.getUniqueId());
        return true;
    }
}
