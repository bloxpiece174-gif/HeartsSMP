package com.heartssmp.listeners;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class GemAbilityListener implements Listener {
    private final HeartsSMPPlugin plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Set<UUID> celestiaTranscendenceActive = new HashSet<>();

    public GemAbilityListener(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean onCooldown(UUID uuid, String key, long ms) {
        long now = System.currentTimeMillis();
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>());
        if (now - cooldowns.get(uuid).getOrDefault(key, 0L) < ms) return true;
        cooldowns.get(uuid).put(key, now);
        return false;
    }

    private long cdLeft(UUID uuid, String key, long ms) {
        long now = System.currentTimeMillis();
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>());
        return Math.max(0, (ms - (now - cooldowns.get(uuid).getOrDefault(key, 0L))) / 1000);
    }

    // RIGHT CLICK with empty hand = gem active ability
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        // Only trigger if holding nothing or a non-custom item
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held != null && held.getType() != Material.AIR && held.hasItemMeta()) return; // let item listener handle

        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null || data.getGemId() == null) return;

        int mastery = data.getGemMastery();
        switch (data.getGemId()) {
            case "LEGENDARY_AURORA" -> useAuroraAbility(player, mastery);
            case "MYTHICAL_VOID" -> useVoidAbility(player, mastery);
            case "DIVINE_CELESTIA" -> useCelestiaAbility(player, mastery);
            case "EPIC_SHADOW" -> useShadowAbility(player, mastery);
        }
    }

    // ─── AURORA GEM ─────────────────────────────────────────────────
    private void useAuroraAbility(Player player, int mastery) {
        if (mastery == 1) {
            if (onCooldown(player.getUniqueId(), "aurora_1", 60_000)) {
                player.sendMessage(plugin.prefix() + "§cAurora Heal on cooldown! §e" + cdLeft(player.getUniqueId(), "aurora_1", 60_000) + "s");
                return;
            }
            if (player.getHealth() / player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue() > 0.30) {
                player.sendMessage(plugin.prefix() + "§cAurora Heal only triggers below 30% HP!");
                return;
            }
            double heal = Math.min(10.0, player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue() - player.getHealth());
            player.setHealth(player.getHealth() + heal);
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0,1,0), 60, 0.5, 0.5, 0.5, 0.05);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1f, 1.5f);
            player.sendMessage(plugin.prefix() + "§e✨ Aurora Heal! §7Healed §a5 hearts§7!");

        } else if (mastery == 2) {
            if (onCooldown(player.getUniqueId(), "aurora_2", 45_000)) {
                player.sendMessage(plugin.prefix() + "§cRadiant Burst on cooldown! §e" + cdLeft(player.getUniqueId(), "aurora_2", 45_000) + "s");
                return;
            }
            double maxHp = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(Math.min(maxHp, player.getHealth() + 8.0));
            for (Entity e : player.getNearbyEntities(8, 8, 8)) {
                if (e instanceof LivingEntity le && e != player) {
                    le.damage(6.0, player);
                    player.getWorld().spawnParticle(Particle.FIREWORK, le.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
                }
            }
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 100, 1, 1, 1, 0.2);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1.5f);
            player.sendMessage(plugin.prefix() + "§e✨ Radiant Burst! §7Healed §a4 hearts §7and blasted nearby enemies!");

        } else if (mastery == 3) {
            player.sendMessage(plugin.prefix() + "§e✨ Light of Aurora is §lpassive §r§7— activates automatically on death (once per life)!");
        }
    }

    // ─── VOID GEM ────────────────────────────────────────────────────
    private void useVoidAbility(Player player, int mastery) {
        if (mastery == 1) {
            if (onCooldown(player.getUniqueId(), "void_1", 10_000)) {
                player.sendMessage(plugin.prefix() + "§cVoid Tear on cooldown! §e" + cdLeft(player.getUniqueId(), "void_1", 10_000) + "s");
                return;
            }
            Player target = getNearestPlayer(player, 15);
            if (target == null) { player.sendMessage(plugin.prefix() + "§cNo target in range!"); return; }
            target.setVelocity(new Vector(0, 2.5, 0));
            player.getWorld().spawnParticle(Particle.PORTAL, target.getLocation(), 60, 0.5, 0.5, 0.5, 0.3);
            player.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
            target.sendMessage(plugin.prefix() + "§5" + player.getName() + "'s Void Tear launched you!");
            player.sendMessage(plugin.prefix() + "§5🌀 Void Tear! §7Launched §e" + target.getName() + " §7into the air!");

        } else if (mastery == 2) {
            if (onCooldown(player.getUniqueId(), "void_2", 20_000)) {
                player.sendMessage(plugin.prefix() + "§cDimension Pull on cooldown! §e" + cdLeft(player.getUniqueId(), "void_2", 20_000) + "s");
                return;
            }
            int count = 0;
            for (Entity e : player.getNearbyEntities(15, 15, 15)) {
                if (e instanceof LivingEntity le && e != player) {
                    Vector pull = player.getLocation().subtract(e.getLocation()).toVector().normalize().multiply(2.5);
                    e.setVelocity(pull);
                    le.damage(5.0, player);
                    player.getWorld().spawnParticle(Particle.PORTAL, e.getLocation(), 20, 0.3, 0.3, 0.3, 0.3);
                    count++;
                }
            }
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 1f, 0.5f);
            player.sendMessage(plugin.prefix() + "§5🌀 Dimension Pull! §7Pulled §e" + count + " §7entities!");

        } else if (mastery == 3) {
            if (onCooldown(player.getUniqueId(), "void_3", 90_000)) {
                player.sendMessage(plugin.prefix() + "§cVoid Collapse on cooldown! §e" + cdLeft(player.getUniqueId(), "void_3", 90_000) + "s");
                return;
            }
            int count = 0;
            for (Entity e : player.getNearbyEntities(20, 20, 20)) {
                if (e instanceof LivingEntity le && e != player) {
                    le.damage(20.0, player);
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 254, false, true));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 128, false, true));
                    Vector pull = player.getLocation().subtract(e.getLocation()).toVector().normalize().multiply(3);
                    e.setVelocity(pull);
                    player.getWorld().spawnParticle(Particle.PORTAL, e.getLocation(), 40, 0.5, 0.5, 0.5, 0.5);
                    count++;
                }
            }
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 0.5f);
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 300, 2, 2, 2, 0.5);
            player.sendMessage(plugin.prefix() + "§5☠ VOID COLLAPSE! §7Imploded §e" + count + " §7entities — 20 dmg + 5s stun!");
        }
    }

    // ─── CELESTIA GEM ───────────────────────────────────────────────
    private void useCelestiaAbility(Player player, int mastery) {
        if (mastery == 1) {
            if (onCooldown(player.getUniqueId(), "celestia_1", 60_000)) {
                player.sendMessage(plugin.prefix() + "§cCelestial Guard on cooldown! §e" + cdLeft(player.getUniqueId(), "celestia_1", 60_000) + "s");
                return;
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 4, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 2, false, true));
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0,1,0), 80, 0.8, 0.8, 0.8, 0.1);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);
            player.sendMessage(plugin.prefix() + "§e★ Celestial Guard! §7Absorption V + Resistance III for §e10s§7!");

        } else if (mastery == 2) {
            if (onCooldown(player.getUniqueId(), "celestia_2", 90_000)) {
                player.sendMessage(plugin.prefix() + "§cStar Convergence on cooldown! §e" + cdLeft(player.getUniqueId(), "celestia_2", 90_000) + "s");
                return;
            }
            plugin.getServer().broadcastMessage("§e[HeartsSMP] ✨ " + player.getName() + " calls upon the stars!");
            new BukkitRunnable() {
                int ticks = 0;
                public void run() {
                    if (!player.isOnline()) { cancel(); return; }
                    ticks++;
                    for (Entity e : player.getNearbyEntities(15, 15, 15)) {
                        if (e instanceof LivingEntity le && e != player) {
                            le.damage(3.0, player);
                            player.getWorld().strikeLightningEffect(e.getLocation());
                        }
                    }
                    player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0,3,0), 20, 2, 0.5, 2, 0.1);
                    if (ticks >= 10) { cancel(); player.sendMessage(plugin.prefix() + "§7Star Convergence ended."); }
                }
            }.runTaskTimer(plugin, 0L, 20L);
            player.sendMessage(plugin.prefix() + "§e★ Star Convergence! §7Stars rain for §e10s§7!");

        } else if (mastery == 3) {
            if (celestiaTranscendenceActive.contains(player.getUniqueId())) {
                player.sendMessage(plugin.prefix() + "§cDivine Transcendence already active!");
                return;
            }
            if (onCooldown(player.getUniqueId(), "celestia_3", 120_000)) {
                player.sendMessage(plugin.prefix() + "§cDivine Transcendence on cooldown! §e" + cdLeft(player.getUniqueId(), "celestia_3", 120_000) + "s");
                return;
            }
            celestiaTranscendenceActive.add(player.getUniqueId());
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 600, 255, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 600, 6, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 3, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 4, false, false));
            player.setAllowFlight(true);
            player.setFlying(true);
            plugin.getServer().broadcastMessage("§e§l[HeartsSMP] ★ " + player.getName() + " has ascended to DIVINE TRANSCENDENCE! ★");
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 400, 3, 3, 3, 0.4);
            player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 2f, 0.7f);
            player.sendMessage(plugin.prefix() + "§e★ DIVINE TRANSCENDENCE! §730s — unkillable, 8x dmg, flight!");

            new BukkitRunnable() {
                int ticks = 0;
                public void run() {
                    if (!player.isOnline()) { cancel(); return; }
                    ticks++;
                    // Heal nearby allies 2 hearts/s
                    for (Entity e : player.getNearbyEntities(20, 20, 20)) {
                        if (e instanceof Player ally && ally != player) {
                            double maxHp = ally.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                            ally.setHealth(Math.min(maxHp, ally.getHealth() + 4.0));
                            player.getWorld().spawnParticle(Particle.HEART, ally.getLocation().add(0,1,0), 5, 0.3, 0.3, 0.3, 0.05);
                        }
                    }
                    // Supernova trail
                    player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation(), 10, 0.5, 0.5, 0.5, 0.2);
                    player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 5, 0.3, 0.3, 0.3, 0.05);
                    if (ticks >= 30) {
                        celestiaTranscendenceActive.remove(player.getUniqueId());
                        player.removePotionEffect(PotionEffectType.RESISTANCE);
                        player.removePotionEffect(PotionEffectType.STRENGTH);
                        if (!player.isOp()) player.setAllowFlight(false);
                        player.setFlying(false);
                        player.sendMessage(plugin.prefix() + "§7Divine Transcendence ended.");
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
    }

    // ─── SHADOW GEM ─────────────────────────────────────────────────
    private void useShadowAbility(Player player, int mastery) {
        if (onCooldown(player.getUniqueId(), "shadow_gem", 20_000)) {
            player.sendMessage(plugin.prefix() + "§cShadow Gem on cooldown! §e" + cdLeft(player.getUniqueId(), "shadow_gem", 20_000) + "s");
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100 + mastery * 40, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, mastery, false, false));
        if (mastery >= 2) player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 0, false, false));
        player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation(), 40, 0.3, 0.5, 0.3, 0.05);
        player.sendMessage(plugin.prefix() + "§8Shadow Cloak! §7Vanished!");
    }

    private Player getNearestPlayer(Player source, double range) {
        Player nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Entity e : source.getNearbyEntities(range, range, range)) {
            if (e instanceof Player p && p != source) {
                double dist = p.getLocation().distanceSquared(source.getLocation());
                if (dist < minDist) { minDist = dist; nearest = p; }
            }
        }
        return nearest;
    }
}
