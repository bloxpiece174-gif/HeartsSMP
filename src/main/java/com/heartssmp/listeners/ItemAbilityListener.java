package com.heartssmp.listeners;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class ItemAbilityListener implements Listener {
    private final HeartsSMPPlugin plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public ItemAbilityListener(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean onCooldown(UUID uuid, String item, long milliseconds) {
        long now = System.currentTimeMillis();
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>());
        long last = cooldowns.get(uuid).getOrDefault(item, 0L);
        if (now - last < milliseconds) {
            long remaining = (milliseconds - (now - last)) / 1000;
            return true;
        }
        cooldowns.get(uuid).put(item, now);
        return false;
    }

    private long getCooldownLeft(UUID uuid, String item, long milliseconds) {
        long now = System.currentTimeMillis();
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>());
        long last = cooldowns.get(uuid).getOrDefault(item, 0L);
        return Math.max(0, (milliseconds - (now - last)) / 1000);
    }

    private String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return null;
        // Our items have "HeartsSMP Custom Item" as last lore line — identify by display name
        if (!meta.hasDisplayName()) return null;
        String name = meta.getDisplayName();
        // Map display name to ID
        return switch (name) {
            case "Chrono Watch" -> "chrono_watch";
            case "Void Cloak" -> "void_cloak";
            case "Shadow Cloak" -> "shadow_cloak";
            case "Storm Staff" -> "storm_staff";
            case "Celestial Blade" -> "celestial_blade";
            case "Star Fragment" -> "star_fragment";
            case "Titan Hammer" -> "titan_hammer";
            case "Aurora Staff" -> "aurora_staff";
            case "Hell\u0441ore Fragment" -> "hell_core";
            case "Hellcore Fragment" -> "hell_core";
            case "Heart Shard" -> "heart_shard";
            case "Life Crystal" -> "life_crystal";
            case "Celestia Dust" -> "celestia_dust";
            default -> null;
        };
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        String id = getItemId(item);
        if (id == null) return;

        switch (id) {
            case "chrono_watch" -> useChronoWatch(player);
            case "storm_staff" -> useStormStaff(player);
            case "titan_hammer" -> useTitanHammer(player);
            case "star_fragment" -> useStarFragment(player);
            case "aurora_staff" -> useAuroraStaff(player);
            case "hell_core" -> useHellCore(player);
            case "heart_shard" -> useHeartShard(player, item);
            case "life_crystal" -> useLifeCrystal(player, item);
            case "celestia_dust" -> useCelestiaDust(player, item);
            case "celestial_blade" -> {} // handled in damage event
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        String id = getItemId(item);
        if (id == null) return;

        switch (id) {
            case "void_cloak" -> useVoidCloak(player);
            case "shadow_cloak" -> useShadowCloak(player);
        }
    }

    // ─── CHRONO WATCH ───────────────────────────────────────────────
    private void useChronoWatch(Player player) {
        if (onCooldown(player.getUniqueId(), "chrono_watch", 30_000)) {
            player.sendMessage(plugin.prefix() + "§cChrono Watch on cooldown! §e" + getCooldownLeft(player.getUniqueId(), "chrono_watch", 30_000) + "s left");
            return;
        }
        int count = 0;
        for (Entity e : player.getNearbyEntities(8, 8, 8)) {
            if (e instanceof LivingEntity le && e != player) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 254, false, true));
                le.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 128, false, true));
                player.getWorld().spawnParticle(Particle.END_ROD, le.getLocation().add(0,1,0), 20, 0.5, 0.5, 0.5, 0.05);
                count++;
            }
        }
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 2f);
        player.sendMessage(plugin.prefix() + "§b⏱ Chrono Watch! §7Froze §e" + count + " §7entities for §e3s§7!");
    }

    // ─── VOID CLOAK ─────────────────────────────────────────────────
    private void useVoidCloak(Player player) {
        if (onCooldown(player.getUniqueId(), "void_cloak", 120_000)) {
            player.sendMessage(plugin.prefix() + "§cVoid Cloak on cooldown! §e" + getCooldownLeft(player.getUniqueId(), "void_cloak", 120_000) + "s left");
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 255, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, false, false));
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 80, 0.5, 1, 0.5, 0.3);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
        player.sendMessage(plugin.prefix() + "§5🌀 Void Cloak activated! §75s invincibility!");
        new BukkitRunnable() {
            public void run() {
                if (player.isOnline()) {
                    player.removePotionEffect(PotionEffectType.RESISTANCE);
                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                    player.sendMessage(plugin.prefix() + "§7Void Cloak worn off.");
                }
            }
        }.runTaskLater(plugin, 100L);
    }

    // ─── SHADOW CLOAK ───────────────────────────────────────────────
    private void useShadowCloak(Player player) {
        if (onCooldown(player.getUniqueId(), "shadow_cloak", 20_000)) {
            player.sendMessage(plugin.prefix() + "§cShadow Cloak on cooldown! §e" + getCooldownLeft(player.getUniqueId(), "shadow_cloak", 20_000) + "s left");
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, false, false));
        player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation(), 40, 0.3, 0.5, 0.3, 0.05);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 0.5f, 1.5f);
        player.sendMessage(plugin.prefix() + "§8Shadow Cloak! §7Invisible for §e5s§7!");
        new BukkitRunnable() {
            public void run() {
                if (player.isOnline()) player.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        }.runTaskLater(plugin, 100L);
    }

    // ─── STORM STAFF ────────────────────────────────────────────────
    private void useStormStaff(Player player) {
        if (onCooldown(player.getUniqueId(), "storm_staff", 15_000)) {
            player.sendMessage(plugin.prefix() + "§cStorm Staff on cooldown! §e" + getCooldownLeft(player.getUniqueId(), "storm_staff", 15_000) + "s left");
            return;
        }
        for (Entity e : player.getNearbyEntities(10, 10, 10)) {
            if (e instanceof LivingEntity && e != player) {
                player.getWorld().strikeLightning(e.getLocation());
            }
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
        player.sendMessage(plugin.prefix() + "§9⚡ Storm Staff! §7Lightning strikes all nearby enemies!");
    }

    // ─── TITAN HAMMER ───────────────────────────────────────────────
    private void useTitanHammer(Player player) {
        if (onCooldown(player.getUniqueId(), "titan_hammer", 20_000)) {
            player.sendMessage(plugin.prefix() + "§cTitan Hammer on cooldown! §e" + getCooldownLeft(player.getUniqueId(), "titan_hammer", 20_000) + "s left");
            return;
        }
        player.getWorld().createExplosion(player.getLocation(), 0f, false, false);
        player.getWorld().spawnParticle(Particle.BLOCK, player.getLocation(), 150, 1, 0.1, 1, 0.5, Material.STONE.createBlockData());
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 2f, 0.5f);
        for (Entity e : player.getNearbyEntities(6, 4, 6)) {
            if (e instanceof LivingEntity le && e != player) {
                le.damage(10.0, player);
                Vector v = le.getLocation().subtract(player.getLocation()).toVector().normalize().multiply(2).setY(1.2);
                le.setVelocity(v);
            }
        }
        player.sendMessage(plugin.prefix() + "§8🔨 Ground Slam! §7Shockwave hits all nearby enemies!");
    }

    // ─── STAR FRAGMENT ──────────────────────────────────────────────
    private void useStarFragment(Player player) {
        if (onCooldown(player.getUniqueId(), "star_fragment", 60_000)) {
            player.sendMessage(plugin.prefix() + "§cStar Fragment on cooldown! §e" + getCooldownLeft(player.getUniqueId(), "star_fragment", 60_000) + "s left");
            return;
        }
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null || data.getSkills().isEmpty()) {
            player.sendMessage(plugin.prefix() + "§cYou have no skills to boost!");
            return;
        }
        // Give 3 random short-burst buffs representing skill uses
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 2, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 3, false, true));
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0,1,0), 100, 1, 1, 1, 0.2);
        player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.5f);
        player.sendMessage(plugin.prefix() + "§e⭐ Star Fragment! §7Absorbed — Strength III, Speed III & Absorption IV for 10s!");
    }

    // ─── AURORA STAFF ───────────────────────────────────────────────
    private void useAuroraStaff(Player player) {
        if (onCooldown(player.getUniqueId(), "aurora_staff", 25_000)) {
            player.sendMessage(plugin.prefix() + "§cAurora Staff on cooldown! §e" + getCooldownLeft(player.getUniqueId(), "aurora_staff", 25_000) + "s left");
            return;
        }
        int healed = 0;
        for (Entity e : player.getNearbyEntities(10, 10, 10)) {
            if (e instanceof Player ally && ally != player) {
                ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 2, false, true));
                player.getWorld().spawnParticle(Particle.HEART, ally.getLocation().add(0,1,0), 10, 0.5, 0.5, 0.5, 0.1);
                healed++;
            }
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1, false, true));
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 60, 0.8, 0.8, 0.8, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1f, 1.5f);
        player.sendMessage(plugin.prefix() + "§e✨ Aurora Staff! §7Healed yourself + §e" + healed + " §7nearby allies!");
    }

    // ─── HELLCORE FRAGMENT ──────────────────────────────────────────
    private void useHellCore(Player player) {
        if (onCooldown(player.getUniqueId(), "hell_core", 25_000)) {
            player.sendMessage(plugin.prefix() + "§cHellcore on cooldown! §e" + getCooldownLeft(player.getUniqueId(), "hell_core", 25_000) + "s left");
            return;
        }
        for (Entity e : player.getNearbyEntities(8, 8, 8)) {
            if (e instanceof LivingEntity le && e != player) {
                le.setFireTicks(100);
                le.damage(6.0, player);
                player.getWorld().spawnParticle(Particle.FLAME, le.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
            }
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 0.7f);
        player.getWorld().spawnParticle(Particle.LAVA, player.getLocation(), 40, 1, 0.5, 1, 0.3);
        player.sendMessage(plugin.prefix() + "§4🔥 Hellcore Burst! §7All nearby enemies are burning!");
    }

    // ─── HEART SHARD ────────────────────────────────────────────────
    private void useHeartShard(Player player, ItemStack item) {
        plugin.getHeartManager().addHearts(player, 1);
        item.setAmount(item.getAmount() - 1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0,1,0), 15, 0.5, 0.5, 0.5, 0.1);
        player.sendMessage(plugin.prefix() + "§c❤ +1 Heart from Heart Shard!");
    }

    // ─── LIFE CRYSTAL ───────────────────────────────────────────────
    private void useLifeCrystal(Player player, ItemStack item) {
        plugin.getLivesManager().addLives(player, 1);
        item.setAmount(item.getAmount() - 1);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.2f);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0,1,0), 20, 0.5, 0.5, 0.5, 0.05);
        player.sendMessage(plugin.prefix() + "§a♥ +1 Life from Life Crystal!");
    }

    // ─── CELESTIA DUST ──────────────────────────────────────────────
    private void useCelestiaDust(Player player, ItemStack item) {
        if (onCooldown(player.getUniqueId(), "celestia_dust", 120_000)) {
            player.sendMessage(plugin.prefix() + "§cCelestia Dust on cooldown! §e" + getCooldownLeft(player.getUniqueId(), "celestia_dust", 120_000) + "s left");
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 2, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 2, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 300, 1, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 300, 1, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 300, 3, false, true));
        item.setAmount(item.getAmount() - 1);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0,1,0), 80, 0.8, 0.8, 0.8, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.8f);
        player.sendMessage(plugin.prefix() + "§e✨ Celestia Dust! §7All stats boosted for §e15s§7!");
    }
}
