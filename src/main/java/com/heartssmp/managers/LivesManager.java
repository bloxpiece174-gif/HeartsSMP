package com.heartssmp.managers;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class LivesManager {
    private final HeartsSMPPlugin plugin;

    public LivesManager(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    public void setLives(Player player, int amount) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        data.setLives(amount);
        plugin.getDataManager().save(player.getUniqueId());
        player.sendMessage(plugin.prefix() + "§aYour lives have been set to §e" + amount);
    }

    public void addLives(Player player, int amount) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        data.setLives(data.getLives() + amount);
        plugin.getDataManager().save(player.getUniqueId());
        player.sendMessage(plugin.prefix() + "§a+" + amount + " §alives!");
    }

    public void removeLives(Player player, int amount) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        int newVal = Math.max(data.getLives() - amount, 0);
        data.setLives(newVal);
        plugin.getDataManager().save(player.getUniqueId());
    }

    // Remove exactly 1 life
    public void removeLife(Player player) {
        removeLives(player, 1);
    }

    // Sacrifice 1 life voluntarily (used by /sacrifice command)
    public void sacrifice(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;

        if (data.getLives() <= 1) {
            player.sendMessage(plugin.prefix() + "§cYou cannot sacrifice your last life!");
            return;
        }

        removeLives(player, 1);
        player.sendMessage(plugin.prefix() + "§eYou sacrificed a life. §7Lives remaining: §e" + data.getLives());
    }

    // Called on normal player death from PlayerDeathListener
    public void onPlayerDeath(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;

        // Remove 1 heart first
        int hearts = data.getHearts() - 1;
        data.setHearts(Math.max(hearts, 0));

        if (hearts <= 0) {
            // No hearts left — lose a life
            int lives = data.getLives() - 1;
            data.setLives(Math.max(lives, 0));

            if (lives <= 0) {
                // Eliminated
                handleElimination(player);
            } else {
                // Reset hearts, lose a life
                int starting = plugin.getConfig().getInt("hearts.starting", 10);
                data.setHearts(starting);
                plugin.getHeartManager().resetHearts(player);
                plugin.getDataManager().save(player.getUniqueId());
                player.sendMessage(plugin.prefix() + "§cYou lost a life! §7Lives remaining: §e" + data.getLives());
            }
        } else {
            plugin.getHeartManager().removeHearts(player, 1);
            plugin.getDataManager().save(player.getUniqueId());
            player.sendMessage(plugin.prefix() + "§cYou lost a heart! §7Hearts remaining: §e" + data.getHearts());
        }
    }

    public void handleElimination(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        data.setEliminated(true);
        data.setLives(0);
        data.setHearts(0);
        plugin.getDataManager().save(player.getUniqueId());

        // Ban the player
        plugin.getServer().getBanList(BanList.Type.NAME).addBan(
                player.getName(),
                "§cEliminated from HeartsSMP — all lives lost.",
                null, "HeartsSMP"
        );

        plugin.getServer().broadcastMessage("");
        plugin.getServer().broadcastMessage("§c§l✦ ══════════════════════════════════ ✦");
        plugin.getServer().broadcastMessage("§4§l        ☠ PLAYER ELIMINATED ☠        ");
        plugin.getServer().broadcastMessage("§e  " + player.getName() + " §7has lost all their lives.");
        plugin.getServer().broadcastMessage("§7  They have been eliminated from HeartsSMP.");
        plugin.getServer().broadcastMessage("§c§l✦ ══════════════════════════════════ ✦");
        plugin.getServer().broadcastMessage("");

        player.sendMessage("§4§l☠ You have been eliminated from HeartsSMP.");
        player.sendMessage("§7Ask an admin to use §e/adminunban §7to restore you.");
        player.kickPlayer("§4☠ Eliminated from HeartsSMP.\n§7Contact an admin to be restored.");
    }
}
