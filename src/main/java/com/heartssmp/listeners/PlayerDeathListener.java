package com.heartssmp.listeners;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDeathListener implements Listener {
    private final HeartsSMPPlugin plugin;

    public PlayerDeathListener(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;

        // ── PHOENIX RISE — Rebirth (passive on death) ─────────────────
        if (data.hasSkill("phoenix_rise") && !data.hasUsedPhoenixRebirth()) {
            event.setCancelled(true);
            data.setUsedPhoenixRebirth(true);

            double maxHp = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(maxHp * 0.5);
            player.setFireTicks(0);
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1, false, false));

            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 150, 1, 1, 1, 0.2);
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 80, 0.8, 0.8, 0.8, 0.1);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, 2f, 0.5f);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.7f);

            plugin.getServer().broadcastMessage("§6[HeartsSMP] 🔥 " + player.getName() + " rose from the ashes! §ePhoenix Rebirth triggered!");
            player.sendMessage(plugin.prefix() + "§6🔥 PHOENIX REBIRTH! §7You survived death — §eStrength II, Speed II, Fire Resistance for 10s§7!");
            return;
        }

        // ── AURORA GEM Mastery 3 — Light of Aurora (once per life) ────
        if (data.getGemId() != null && data.getGemId().equals("LEGENDARY_AURORA")
                && data.getGemMastery() >= 3 && !data.hasUsedAuroraRevive()) {
            event.setCancelled(true);
            data.setUsedAuroraRevive(true);

            double maxHp = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(maxHp);
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 255, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 4, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 3, false, false));

            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 200, 1.5, 1.5, 1.5, 0.2);
            player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 2f, 1.5f);

            plugin.getServer().broadcastMessage("§e[HeartsSMP] ✨ " + player.getName() + "'s Aurora Gem shone with divine light — death denied!");
            player.sendMessage(plugin.prefix() + "§e✨ LIGHT OF AURORA! §7Death denied — healed to full + immune for 5s!");
            return;
        }

        // ── Normal death — remove a heart ──────────────────────────────
        plugin.getHeartManager().removeHeart(player);

        int hearts = data.getHearts();
        int lives = data.getLives();

        if (hearts <= 0) {
            if (lives <= 0) {
                // Permadeath
                plugin.getServer().broadcastMessage("§c[HeartsSMP] ☠ " + player.getName() + " has lost all their lives and has been eliminated!");
                plugin.getLivesManager().handleElimination(player);
            } else {
                // Lose a life, reset hearts
                plugin.getLivesManager().removeLife(player);
                plugin.getHeartManager().resetHearts(player);
                player.sendMessage(plugin.prefix() + "§cYou lost a life! §7Lives remaining: §e" + data.getLives());
            }
        } else {
            player.sendMessage(plugin.prefix() + "§cYou lost a heart! §7Hearts remaining: §e" + hearts);
        }

        // Reset one-time skill/gem uses on respawn
        data.setUsedPhoenixRebirth(false);
        data.setUsedAuroraRevive(false);
    }
}
