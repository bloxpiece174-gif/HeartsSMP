package com.heartssmp.listeners;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class GemWeaponListener implements Listener {
    private final HeartsSMPPlugin plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public GemWeaponListener(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isHoldingGem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName() && meta.getDisplayName().contains("Gem");
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

    // RIGHT CLICK = Ability 1
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if (!isHoldingGem(player)) return;

        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null || data.getGemId() == null) return;

        useGemAbility1(player, data);
    }

    // LEFT CLICK (attack entity) = Ability 2
    @EventHandler
    public void onLeftClick(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!isHoldingGem(player)) return;

        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null || data.getGemId() == null) return;

        if (!(event.getEntity() instanceof LivingEntity target)) return;

        useGemAbility2(player, data, target);
    }

    // ═══════════════════════════════════════════════════
    // ABILITY 1 — RIGHT CLICK
    // ═══════════════════════════════════════════════════
    private void useGemAbility1(Player player, PlayerData data) {
        switch (data.getGemId()) {
            case "COMMON_EMBER" -> emberAbility1(player, data.getGemMastery());
            case "COMMON_TIDE" -> tideAbility1(player, data.getGemMastery());
            case "COMMON_STONE" -> stoneAbility1(player, data.getGemMastery());
            case "UNCOMMON_GALE" -> galeAbility1(player, data.getGemMastery());
            case "EPIC_SHADOW" -> shadowAbility1(player, data.getGemMastery());
            case "LEGENDARY_AURORA" -> auroraAbility1(player, data.getGemMastery());
            case "MYTHICAL_VOID" -> voidAbility1(player, data.getGemMastery());
            case "DIVINE_CELESTIA" -> celestiaAbility1(player, data.getGemMastery());
        }
    }

    // ═══════════════════════════════════════════════════
    // ABILITY 2 — LEFT CLICK ON ENTITY
    // ═══════════════════════════════════════════════════
    private void useGemAbility2(Player player, PlayerData data, LivingEntity target) {
        switch (data.getGemId()) {
            case "COMMON_EMBER" -> emberAbility2(player, target, data.getGemMastery());
            case "COMMON_TIDE" -> tideAbility2(player, target, data.getGemMastery());
            case "COMMON_STONE" -> stoneAbility2(player, target, data.getGemMastery());
            case "UNCOMMON_GALE" -> galeAbility2(player, target, data.getGemMastery());
            case "EPIC_SHADOW" -> shadowAbility2(player, target, data.getGemMastery());
            case "LEGENDARY_AURORA" -> auroraAbility2(player, target, data.getGemMastery());
            case "MYTHICAL_VOID" -> voidAbility2(player, target, data.getGemMastery());
            case "DIVINE_CELESTIA" -> celestiaAbility2(player, target, data.getGemMastery());
        }
    }

    // ═══════════════════════════════════════════════════
    // EMBER GEM
    // ═══════════════════════════════════════════════════
    private void emberAbility1(Player p, int mastery) {
        if (onCooldown(p.getUniqueId(), "ember_1", 15_000)) {
            p.sendMessage(plugin.prefix() + "§cEmber Burst on cooldown! §e" + cdLeft(p.getUniqueId(), "ember_1", 15_000) + "s"); return;
        }
        int radius = 4 + mastery;
        for (Entity e : p.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity le && e != p) {
                le.setFireTicks(60 + mastery * 20);
                le.damage(4 + mastery * 2.0, p);
                p.getWorld().spawnParticle(Particle.FLAME, le.getLocation(), 20, 0.3, 0.3, 0.3, 0.1);
            }
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 0.8f);
        p.sendMessage(plugin.prefix() + "§6🔥 Ember Burst! §7Ignited all nearby enemies!");
    }

    private void emberAbility2(Player p, LivingEntity target, int mastery) {
        if (onCooldown(p.getUniqueId(), "ember_2", 8_000)) return;
        target.setFireTicks(100 + mastery * 20);
        target.damage(8 + mastery * 3.0, p);
        p.getWorld().spawnParticle(Particle.LAVA, target.getLocation(), 30, 0.5, 0.5, 0.5, 0.2);
        p.getWorld().strikeLightningEffect(target.getLocation());
        p.sendMessage(plugin.prefix() + "§6🔥 Ember Strike! §7Blazing hit on target!");
    }

    // ═══════════════════════════════════════════════════
    // TIDE GEM
    // ═══════════════════════════════════════════════════
    private void tideAbility1(Player p, int mastery) {
        if (onCooldown(p.getUniqueId(), "tide_1", 20_000)) {
            p.sendMessage(plugin.prefix() + "§cTide Wave on cooldown! §e" + cdLeft(p.getUniqueId(), "tide_1", 20_000) + "s"); return;
        }
        for (Entity e : p.getNearbyEntities(8, 4, 8)) {
            if (e instanceof LivingEntity le && e != p) {
                Vector push = e.getLocation().subtract(p.getLocation()).toVector().normalize().multiply(3 + mastery).setY(1.5);
                e.setVelocity(push);
                le.damage(5 + mastery * 2.0, p);
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, mastery));
                p.getWorld().spawnParticle(Particle.FALLING_WATER, e.getLocation(), 30, 0.5, 0.5, 0.5, 0.3);
            }
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 2f, 0.5f);
        p.sendMessage(plugin.prefix() + "§b🌊 Tide Wave! §7Swept all nearby enemies away!");
    }

    private void tideAbility2(Player p, LivingEntity target, int mastery) {
        if (onCooldown(p.getUniqueId(), "tide_2", 10_000)) return;
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 4));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, mastery));
        target.damage(6 + mastery * 2.0, p);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
        p.getWorld().spawnParticle(Particle.FALLING_WATER, target.getLocation(), 40, 0.5, 0.5, 0.5, 0.2);
        p.sendMessage(plugin.prefix() + "§b🌊 Tide Lock! §7Slowed and weakened target!");
    }

    // ═══════════════════════════════════════════════════
    // STONE GEM
    // ═══════════════════════════════════════════════════
    private void stoneAbility1(Player p, int mastery) {
        if (onCooldown(p.getUniqueId(), "stone_1", 25_000)) {
            p.sendMessage(plugin.prefix() + "§cStone Shield on cooldown! §e" + cdLeft(p.getUniqueId(), "stone_1", 25_000) + "s"); return;
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100 + mastery * 40, 2 + mastery));
        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, mastery));
        p.getWorld().spawnParticle(Particle.BLOCK, p.getLocation(), 80, 0.5, 0.5, 0.5, 0.3, Material.STONE.createBlockData());
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1f, 0.7f);
        p.sendMessage(plugin.prefix() + "§8🪨 Stone Shield! §7Hardened skin activated!");
    }

    private void stoneAbility2(Player p, LivingEntity target, int mastery) {
        if (onCooldown(p.getUniqueId(), "stone_2", 12_000)) return;
        target.damage(10 + mastery * 4.0, p);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 4));
        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 80, 128));
        p.getWorld().spawnParticle(Particle.BLOCK, target.getLocation(), 60, 0.5, 0.5, 0.5, 0.3, Material.STONE.createBlockData());
        p.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.5f);
        p.sendMessage(plugin.prefix() + "§8🪨 Stone Crush! §7Target is rooted!");
    }

    // ═══════════════════════════════════════════════════
    // GALE GEM
    // ═══════════════════════════════════════════════════
    private void galeAbility1(Player p, int mastery) {
        if (onCooldown(p.getUniqueId(), "gale_1", 12_000)) {
            p.sendMessage(plugin.prefix() + "§cGale Dash on cooldown! §e" + cdLeft(p.getUniqueId(), "gale_1", 12_000) + "s"); return;
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60 + mastery * 20, 3 + mastery));
        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 2 + mastery));
        p.setVelocity(p.getLocation().getDirection().multiply(2 + mastery));
        p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation(), 40, 0.3, 0.3, 0.3, 0.2);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1f, 1.5f);
        p.sendMessage(plugin.prefix() + "§f💨 Gale Dash! §7Launched forward at wind speed!");
    }

    private void galeAbility2(Player p, LivingEntity target, int mastery) {
        if (onCooldown(p.getUniqueId(), "gale_2", 8_000)) return;
        Vector v = target.getLocation().subtract(p.getLocation()).toVector().normalize().multiply(-3).setY(2.5 + mastery * 0.5);
        target.setVelocity(v);
        target.damage(6 + mastery * 2.0, p);
        p.getWorld().spawnParticle(Particle.CLOUD, target.getLocation(), 30, 0.5, 0.3, 0.5, 0.3);
        p.sendMessage(plugin.prefix() + "§f💨 Gale Launch! §7Target blown away!");
    }

    // ═══════════════════════════════════════════════════
    // SHADOW GEM
    // ═══════════════════════════════════════════════════
    private void shadowAbility1(Player p, int mastery) {
        if (onCooldown(p.getUniqueId(), "shadow_1", 20_000)) {
            p.sendMessage(plugin.prefix() + "§cShadow Vanish on cooldown! §e" + cdLeft(p.getUniqueId(), "shadow_1", 20_000) + "s"); return;
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 80 + mastery * 40, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1 + mastery));
        p.getWorld().spawnParticle(Particle.SMOKE, p.getLocation(), 40, 0.3, 0.5, 0.3, 0.05);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f);
        p.sendMessage(plugin.prefix() + "§8👤 Shadow Vanish! §7Disappeared into darkness!");
    }

    private void shadowAbility2(Player p, LivingEntity target, int mastery) {
        if (onCooldown(p.getUniqueId(), "shadow_2", 10_000)) return;
        // Teleport behind target, deal backstab damage
        Location behind = target.getLocation().clone().subtract(target.getLocation().getDirection().multiply(2));
        p.teleport(behind);
        double dmg = 15 + mastery * 5.0;
        target.damage(dmg, p);
        p.getWorld().spawnParticle(Particle.PORTAL, target.getLocation(), 40, 0.3, 0.5, 0.3, 0.3);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 1f, 0.5f);
        p.sendMessage(plugin.prefix() + "§8👤 Shadow Stab! §7Teleported and backstabbed for §c" + (int)dmg + " §7damage!");
    }

    // ═══════════════════════════════════════════════════
    // AURORA GEM
    // ═══════════════════════════════════════════════════
    private void auroraAbility1(Player p, int mastery) {
        if (onCooldown(p.getUniqueId(), "aurora_1", 25_000)) {
            p.sendMessage(plugin.prefix() + "§cAurora Heal on cooldown! §e" + cdLeft(p.getUniqueId(), "aurora_1", 25_000) + "s"); return;
        }
        // Heal self + nearby allies
        double maxHp = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        p.setHealth(Math.min(maxHp, p.getHealth() + 6 + mastery * 2.0));
        int healed = 0;
        for (Entity e : p.getNearbyEntities(10, 10, 10)) {
            if (e instanceof Player ally && ally != p) {
                double allyMax = ally.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                ally.setHealth(Math.min(allyMax, ally.getHealth() + 4 + mastery * 2.0));
                ally.sendMessage(plugin.prefix() + "§e✨ Healed by " + p.getName() + "'s Aurora Gem!");
                p.getWorld().spawnParticle(Particle.HEART, ally.getLocation().add(0,1,0), 10, 0.5, 0.5, 0.5, 0.1);
                healed++;
            }
        }
        p.getWorld().spawnParticle(Particle.END_ROD, p.getLocation().add(0,1,0), 60, 0.8, 0.8, 0.8, 0.05);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1f, 1.5f);
        p.sendMessage(plugin.prefix() + "§e✨ Aurora Heal! §7Healed yourself + §e" + healed + " §7allies!");
    }

    private void auroraAbility2(Player p, LivingEntity target, int mastery) {
        if (onCooldown(p.getUniqueId(), "aurora_2", 15_000)) return;
        // Radiant blast — heals you and damages target
        target.damage(12 + mastery * 4.0, p);
        double maxHp = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        p.setHealth(Math.min(maxHp, p.getHealth() + 4 + mastery * 2.0));
        p.getWorld().spawnParticle(Particle.FIREWORK, target.getLocation(), 60, 0.5, 0.5, 0.5, 0.15);
        p.getWorld().spawnParticle(Particle.END_ROD, target.getLocation(), 30, 0.3, 0.3, 0.3, 0.1);
        p.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1.5f);
        p.sendMessage(plugin.prefix() + "§e✨ Radiant Blast! §7Hit target + healed yourself!");
    }

    // ═══════════════════════════════════════════════════
    // VOID GEM
    // ═══════════════════════════════════════════════════
    private void voidAbility1(Player p, int mastery) {
        if (onCooldown(p.getUniqueId(), "void_1", 15_000)) {
            p.sendMessage(plugin.prefix() + "§cVoid Pull on cooldown! §e" + cdLeft(p.getUniqueId(), "void_1", 15_000) + "s"); return;
        }
        int count = 0;
        for (Entity e : p.getNearbyEntities(15, 15, 15)) {
            if (e instanceof LivingEntity le && e != p) {
                Vector pull = p.getLocation().subtract(e.getLocation()).toVector().normalize().multiply(3 + mastery);
                e.setVelocity(pull);
                le.damage(4 + mastery * 2.0, p);
                count++;
                p.getWorld().spawnParticle(Particle.PORTAL, e.getLocation(), 15, 0.3, 0.3, 0.3, 0.2);
            }
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 1f, 0.5f);
        p.sendMessage(plugin.prefix() + "§5🌀 Void Pull! §7Pulled §e" + count + " §7enemies to you!");
    }

    private void voidAbility2(Player p, LivingEntity target, int mastery) {
        if (onCooldown(p.getUniqueId(), "void_2", 12_000)) return;
        // Void crush — freeze + massive damage
        target.damage(20 + mastery * 6.0, p);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 254));
        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 80, 128));
        p.getWorld().spawnParticle(Particle.PORTAL, target.getLocation(), 80, 0.5, 0.8, 0.5, 0.4);
        p.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1f, 0.5f);
        p.sendMessage(plugin.prefix() + "§5🌀 Void Crush! §7Target frozen and crushed for §c" + (int)(20 + mastery * 6.0) + " §7damage!");
    }

    // ═══════════════════════════════════════════════════
    // CELESTIA GEM (Divine)
    // ═══════════════════════════════════════════════════
    private void celestiaAbility1(Player p, int mastery) {
        if (onCooldown(p.getUniqueId(), "celestia_1", 30_000)) {
            p.sendMessage(plugin.prefix() + "§cDivine Transcendence on cooldown! §e" + cdLeft(p.getUniqueId(), "celestia_1", 30_000) + "s"); return;
        }
        // Grant all buffs + flight for 20s
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 400, 255));
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 400, 6 + mastery));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 3 + mastery));
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 4));
        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 400, 4));
        p.setAllowFlight(true);
        p.setFlying(true);
        p.getWorld().spawnParticle(Particle.END_ROD, p.getLocation(), 200, 2, 2, 2, 0.3);
        p.getWorld().spawnParticle(Particle.FIREWORK, p.getLocation(), 100, 1, 1, 1, 0.2);
        p.getWorld().playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 2f, 0.8f);
        plugin.getServer().broadcastMessage("§e§l[HeartsSMP] ★ " + p.getName() + " has entered DIVINE TRANSCENDENCE!");
        p.sendMessage(plugin.prefix() + "§e★ DIVINE TRANSCENDENCE! §720s — unkillable, full power, flight!");
        new BukkitRunnable() {
            public void run() {
                if (p.isOnline()) {
                    p.removePotionEffect(PotionEffectType.RESISTANCE);
                    p.removePotionEffect(PotionEffectType.STRENGTH);
                    if (!p.isOp()) p.setAllowFlight(false);
                    p.sendMessage(plugin.prefix() + "§7Divine Transcendence ended.");
                }
            }
        }.runTaskLater(plugin, 400L);
    }

    private void celestiaAbility2(Player p, LivingEntity target, int mastery) {
        if (onCooldown(p.getUniqueId(), "celestia_2", 20_000)) return;
        // Star Judgement — instant massive damage + heal
        double dmg = 30 + mastery * 10.0;
        target.damage(dmg, p);
        double maxHp = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        p.setHealth(Math.min(maxHp, p.getHealth() + 10));
        p.getWorld().strikeLightningEffect(target.getLocation());
        p.getWorld().spawnParticle(Particle.END_ROD, target.getLocation(), 150, 1, 2, 1, 0.3);
        p.getWorld().spawnParticle(Particle.FIREWORK, target.getLocation(), 100, 0.8, 0.8, 0.8, 0.2);
        p.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 2f, 1f);
        plugin.getServer().broadcastMessage("§e[HeartsSMP] ★ " + p.getName() + " unleashed §6Star Judgement §eon " + (target instanceof Player pt ? pt.getName() : "a mob") + "!");
        p.sendMessage(plugin.prefix() + "§e★ Star Judgement! §c" + (int)dmg + " §7divine damage + healed §a5 hearts§7!");
    }
}
