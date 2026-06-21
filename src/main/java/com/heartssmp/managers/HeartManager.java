package com.heartssmp.managers;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class HeartManager {
    private final HeartsSMPPlugin plugin;

    public HeartManager(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    public void setHearts(Player player, int amount) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        data.setHearts(amount);
        applyMaxHealth(player, amount);
        plugin.getDataManager().save(player.getUniqueId());
    }

    public void addHearts(Player player, int amount) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        int max = plugin.getConfig().getInt("hearts.max", 20);
        int newVal = Math.min(data.getHearts() + amount, max);
        data.setHearts(newVal);
        applyMaxHealth(player, newVal);
        plugin.getDataManager().save(player.getUniqueId());
    }

    public void removeHearts(Player player, int amount) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        int newVal = Math.max(data.getHearts() - amount, 1);
        data.setHearts(newVal);
        applyMaxHealth(player, newVal);
        plugin.getDataManager().save(player.getUniqueId());
    }

    // Remove exactly 1 heart on death
    public void removeHeart(Player player) {
        removeHearts(player, 1);
    }

    // Reset hearts to starting value
    public void resetHearts(Player player) {
        int starting = plugin.getConfig().getInt("hearts.starting", 10);
        setHearts(player, starting);
    }

    public void onKillPlayer(Player killer) {
        // Reward killer with +1 heart (configurable)
        if (plugin.getConfig().getBoolean("hearts.reward-on-kill", false)) {
            addHearts(killer, 1);
            killer.sendMessage(plugin.prefix() + "§c+1 Heart §7for player kill!");
        }
        plugin.getSkillManager().onKillPlayer(killer);
    }

    public void onKillMob(Player killer) {
        plugin.getSkillManager().onKillMob(killer);
    }

    public void applyMaxHealth(Player player, PlayerData data) {
        applyMaxHealth(player, data.getHearts());
    }

    public void applyMaxHealth(Player player, int hearts) {
        double maxHp = hearts * 2.0;
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHp);
        if (player.getHealth() > maxHp) player.setHealth(maxHp);
    }
}
