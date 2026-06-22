package com.heartssmp.listeners;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
    private final Set<UUID> starStormActive = new HashSet<>();

    public ItemAbilityListener(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean onCooldown(UUID uuid, String item, long ms) {
        long now = System.currentTimeMillis();
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>());
        if (now - cooldowns.get(uuid).getOrDefault(item, 0L) < ms) return true;
        cooldowns.get(uuid).put(item, now);
        return false;
    }

    private long getCooldownLeft(UUID uuid, String item, long ms) {
        long now = System.currentTimeMillis();
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>());
        return Math.max(0, (ms - (now - cooldowns.get(uuid).getOrDefault(item, 0L))) / 1000);
    }

    private String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return null;
        return switch (meta.getDisplayName()) {
            case "Chrono Watch"        -> "chrono_watch";
            case "Void Cloak"          -> "void_cloak";
            case "Shadow Cloak"        -> "shadow_cloak";
            case "Storm Staff"         -> "storm_staff";
            case "§6§lCelestial Blade" -> "celestial_blade";
            case "Star Fragment"       -> "star_fragment";
            case "Titan Hammer"        -> "titan_hammer";
            case "Aurora Staff"        -> "aurora_staff";
            case "Hellcore Fragment"   -> "hell_core";
            case "Heart Shard"         -> "heart_shard";
            case "Life Crystal"        -> "life_crystal";
            case "Celestia Dust"       -> "celestia_dust";
            default -> null;
        };
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
         && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        String id = getItemId(item);
        if (id == null) return;

        switch (id) {
            case "chrono_watch"    -> useChronoWatch(player);
            case "storm_staff"     -> useStormStaff(player);
            case "titan_hammer"    -> useTitanHammer(player);
            case "star_fragment"   -> useStarFragment(player);
            case "aurora_staff"    -> useAuroraStaff(player);
            case "hell_core"       -> useHellCore(player);
            case "heart_shard"     -> useHeartShard(player, item);
            case "life_crystal"    -> useLifeCrystal(player, item);
            case "celestia_dust"   -> useCelestiaDust(player, item);
            case "celestial_blade" -> {
                if (player.isSneaking()) useCelestialBladeStarStorm(player);
                else useCelestialBladeSmash(player);
            }
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
            case "void_cloak"      -> useVoidCloak(player);
            case "shadow_cloak"    -> useShadowCloak(player);
            case "celestial_blade" -> useCelestialBladeNovaBurst(player);
        }
    }

    // ── Celestial Blade hit passive (fires on every enemy hit) ────────────────
    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!"celestial_blade".equals(getItemId(held))) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        World world = player.getWorld();

        // Passive 1 — ignite
        target.setFireTicks(60);

        // Passive 2 — knockback + brief launch
        Vector kb = target.getLocation().subtract(player.getLocation())
                .toVector().normalize().multiply(1.8).setY(0.5);
        target.setVelocity(kb);

        // Passive 3 — mini starburst particle on every hit
        world.spawnParticle(Particle.END_ROD,   target.getLocation().add(0, 1, 0), 12, 0.4, 0.4, 0.4, 0.08);
        world.spawnParticle(Particle.FIREWORK,  target.getLocation().add(0, 1, 0),  8, 0.3, 0.3, 0.3, 0.1);
        world.spawnParticle(Particle.FLAME,     target.getLocation().add(0, 1, 0),  6, 0.2, 0.2, 0.2, 0.05);
        world.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.6f, 1.8f);

        // Passive 4 — 20% chance extra lightning bolt on hit
        if (Math.random() < 0.20) {
            world.strikeLightningEffect(target.getLocation());
            target.damage(4.0, player);
        }
    }

    // ── CELESTIAL BLADE — Ability 1: Celestial Smash (right click) ────────────
    private void useCelestialBladeSmash(Player player) {
        if (onCooldown(player.getUniqueId(), "cb_smash", 8_000)) {
            player.sendMessage(plugin.prefix() + "§6Celestial Smash §ccooldown! §e"
                + getCooldownLeft(player.getUniqueId(), "cb_smash", 8_000) + "s");
            return;
        }
        World world = player.getWorld();
        Location center = player.getLocation();

        // Central explosion (no block damage)
        world.createExplosion(center, 0f, false, false);

        // Expanding starburst particle ring
        for (int ring = 1; ring <= 3; ring++) {
            final int r = ring;
            new BukkitRunnable() {
                public void run() {
                    for (int i = 0; i < 16; i++) {
                        double angle = (2 * Math.PI / 16) * i;
                        Location loc = center.clone().add(
                            Math.cos(angle) * r * 2, 0.5, Math.sin(angle) * r * 2);
                        world.spawnParticle(Particle.END_ROD, loc, 3, 0.1, 0.2, 0.1, 0.05);
                        world.spawnParticle(Particle.FIREWORK, loc, 2, 0.1, 0.2, 0.1, 0.1);
                    }
                }
            }.runTaskLater(plugin, r * 3L);
        }

        // Damage + launch all nearby entities
        int hit = 0;
        for (Entity e : player.getNearbyEntities(8, 8, 8)) {
            if (e instanceof LivingEntity le && e != player) {
                le.damage(12.0, player);
                le.setFireTicks(80);
                Vector v = le.getLocation().subtract(center).toVector().normalize().multiply(2.5).setY(1.0);
                le.setVelocity(v);
                world.strikeLightningEffect(le.getLocation());
                hit++;
            }
        }

        world.spawnParticle(Particle.EXPLOSION, center, 5, 1, 1, 1, 0.5);
        world.spawnParticle(Particle.FIREWORK,  center, 80, 2, 2, 2, 0.4);
        world.spawnParticle(Particle.END_ROD,   center, 60, 1, 1, 1, 0.2);
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.6f);
        world.playSound(center, Sound.ENTITY_WITHER_SHOOT,    1.0f, 0.8f);
        player.sendMessage(plugin.prefix() + "§6§l⚔ Celestial Smash! §7Hit §e" + hit + " §7entities!");
    }

    // ── CELESTIAL BLADE — Ability 2: Star Storm (shift + right click) ──────────
    private void useCelestialBladeStarStorm(Player player) {
        if (starStormActive.contains(player.getUniqueId())) {
            player.sendMessage(plugin.prefix() + "§6Star Storm §7is already active!");
            return;
        }
        if (onCooldown(player.getUniqueId(), "cb_star_storm", 25_000)) {
            player.sendMessage(plugin.prefix() + "§6Star Storm §ccooldown! §e"
                + getCooldownLeft(player.getUniqueId(), "cb_star_storm", 25_000) + "s");
            return;
        }

        starStormActive.add(player.getUniqueId());
        player.sendMessage(plugin.prefix() + "§6§l★ Star Storm! §7Raining lightning for §e5s§7!");
        plugin.getServer().broadcastMessage("§6✦ " + player.getName() + " unleashes the §6§lStar Storm§6✦!");

        new BukkitRunnable() {
            int ticks = 0;
            public void run() {
                if (!player.isOnline() || ticks >= 10) {
                    starStormActive.remove(player.getUniqueId());
                    if (player.isOnline()) player.sendMessage(plugin.prefix() + "§7Star Storm ended.");
                    cancel();
                    return;
                }
                ticks++;
                World world = player.getWorld();

                // Strike 4 random nearby enemies or locations
                List<Entity> nearby = new ArrayList<>(player.getNearbyEntities(20, 20, 20));
                int strikes = Math.min(4, nearby.size());
                for (int i = 0; i < strikes; i++) {
                    Entity target = nearby.get((int)(Math.random() * nearby.size()));
                    if (target instanceof LivingEntity le && target != player) {
                        world.strikeLightning(target.getLocation());
                        le.damage(6.0, player);
                    }
                }

                // Also strike 2 random locations near player
                for (int i = 0; i < 2; i++) {
                    double x = player.getLocation().getX() + (Math.random() - 0.5) * 20;
                    double z = player.getLocation().getZ() + (Math.random() - 0.5) * 20;
                    Location loc = new Location(world, x,
                        world.getHighestBlockYAt((int)x, (int)z), z);
                    world.strikeLightningEffect(loc);
                }

                // Particle trail on player
                world.spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.15);
                world.spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    // ── CELESTIAL BLADE — Ability 3: Nova Burst (sneak) ─────────────────────
    private void useCelestialBladeNovaBurst(Player player) {
        if (onCooldown(player.getUniqueId(), "cb_nova", 40_000)) {
            player.sendMessage(plugin.prefix() + "§6Nova Burst §ccooldown! §e"
                + getCooldownLeft(player.getUniqueId(), "cb_nova", 40_000) + "s");
            return;
        }

        World world = player.getWorld();
        Location center = player.getLocation();

        plugin.getServer().broadcastMessage("§6§l⭐ " + player.getName() + " triggers a NOVA BURST! ⭐");

        // Shockwave: kill/damage everything in a big radius
        for (Entity e : player.getNearbyEntities(15, 15, 15)) {
            if (e instanceof LivingEntity le && e != player) {
                le.damage(20.0, player);
                le.setFireTicks(120);
                Vector v = le.getLocation().subtract(center).toVector().normalize().multiply(3).setY(1.5);
                le.setVelocity(v);
                world.strikeLightningEffect(le.getLocation());
            }
        }

        // Give player brief immunity so they don't get hit by their own blast
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 255, false, false));

        // Multi-wave supernova particle expansion
        for (int wave = 1; wave <= 5; wave++) {
            final int w = wave;
            new BukkitRunnable() {
                public void run() {
                    double radius = w * 3;
                    int points = 24;
                    for (int i = 0; i < points; i++) {
                        double angle = (2 * Math.PI / points) * i;
                        Location loc = center.clone().add(
                            Math.cos(angle) * radius, 0.5, Math.sin(angle) * radius);
                        world.spawnParticle(Particle.END_ROD,   loc, 5, 0.2, 0.3, 0.2, 0.08);
                        world.spawnParticle(Particle.FIREWORK,  loc, 4, 0.2, 0.2, 0.2, 0.15);
                        world.spawnParticle(Particle.FLAME,     loc, 3, 0.2, 0.2, 0.2, 0.05);
                    }
                    world.createExplosion(center.clone().add(
                        (Math.random() - 0.5) * radius,
                        0,
                        (Math.random() - 0.5) * radius), 0f, false, false);
                    world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.4f + (w * 0.1f));
                }
            }.runTaskLater(plugin, wave * 5L);
        }

        world.spawnParticle(Particle.EXPLOSION, center, 10, 2, 2, 2, 0.5);
        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 0.5f);
        world.playSound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2f, 0.6f);
        player.sendMessage(plugin.prefix() + "§6§l⭐ NOVA BURST! §7Everything in 15 blocks obliterated!");
    }

    // ── All other existing item abilities (unchanged) ─────────────────────────

    private void useChronoWatch(Player player) {
        if (onCooldown(player.getUniqueId(), "chrono_watch", 30_000)) {
            player.sendMessage(plugin.prefix() + "§cChrono Watch on cooldown! §e" + getCooldownLeft(player.getUniqueId(), "chrono_watch", 30_000) + "s left"); return;
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

    private void useVoidCloak(Player player) {
        if (onCooldown(player.getUniqueId(), "void_cloak", 120_000)) {
            player.sendMessage(plugin.prefix() + "§cVoid Cloak on cooldown! §e" + getCooldownLeft(player.getUniqueId(), "void_cloak", 120_000) + "s left"); return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 255, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, false, false));
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 80, 0.5, 1, 0.5, 0.3);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
        player.sendMessage(plugin.prefix() + "§5🌀 Void Cloak activated! §75s invincibility!");
        new BukkitRunnable() { public void run() {
            if (player.isOnline()) {
                player.removePotionEffect(PotionEffectType.RESISTANCE);
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                player.sendMessage(plugin.prefix() + "§7Void Cloak worn off.");
            }
        }}.runTaskLater(plugin, 100L);
    }

    private void useShadowCloak(Player player) {
        if (onCooldown(player.getUniqueId(), "shadow_cloak", 20_000)) {
            player.sendMessage(plugin.prefix() + "§cShadow Cloak on cooldown! §e" + getCooldownLeft(player.getUniqueId(), "shadow_cloak", 20_000) + "s left"); return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, false, false));
        player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation(), 40, 0.3, 0.5, 0.3, 0.05);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 0.5f, 1.5f);
        player.sendMessage(plugin.prefix() + "§8Shadow Cloak! §7Invisible for §e5s§7!");
        new BukkitRunnable() { public void run() {
            if (player.isOnline()) player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }}.runTaskLater(plugin, 100L);
    }

    private void useStormStaff(Player player) {
        if (onCooldown(player.getUniqueId(), "storm_staff", 15_000)) {
            player.sendMessage(plugin.prefix() + "§cStorm Staff on cooldown! §e" + getCooldownLeft(player.getUniqueId(), "storm_staff", 15_000) + "s left"); return;
        }
        for (Entity e : player.getNearbyEntities(10, 10, 10)) {
            if (e instanceof LivingEntity && e != player) player.getWorld().strikeLightning(e.getLocation());
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
        player.sendMessage(plugin.prefix() + "§9⚡ Storm Staff! §7Lightning strikes all nearby enemies!");
    }

    private void useTitanHammer(Player player) {
        if (onCooldown(player.getUniqueId(), "titan_hammer", 20_000)) {
            player.sendMessage(plugin.prefix() + "§cTitan Hammer on cooldown! §e" + getCooldownLeft(player.getUniqueId(), "titan_hammer", 20_000) + "s left"); return;
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

    private void useStarFragment(Player player) {
        if (onCooldown(player.getUniqueId(), "star_fragment", 60_000)) {
            player.sendMessage(plugin.prefix() + "§cStar Fragment on cooldown! §e" + getCooldownLeft(player.getUniqueId(), "star_fragment", 60_000) + "s left"); return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,   200, 2, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,       200, 2, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,  200, 3, false, true));
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0,1,0), 100, 1, 1, 1, 0.2);
        player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.5f);
        player.sendMessage(plugin.prefix() + "§e⭐ Star Fragment! §7Strength III, Speed III & Absorption IV for 10s!");
    }

    private void useAuroraStaff(Player player) {
        if (onCooldown(player.getUniqueId(), "aurora_staff", 25_000)) {
            player.sendMessage(plugin.prefix() + "§cAurora Staff on cooldown! §e" + getCooldownLeft(player.getUniqueId(), "aurora_staff", 25_000) + "s left"); return;
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

    private void useHellCore(Player player) {
        if (onCooldown(player.getUniqueId(), "hell_core", 25_000)) {
            player.sendMessage(plugin.prefix() + "§cHellcore on cooldown! §e" + getCooldownLeft(player.getUniqueId(), "hell_core", 25_000) + "s left"); return;
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

    private void useHeartShard(Player player, ItemStack item) {
        plugin.getHeartManager().addHearts(player, 1);
        item.setAmount(item.getAmount() - 1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0,1,0), 15, 0.5, 0.5, 0.5, 0.1);
        player.sendMessage(plugin.prefix() + "§c❤ +1 Heart from Heart Shard!");
    }

    private void useLifeCrystal(Player player, ItemStack item) {
        plugin.getLivesManager().addLives(player, 1);
        item.setAmount(item.getAmount() - 1);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.2f);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0,1,0), 20, 0.5, 0.5, 0.5, 0.05);
        player.sendMessage(plugin.prefix() + "§a♥ +1 Life from Life Crystal!");
    }

    private void useCelestiaDust(Player player, ItemStack item) {
        if (onCooldown(player.getUniqueId(), "celestia_dust", 120_000)) {
            player.sendMessage(plugin.prefix() + "§cCelestia Dust on cooldown! §e" + getCooldownLeft(player.getUniqueId(), "celestia_dust", 120_000) + "s left"); return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,    300, 2, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,       300, 2, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,  300, 1, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,300, 1, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,  300, 3, false, true));
        item.setAmount(item.getAmount() - 1);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0,1,0), 80, 0.8, 0.8, 0.8, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.8f);
        player.sendMessage(plugin.prefix() + "§e✨ Celestia Dust! §7All stats boosted for §e15s§7!");
    }
}
