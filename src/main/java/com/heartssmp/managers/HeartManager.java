package com.heartssmp.managers;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public class HeartManager {
    private final HeartsSMPPlugin plugin;

    public HeartManager(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    public int getMaxHearts() {
        return plugin.getConfig().getInt("hearts.maximum", 30);
    }

    public int getKillReward() {
        return plugin.getConfig().getInt("hearts.kill-reward", 1);
    }

    public void addHearts(Player player, int amount) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;

        int newHearts = Math.min(data.getHearts() + amount, getMaxHearts());
        data.setHearts(newHearts);
        applyMaxHealth(player, data);
    }

    public void removeHearts(Player player, int amount) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;

        int newHearts = data.getHearts() - amount;
        data.setHearts(newHearts);
        applyMaxHealth(player, data);
    }

    public void setHearts(Player player, int amount) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;

        int clamped = Math.max(0, Math.min(amount, getMaxHearts()));
        data.setHearts(clamped);
        applyMaxHealth(player, data);
    }

    public void applyMaxHealth(Player player, PlayerData data) {
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr == null) return;
        double newMax = data.getHearts() * 2.0;
        newMax = Math.max(2.0, newMax);
        attr.setBaseValue(newMax);
        if (player.getHealth() > newMax) {
            player.setHealth(newMax);
        }
    }

    public void onKillPlayer(Player killer) {
        PlayerData data = plugin.getDataManager().get(killer.getUniqueId());
        if (data == null) return;

        addHearts(killer, getKillReward());
        data.addKill();

        killer.sendMessage(plugin.prefix() + "§c+1 Heart §7from player kill! §c❤ " + data.getHearts() + " hearts");
        plugin.getDataManager().save(killer.getUniqueId());

        plugin.getSkillManager().checkSkillUnlock(killer, data);
    }

    public void onKillMob(Player killer) {
        PlayerData data = plugin.getDataManager().get(killer.getUniqueId());
        if (data == null) return;

        data.addMobKill();
        plugin.getDataManager().save(killer.getUniqueId());
        plugin.getSkillManager().checkSkillUnlock(killer, data);
    }
}
