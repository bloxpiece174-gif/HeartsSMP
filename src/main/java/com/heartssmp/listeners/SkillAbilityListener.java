package com.heartssmp.listeners;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class SkillAbilityListener implements Listener {
    private final HeartsSMPPlugin plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Set<UUID> divineFormActive = new HashSet<>();
    private final Set<UUID> absoluteGraceActive = new HashSet<>();
    private final Set<UUID> phoenixRevived = new HashSet<>();

    public SkillAbilityListener(HeartsSMPPlugin plugin) {
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

    // ── SNEAK = activate skill move 1 / 2 ───────────────────────────
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        Player player = event.getPlayer();
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;

        // Grace Move 1 — sneak → Aura of Protection
        if (data.hasSkill("graceful_enlightenment") && data.getSkillMastery("graceful_enlightenment") >= 3) {
            useGraceMove1(player);
        }
        // Phoenix — sneak at low HP → Rebirth passive handled in death listener
        // Void Step — sneak → Phase Through
        if (data.hasSkill("void_step") && data.getSkillMastery("void_step") >= 9) {
            useVoidPhase(player);
        }
        // DragonScale — sneak → Scale Harden
        if (data.hasSkill("dragonscale_skin") && data.getSkillMastery("dragonscale_skin") >= 3) {
            useDragonHarden(player);
        }
    }

    // ── DROP KEY = activate skill move 2 / ultimate ──────────────────
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        event.setCancelled(true); // prevent actual drop
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;

        // Grace Move 2 — Celestial Smite
        if (data.hasSkill("graceful_enlightenment") && data.getSkillMastery("graceful_enlightenment") >= 6) {
            useGraceMove2(player);
            return;
        }
        // Grace Ultimate — Absolute Grace
        if (data.hasSkill("graceful_enlightenment") && data.getSkillMastery("graceful_enlightenment") >= 15) {
            useAbsoluteGrace(player);
            return;
        }
        // Storm Caller — Apocalypse Storm
        if (data.hasSkill("storm_caller") && data.getSkillMastery("storm_caller") >= 12) {
            useApocalypseStorm(player);
            return;
        }
        // Phoenix — Phoenix Ignition
        if (data.hasSkill("phoenix_rise") && data.getSkillMastery("phoenix_rise") >= 15) {
            usePhoenixIgnition(player);
            return;
        }
        // Void Step — Blink
        if (data.hasSkill("void_step") && data.getSkillMastery("void_step") >= 3) {
            useVoidBlink(player);
            return;
        }
        // Earth Shatter — Ground Slam
        if (data.hasSkill("earth_shatter") && data.getSkillMastery("earth_shatter") >= 3) {
            useGroundSlam(player);
            return;
        }
        // Soul Reaper — Soul Drain
        if (data.hasSkill("soul_reaper") && data.getSkillMastery("soul_reaper") >= 3) {
            useSoulDrain(player);
        }
    }

    // ── SWAP HANDS = activate move 3 / Divine Form ───────────────────
    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;

        // Grace Move 3 — Enlightened Form (DIVINE FORM)
        if (data.hasSkill("graceful_enlightenment") && data.getSkillMastery("graceful_enlightenment") >= 9) {
            useEnlightenedForm(player);
            return;
        }
        // Storm — Eye of Hurricane
        if (data.hasSkill("storm_caller") && data.getSkillMastery("storm_caller") >= 9) {
            useEyeOfHurricane(player);
            return;
        }
        // Phoenix — Blazing Wings
        if (data.hasSkill("phoenix_rise") && data.getSkillMastery("phoenix_rise") >= 6) {
            useBlazingWings(player);
            return;
        }
        // Storm — Gale Force
        if (data.hasSkill("storm_caller") && data.getSkillMastery("storm_caller") >= 6) {
            useGaleForce(player);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // GRACEFUL ENLIGHTENMENT MOVES
    // ═══════════════════════════════════════════════════════════════

    private void useGraceMove1(Player player) {
        if (onCooldown(player.getUniqueId(), "grace_m1", 30_000)) {
            player.sendMessage(plugin.prefix() + "§cAura of Protection on cooldown! §e" + cdLeft(player.getUniqueId(), "grace_m1", 30_000) + "s");
            return;
        }
        int count = 0;
        for (Entity e : player.getNearbyEntities(10, 10, 10)) {
            if (e instanceof Player ally && ally != player) {
                ally.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 300, 1, false, true));
                ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 300, 1, false, true));
                ally.sendMessage(plugin.prefix() + "§e✨ " + player.getName() + "'s Divine Aura protects you!");
                player.getWorld().spawnParticle(Particle.END_ROD, ally.getLocation().add(0,1,0), 30, 0.5, 0.5, 0.5, 0.05);
                count++;
            }
        }
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);
        player.sendMessage(plugin.prefix() + "§e✨ Aura of Protection! §7Buffed §e" + count + " §7nearby allies!");
    }

    private void useGraceMove2(Player player) {
        if (onCooldown(player.getUniqueId(), "grace_m2", 45_000)) {
            player.sendMessage(plugin.prefix() + "§cCelestial Smite on cooldown! §e" + cdLeft(player.getUniqueId(), "grace_m2", 45_000) + "s");
            return;
        }
        // Find nearest enemy player
        Player target = getNearestPlayer(player, 20);
        if (target == null) {
            player.sendMessage(plugin.prefix() + "§cNo enemy in range for Celestial Smite!");
            return;
        }
        target.damage(30.0, player);
        player.heal(Math.min(10.0, player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue() - player.getHealth()));
        player.getWorld().strikeLightningEffect(target.getLocation());
        player.getWorld().spawnParticle(Particle.END_ROD, target.getLocation(), 100, 1, 2, 1, 0.2);
        player.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2f, 0.5f);
        target.sendMessage(plugin.prefix() + "§c☠ You were struck by §e" + player.getName() + "§c's Celestial Smite!");
        player.sendMessage(plugin.prefix() + "§e⚡ Celestial Smite! §7Hit §c" + target.getName() + " §7for §c30 damage §7and healed §a10 hearts§7!");
    }

    private void useEnlightenedForm(Player player) {
        if (divineFormActive.contains(player.getUniqueId())) {
            player.sendMessage(plugin.prefix() + "§cDivine Form already active!");
            return;
        }
        if (onCooldown(player.getUniqueId(), "grace_m3", 90_000)) {
            player.sendMessage(plugin.prefix() + "§cEnlightened Form on cooldown! §e" + cdLeft(player.getUniqueId(), "grace_m3", 90_000) + "s");
            return;
        }

        divineFormActive.add(player.getUniqueId());
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 400, 255, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 400, 9, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 3, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 4, false, false));
        player.setAllowFlight(true);
        player.setFlying(true);

        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 300, 2, 2, 2, 0.3);
        player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 2f, 0.8f);
        plugin.getServer().broadcastMessage("§e[HeartsSMP] ✨ " + player.getName() + " has entered §6§lENLIGHTENED FORM§e!");
        player.sendMessage(plugin.prefix() + "§e✨ ENLIGHTENED FORM ACTIVATED! §720s — immune, 10x dmg, flight!");

        // Deal aura damage every second
        new BukkitRunnable() {
            int ticks = 0;
            public void run() {
                if (!player.isOnline() || !divineFormActive.contains(player.getUniqueId())) { cancel(); return; }
                ticks++;
                for (Entity e : player.getNearbyEntities(8, 8, 8)) {
                    if (e instanceof LivingEntity le && e != player) {
                        le.damage(5.0, player);
                        player.getWorld().spawnParticle(Particle.FIREWORK, le.getLocation(), 5, 0.3, 0.3, 0.3, 0.1);
                    }
                }
                player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0,1,0), 5, 0.5, 0.5, 0.5, 0.05);
                if (ticks >= 20) {
                    endDivineForm(player);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void endDivineForm(Player player) {
        divineFormActive.remove(player.getUniqueId());
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.REGENERATION);
        if (!player.isOp()) player.setAllowFlight(false);
        player.setFlying(false);
        player.sendMessage(plugin.prefix() + "§7Enlightened Form has ended.");
    }

    private void useAbsoluteGrace(Player player) {
        if (absoluteGraceActive.contains(player.getUniqueId())) {
            player.sendMessage(plugin.prefix() + "§cAbsolute Grace already active!");
            return;
        }
        if (onCooldown(player.getUniqueId(), "grace_ult", 300_000)) {
            player.sendMessage(plugin.prefix() + "§cAbsolute Grace on cooldown! §e" + cdLeft(player.getUniqueId(), "grace_ult", 300_000) + "s");
            return;
        }

        absoluteGraceActive.add(player.getUniqueId());
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 1200, 255, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 1200, 19, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1200, 4, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 1200, 9, false, false));
        player.setAllowFlight(true);
        player.setFlying(true);

        // Darkness + visual for all online players
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (p != player) p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0, false, false));
        }

        plugin.getServer().broadcastMessage("§5§l☠ " + player.getName() + " has unleashed ABSOLUTE GRACE. The world trembles. ☠");
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 500, 3, 3, 3, 0.5);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 0.5f);
        player.sendMessage(plugin.prefix() + "§5☠ ABSOLUTE GRACE — 60s DEITY FORM!");

        new BukkitRunnable() {
            int ticks = 0;
            public void run() {
                if (!player.isOnline()) { cancel(); return; }
                ticks++;
                // 5 hearts/s drain to all enemies in 50 blocks
                for (Entity e : player.getNearbyEntities(50, 50, 50)) {
                    if (e instanceof LivingEntity le && e != player) {
                        le.damage(10.0, player);
                        if (ticks % 5 == 0)
                            player.getWorld().spawnParticle(Particle.PORTAL, le.getLocation(), 10, 0.5, 0.5, 0.5, 0.3);
                    }
                }
                player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0,1,0), 8, 1, 1, 1, 0.1);
                if (ticks >= 60) {
                    absoluteGraceActive.remove(player.getUniqueId());
                    player.removePotionEffect(PotionEffectType.RESISTANCE);
                    player.removePotionEffect(PotionEffectType.STRENGTH);
                    if (!player.isOp()) player.setAllowFlight(false);
                    player.setFlying(false);
                    player.sendMessage(plugin.prefix() + "§7Absolute Grace has ended.");
                    plugin.getServer().broadcastMessage("§7[HeartsSMP] " + player.getName() + "'s Absolute Grace has faded.");
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    // ═══════════════════════════════════════════════════════════════
    // PHOENIX RISE MOVES
    // ═══════════════════════════════════════════════════════════════

    private void useBlazingWings(Player player) {
        if (onCooldown(player.getUniqueId(), "phoenix_wings", 40_000)) {
            player.sendMessage(plugin.prefix() + "§cBlazing Wings on cooldown! §e" + cdLeft(player.getUniqueId(), "phoenix_wings", 40_000) + "s");
            return;
        }
        player.setAllowFlight(true);
        player.setFlying(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0, false, false));
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 60, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 0.7f);
        player.sendMessage(plugin.prefix() + "§6🔥 Blazing Wings! §7Flying for §e8s§7!");
        new BukkitRunnable() {
            public void run() {
                if (player.isOnline()) {
                    if (!player.isOp()) player.setAllowFlight(false);
                    player.setFlying(false);
                    player.sendMessage(plugin.prefix() + "§7Blazing Wings faded.");
                }
            }
        }.runTaskLater(plugin, 160L);
    }

    private void usePhoenixIgnition(Player player) {
        if (onCooldown(player.getUniqueId(), "phoenix_ult", 120_000)) {
            player.sendMessage(plugin.prefix() + "§cPhoenix Ignition on cooldown! §e" + cdLeft(player.getUniqueId(), "phoenix_ult", 120_000) + "s");
            return;
        }
        int killed = 0;
        for (Entity e : player.getNearbyEntities(15, 15, 15)) {
            if (e instanceof LivingEntity le && e != player && e.getFireTicks() > 0) {
                le.damage(1000, player); // instant kill burning enemies
                killed++;
            }
        }
        double maxHp = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(maxHp);
        player.getWorld().createExplosion(player.getLocation(), 0f, false, false);
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 200, 2, 2, 2, 0.3);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, 2f, 0.5f);
        player.sendMessage(plugin.prefix() + "§6☠ Phoenix Ignition! §7Killed §c" + killed + " §7burning enemies, healed to full!");
    }

    // ═══════════════════════════════════════════════════════════════
    // VOID STEP MOVES
    // ═══════════════════════════════════════════════════════════════

    private void useVoidBlink(Player player) {
        if (onCooldown(player.getUniqueId(), "void_blink", 10_000)) {
            player.sendMessage(plugin.prefix() + "§cBlink on cooldown! §e" + cdLeft(player.getUniqueId(), "void_blink", 10_000) + "s");
            return;
        }
        org.bukkit.Location target = player.getLocation().add(player.getLocation().getDirection().multiply(12));
        target.setY(target.getY());
        player.teleport(target);
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 40, 0.5, 0.5, 0.5, 0.3);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f);
        player.sendMessage(plugin.prefix() + "§8🌀 Blink!");
    }

    private void useVoidPhase(Player player) {
        if (onCooldown(player.getUniqueId(), "void_phase", 40_000)) {
            player.sendMessage(plugin.prefix() + "§cPhase Through on cooldown! §e" + cdLeft(player.getUniqueId(), "void_phase", 40_000) + "s");
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2, false, false));
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 50, 0.5, 0.5, 0.5, 0.3);
        player.sendMessage(plugin.prefix() + "§8🌀 Phase Through! §75s — invisible & fast!");
    }

    // ═══════════════════════════════════════════════════════════════
    // STORM CALLER MOVES
    // ═══════════════════════════════════════════════════════════════

    private void useGaleForce(Player player) {
        if (onCooldown(player.getUniqueId(), "storm_gale", 20_000)) {
            player.sendMessage(plugin.prefix() + "§cGale Force on cooldown! §e" + cdLeft(player.getUniqueId(), "storm_gale", 20_000) + "s");
            return;
        }
        for (Entity e : player.getNearbyEntities(10, 10, 10)) {
            if (e instanceof LivingEntity && e != player) {
                Vector v = e.getLocation().subtract(player.getLocation()).toVector().normalize().multiply(3).setY(2.5);
                e.setVelocity(v);
            }
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 2f, 0.5f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 80, 1, 0.5, 1, 0.3);
        player.sendMessage(plugin.prefix() + "§9⛈ Gale Force! §7All nearby enemies launched!");
    }

    private void useEyeOfHurricane(Player player) {
        if (onCooldown(player.getUniqueId(), "storm_eye", 60_000)) {
            player.sendMessage(plugin.prefix() + "§cEye of Hurricane on cooldown! §e" + cdLeft(player.getUniqueId(), "storm_eye", 60_000) + "s");
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2, false, true));
        for (Entity e : player.getNearbyEntities(15, 15, 15)) {
            if (e instanceof Player ally && ally != player) {
                ally.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1, false, true));
                ally.sendMessage(plugin.prefix() + "§9⛈ " + player.getName() + "'s hurricane eye protects you!");
            }
        }
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 100, 1, 1, 1, 0.2);
        player.sendMessage(plugin.prefix() + "§9⛈ Eye of the Hurricane! §715s safe zone!");
    }

    private void useApocalypseStorm(Player player) {
        if (onCooldown(player.getUniqueId(), "storm_ult", 180_000)) {
            player.sendMessage(plugin.prefix() + "§cApocalypse Storm on cooldown! §e" + cdLeft(player.getUniqueId(), "storm_ult", 180_000) + "s");
            return;
        }
        plugin.getServer().broadcastMessage("§9[HeartsSMP] ⛈ " + player.getName() + " has unleashed the APOCALYPSE STORM!");
        player.getWorld().setStorm(true);
        player.getWorld().setThundering(true);

        new BukkitRunnable() {
            int ticks = 0;
            public void run() {
                if (!player.isOnline()) { cancel(); return; }
                ticks++;
                for (Entity e : player.getNearbyEntities(20, 20, 20)) {
                    if (e instanceof LivingEntity le && e != player) {
                        if (ticks % 3 == 0) player.getWorld().strikeLightning(e.getLocation());
                        Vector v = e.getLocation().subtract(player.getLocation()).toVector().normalize().multiply(2).setY(1.5);
                        e.setVelocity(v);
                    }
                }
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 30, 2, 1, 2, 0.3);
                if (ticks >= 12) { cancel(); player.sendMessage(plugin.prefix() + "§7Apocalypse Storm faded."); }
            }
        }.runTaskTimer(plugin, 0L, 20L);
        player.sendMessage(plugin.prefix() + "§1☠ APOCALYPSE STORM! §712s of devastation!");
    }

    // ═══════════════════════════════════════════════════════════════
    // DRAGONSCALE MOVES
    // ═══════════════════════════════════════════════════════════════

    private void useDragonHarden(Player player) {
        if (onCooldown(player.getUniqueId(), "dragon_harden", 30_000)) {
            player.sendMessage(plugin.prefix() + "§cScale Harden on cooldown! §e" + cdLeft(player.getUniqueId(), "dragon_harden", 30_000) + "s");
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 4, false, true));
        player.getWorld().spawnParticle(Particle.DRAGON_BREATH, player.getLocation(), 60, 0.5, 0.5, 0.5, 0.05);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1f, 0.5f);
        player.sendMessage(plugin.prefix() + "§2🐉 Scale Harden! §760% damage reduction for §e8s§7!");
    }

    // ═══════════════════════════════════════════════════════════════
    // SOUL REAPER MOVES
    // ═══════════════════════════════════════════════════════════════

    private void useSoulDrain(Player player) {
        if (onCooldown(player.getUniqueId(), "soul_drain", 20_000)) {
            player.sendMessage(plugin.prefix() + "§cSoul Drain on cooldown! §e" + cdLeft(player.getUniqueId(), "soul_drain", 20_000) + "s");
            return;
        }
        Player target = getNearestPlayer(player, 15);
        if (target == null) { player.sendMessage(plugin.prefix() + "§cNo enemy in range!"); return; }
        double drain = Math.min(6.0, target.getHealth());
        target.damage(drain, player);
        double maxHp = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHp, player.getHealth() + drain));
        player.getWorld().spawnParticle(Particle.SOUL, target.getLocation(), 40, 0.5, 1, 0.5, 0.05);
        player.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1f, 0.5f);
        player.sendMessage(plugin.prefix() + "§8💀 Soul Drain! §7Stole §c3 hearts §7from §e" + target.getName() + "§7!");
    }

    // ═══════════════════════════════════════════════════════════════
    // EARTH SHATTER
    // ═══════════════════════════════════════════════════════════════

    private void useGroundSlam(Player player) {
        if (onCooldown(player.getUniqueId(), "earth_slam", 20_000)) {
            player.sendMessage(plugin.prefix() + "§cGround Slam on cooldown! §e" + cdLeft(player.getUniqueId(), "earth_slam", 20_000) + "s");
            return;
        }
        player.getWorld().createExplosion(player.getLocation(), 0f, false, false);
        player.getWorld().spawnParticle(Particle.BLOCK, player.getLocation(), 150, 1, 0.1, 1, 0.5, Material.STONE.createBlockData());
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.5f);
        for (Entity e : player.getNearbyEntities(6, 6, 6)) {
            if (e instanceof LivingEntity le && e != player) {
                le.damage(10.0, player);
                Vector v = new Vector(0, 2.5, 0);
                le.setVelocity(v);
            }
        }
        player.sendMessage(plugin.prefix() + "§8🪨 Ground Slam! §7Enemies launched!");
    }

    // ═══════════════════════════════════════════════════════════════
    // UTIL
    // ═══════════════════════════════════════════════════════════════

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

    public boolean isDivineFormActive(UUID uuid) { return divineFormActive.contains(uuid); }
    public boolean isAbsoluteGraceActive(UUID uuid) { return absoluteGraceActive.contains(uuid); }
}
