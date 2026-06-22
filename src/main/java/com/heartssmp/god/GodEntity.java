package com.heartssmp.god;

import com.heartssmp.HeartsSMPPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

import java.util.*;

public class GodEntity {

    public enum GodForm {
        GUIDE,
        POWER_25,
        POWER_50,
        POWER_75
    }

    private final HeartsSMPPlugin plugin;
    private ArmorStand stand;
    private Location location;
    private GodForm form;
    private final UUID summoner;
    private boolean active = false;
    private int taskId = -1;
    private long spawnTime;

    private final Map<UUID, Long> lastRequest = new HashMap<>();

    private static final String GOD_TEXTURE =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3" +
        "RleHR1cmUvNzg2YzdiYTFhNTNmNTc1MWE2YzM1YjNiYjQyNzliZjFiNzI4YzI1N2RhY2RmYTA1ZDlm" +
        "YzIwZmJiMWRhMSJ9fX0=";

    private static final Map<GodForm, Integer> ITEM_LIMITS = Map.of(
        GodForm.POWER_25, 16,
        GodForm.POWER_50, 64,
        GodForm.POWER_75, 1728
    );

    public GodEntity(HeartsSMPPlugin plugin, Location location, GodForm form, UUID summoner) {
        this.plugin = plugin;
        this.location = location;
        this.form = form;
        this.summoner = summoner;
    }

    public void spawn() {
        despawn();
        stand = location.getWorld().spawn(location, ArmorStand.class, as -> {
            as.setCustomName(getGodTitle());
            as.setCustomNameVisible(true);
            as.setGravity(false);
            as.setVisible(true);
            as.setInvulnerable(true);
            as.setSmall(false);
            as.setBasePlate(false);
            as.setArms(true);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            PlayerProfile profile = plugin.getServer().createProfile(UUID.randomUUID(), "God");
            profile.setProperty(new ProfileProperty("textures", GOD_TEXTURE));
            skullMeta.setPlayerProfile(profile);
            head.setItemMeta(skullMeta);

            ItemStack chest = new ItemStack(Material.GOLDEN_CHESTPLATE);
            ItemStack legs  = new ItemStack(Material.GOLDEN_LEGGINGS);
            ItemStack boots = new ItemStack(Material.GOLDEN_BOOTS);
            ItemStack sword = new ItemStack(Material.GOLDEN_SWORD);
            chest.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 10);
            legs.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 10);
            boots.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 10);
            sword.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.SHARPNESS, 10);

            as.getEquipment().setHelmet(head);
            as.getEquipment().setChestplate(chest);
            as.getEquipment().setLeggings(legs);
            as.getEquipment().setBoots(boots);
            as.getEquipment().setItemInMainHand(sword);
        });

        active = true;
        spawnTime = System.currentTimeMillis();
        broadcastSpawn();
        startFloatingAnimation();
        if (form != GodForm.GUIDE) startSessionTimer();
    }

    private String getGodTitle() {
        return switch (form) {
            case GUIDE    -> "§e✦ §6§lThe Divine§e ✦ §7[Guide]";
            case POWER_25 -> "§e✦ §6§lGod §e✦ §7[25% Power]";
            case POWER_50 -> "§e✦ §6§lGod §e✦ §c[50% Power]";
            case POWER_75 -> "§e✦ §6§l§kX§r §6§lGOD §e✦ §4[75% Power]§e ✦ §k§6X";
        };
    }

    private void broadcastSpawn() {
        switch (form) {
            case GUIDE -> {
                plugin.getServer().broadcastMessage("");
                plugin.getServer().broadcastMessage("§e§l✦ ═══════════════════════════════ ✦");
                plugin.getServer().broadcastMessage("§6§l          THE DIVINE APPEARS          ");
                plugin.getServer().broadcastMessage("§7      The God has come to guide you...");
                plugin.getServer().broadcastMessage("§e§l✦ ═══════════════════════════════ ✦");
                plugin.getServer().broadcastMessage("");
            }
            case POWER_25 -> {
                plugin.getServer().broadcastMessage("");
                plugin.getServer().broadcastMessage("§e§l✦ ═══════════════════════════════════ ✦");
                plugin.getServer().broadcastMessage("§6§l       GOD HAS BEEN SUMMONED [25%]       ");
                plugin.getServer().broadcastMessage("§7   The Divine awakens at a fraction of power...");
                plugin.getServer().broadcastMessage("§e   You have §a15 minutes §eto speak with God.");
                plugin.getServer().broadcastMessage("§7   Type §eyour wish §7in chat to ask God.");
                plugin.getServer().broadcastMessage("§e§l✦ ═══════════════════════════════════ ✦");
                plugin.getServer().broadcastMessage("");
                new BukkitRunnable() { public void run() {
                    if (!active) return;
                    godSpeak("§6✦ God: §e*manifests in a divine glow* I am here, mortal. 25% of my true power. You have §a15 minutes.");
                }}.runTaskLater(plugin, 60L);
            }
            case POWER_50 -> {
                plugin.getServer().broadcastMessage("");
                plugin.getServer().broadcastMessage("§c§l✦ ═══════════════════════════════════ ✦");
                plugin.getServer().broadcastMessage("§6§l       GOD HAS BEEN SUMMONED [50%]       ");
                plugin.getServer().broadcastMessage("§c   The air trembles. God grows stronger...");
                plugin.getServer().broadcastMessage("§e   You have §a15 minutes §eto speak with God.");
                plugin.getServer().broadcastMessage("§c§l✦ ═══════════════════════════════════ ✦");
                plugin.getServer().broadcastMessage("");
                new BukkitRunnable() { public void run() {
                    if (!active) return;
                    godSpeak("§6✦ God: §c*the ground shakes* I return at §c50% §cof my true might. Speak — what do you seek?");
                }}.runTaskLater(plugin, 60L);
            }
            case POWER_75 -> {
                plugin.getServer().broadcastMessage("");
                plugin.getServer().broadcastMessage("§4§l✦ ═══════════════════════════════════════════ ✦");
                plugin.getServer().broadcastMessage("§6§k§lXX§r §4§l    !! GOD — FINAL SUMMON [75%] !!    §6§k§lXX");
                plugin.getServer().broadcastMessage("§4   THE DIVINE REACHES 75% POWER. TREMBLE.");
                plugin.getServer().broadcastMessage("§e   You have §a15 minutes §ewith God. Final audience.");
                plugin.getServer().broadcastMessage("§4§l✦ ═══════════════════════════════════════════ ✦");
                plugin.getServer().broadcastMessage("");
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 0));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 80, 0));
                }
                new BukkitRunnable() { public void run() {
                    if (!active) return;
                    godSpeak("§6✦ God: §4*reality fractures* §675% of my infinite power. Final audience. Ask. Your. Question.");
                }}.runTaskLater(plugin, 100L);
            }
        }
    }

    private void startFloatingAnimation() {
        final double[] yOffset = {0};
        final boolean[] goingUp = {true};
        final float[] yaw = {0};

        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!active || stand == null || stand.isDead()) return;
            if (goingUp[0]) { yOffset[0] += 0.03; if (yOffset[0] >= 0.5) goingUp[0] = false; }
            else             { yOffset[0] -= 0.03; if (yOffset[0] <= 0)   goingUp[0] = true;  }
            yaw[0] = (yaw[0] + 3) % 360;
            Location newLoc = location.clone().add(0, yOffset[0] + 1.5, 0);
            newLoc.setYaw(yaw[0]);
            stand.teleport(newLoc);
            switch (form) {
                case GUIDE    -> location.getWorld().spawnParticle(Particle.END_ROD, newLoc.add(0,1,0), 3, 0.5, 0.3, 0.5, 0.02);
                case POWER_25 -> {
                    location.getWorld().spawnParticle(Particle.END_ROD, newLoc.add(0,1,0), 4, 0.6, 0.4, 0.6, 0.02);
                    location.getWorld().spawnParticle(Particle.FIREWORK, newLoc, 2, 0.4, 0.4, 0.4, 0.05);
                }
                case POWER_50 -> {
                    location.getWorld().spawnParticle(Particle.END_ROD, newLoc.add(0,1,0), 6, 0.8, 0.5, 0.8, 0.03);
                    location.getWorld().spawnParticle(Particle.FLAME, newLoc, 3, 0.3, 0.3, 0.3, 0.02);
                    location.getWorld().spawnParticle(Particle.FIREWORK, newLoc, 3, 0.5, 0.5, 0.5, 0.05);
                }
                case POWER_75 -> {
                    location.getWorld().spawnParticle(Particle.END_ROD, newLoc.add(0,1,0), 10, 1, 0.6, 1, 0.04);
                    location.getWorld().spawnParticle(Particle.FLAME, newLoc, 5, 0.5, 0.4, 0.5, 0.03);
                    location.getWorld().spawnParticle(Particle.PORTAL, newLoc, 8, 0.8, 0.8, 0.8, 0.1);
                    location.getWorld().spawnParticle(Particle.DRAGON_BREATH, newLoc, 3, 0.3, 0.3, 0.3, 0.02);
                    if (Math.random() < 0.05) location.getWorld().strikeLightningEffect(newLoc);
                }
            }
        }, 0L, 2L);
    }

    private void startSessionTimer() {
        new BukkitRunnable() { public void run() {
            if (!active) { cancel(); return; }
            godSpeak("§6✦ God: §e*glances at the divine hourglass* §710 minutes remain.");
        }}.runTaskLater(plugin, 5 * 60 * 20L);
        new BukkitRunnable() { public void run() {
            if (!active) { cancel(); return; }
            godSpeak("§6✦ God: §c5 minutes remain. Choose your final requests wisely.");
        }}.runTaskLater(plugin, 10 * 60 * 20L);
        new BukkitRunnable() { public void run() {
            if (!active) { cancel(); return; }
            godSpeak("§6✦ God: §4§lOne minute. Speak now or hold your peace for eternity.");
        }}.runTaskLater(plugin, 14 * 60 * 20L);
        new BukkitRunnable() { public void run() {
            if (!active) { cancel(); return; }
            if (form == GodForm.POWER_75) endFinalAudience();
            else {
                godSpeak("§6✦ God: §7*fades into divine light* Our time is done. Farewell, mortal.");
                new BukkitRunnable() { public void run() { despawn(); }}.runTaskLater(plugin, 60L);
            }
        }}.runTaskLater(plugin, 15 * 60 * 20L);
    }

    private void endFinalAudience() {
        Player summonerPlayer = plugin.getServer().getPlayer(summoner);
        plugin.getServer().broadcastMessage("");
        plugin.getServer().broadcastMessage("§4§l✦ ═══════════════════════════════════════════ ✦");
        plugin.getServer().broadcastMessage("§6§l     THE FINAL AUDIENCE HAS ENDED     ");
        plugin.getServer().broadcastMessage("§4   God takes back what was given...");
        plugin.getServer().broadcastMessage("§4§l✦ ═══════════════════════════════════════════ ✦");
        plugin.getServer().broadcastMessage("");
        godSpeak("§6✦ God: §4§l*the sky darkens* The divine debt is due. §6The world... is mine now.");
        new BukkitRunnable() { public void run() {
            if (summonerPlayer != null && summonerPlayer.isOnline()) {
                com.heartssmp.data.PlayerData data = plugin.getDataManager().get(summonerPlayer.getUniqueId());
                if (data != null) {
                    data.setLives(1);
                    data.setHearts(plugin.getConfig().getInt("hearts.starting", 10));
                    plugin.getDataManager().save(summonerPlayer.getUniqueId());
                    summonerPlayer.sendMessage("§4§l☠ God has stripped you to your last life. Survive what comes next...");
                }
            }
            plugin.getDivineWorldManager().activateDivineWorld();
            despawn();
        }}.runTaskLater(plugin, 100L);
    }

    // ── Chat request handler ─────────────────────────────────────────────────

    public void handleChatRequest(Player player, String message) {
        if (!active || form == GodForm.GUIDE) return;
        long now = System.currentTimeMillis();
        if (now - lastRequest.getOrDefault(player.getUniqueId(), 0L) < 10_000) {
            player.sendMessage("§7§o[God listens but does not speak twice so quickly...]");
            return;
        }
        lastRequest.put(player.getUniqueId(), now);
        String lower = message.toLowerCase();

        // ── Heal ────────────────────────────────────────────────────
        if (lower.contains("heal") || lower.contains("health")) {
            double maxHp = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(maxHp);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 4));
            spawnGodBlessing(player);
            godSpeak("§6✦ God: §e*places a divine hand upon " + player.getName() + "* §aHealed.");

        // ── Strength ─────────────────────────────────────────────────
        } else if (lower.contains("strength") || lower.contains("power") || lower.contains("strong")) {
            int tier = form == GodForm.POWER_75 ? 4 : form == GodForm.POWER_50 ? 2 : 1;
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 60 * 5, tier));
            spawnGodBlessing(player);
            godSpeak("§6✦ God: §e*channels divine energy* Strength §e" + (tier + 1) + " §eflows through your veins.");

        // ── Speed ─────────────────────────────────────────────────────
        } else if (lower.contains("speed") || lower.contains("fast")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 5, 3));
            spawnGodBlessing(player);
            godSpeak("§6✦ God: §e*snaps fingers* You move like divine wind now, " + player.getName() + ".");

        // ── Hearts / Lives ────────────────────────────────────────────
        } else if (lower.contains("heart") || lower.contains("life") || lower.contains("lives")) {
            com.heartssmp.data.PlayerData data = plugin.getDataManager().get(player.getUniqueId());
            if (data != null) {
                int bonus = form == GodForm.POWER_75 ? 3 : form == GodForm.POWER_50 ? 2 : 1;
                plugin.getHeartManager().addHearts(player, bonus);
                spawnGodBlessing(player);
                godSpeak("§6✦ God: §e*breathes life* §c+" + bonus + " hearts§e granted.");
            }

        // ── Celestia Gem ──────────────────────────────────────────────
        } else if (lower.contains("gem") || lower.contains("celestia")) {
            if (form == GodForm.POWER_75) {
                com.heartssmp.data.PlayerData data = plugin.getDataManager().get(player.getUniqueId());
                if (data != null) {
                    data.setGemId("DIVINE_CELESTIA");
                    data.setGemMastery(3);
                    plugin.getDataManager().save(player.getUniqueId());
                    plugin.getGemManager().giveGemItem(player, "DIVINE_CELESTIA");
                    spawnGodBlessing(player);
                    godSpeak("§6✦ God: §e*tears a fragment of divinity* The §6Celestia Gem§e at full mastery. §c§lOnly because you asked at my peak power.");
                }
            } else {
                godSpeak("§6✦ God: §7That power is beyond " + (form == GodForm.POWER_25 ? "25" : "50") + "%. Summon me at full power.");
            }

        // ── Divine Skill ──────────────────────────────────────────────
        } else if (lower.contains("skill") || lower.contains("grace") || lower.contains("enlighten")) {
            if (form == GodForm.POWER_50 || form == GodForm.POWER_75) {
                plugin.getSkillManager().grantDivineSkill(player);
                com.heartssmp.data.PlayerData data = plugin.getDataManager().get(player.getUniqueId());
                if (data != null) data.maxSkillMastery("graceful_enlightenment");
                spawnGodBlessing(player);
                godSpeak("§6✦ God: §e*sighs* Very well. §6Graceful Enlightenment§e — use it with grace.");
            } else {
                godSpeak("§6✦ God: §7At 25% power I cannot grant divine skills. Summon me again.");
            }

        // ── Celestial Blade ──────────────────────────────────────────
        } else if (lower.contains("blade") || lower.contains("celestial blade") || lower.contains("sword") || lower.contains("weapon")) {
            if (form == GodForm.POWER_50 || form == GodForm.POWER_75) {
                giveCelestialBlade(player);
                spawnGodBlessing(player);
                godSpeak("§6✦ God: §e*forges a blade from stardust and divine fire* The §6§lCelestial Blade§e. It was mine once. Do not dishonour it.");
            } else {
                godSpeak("§6✦ God: §7A weapon of that magnitude requires 50% power at minimum. Summon me stronger.");
            }

        // ── Kill all nearby entities ──────────────────────────────────
        } else if (lower.contains("kill") && (lower.contains("all") || lower.contains("nearby") || lower.contains("everyone") || lower.contains("entity") || lower.contains("mob"))) {
            if (form == GodForm.POWER_50 || form == GodForm.POWER_75) {
                int killed = godKillNearby(player, form == GodForm.POWER_75 ? 60 : 30);
                spawnGodBlessing(player);
                godSpeak("§6✦ God: §c*raises one hand* §eDone. §e" + killed + " §7entities have ceased to exist.");
            } else {
                godSpeak("§6✦ God: §7Mass destruction requires 50% of my power. You are not there yet.");
            }

        // ── Smite single nearest player ───────────────────────────────
        } else if (lower.contains("kill") || lower.contains("smite") || lower.contains("destroy")) {
            Player nearest = getNearestOtherPlayer(player, 30);
            if (nearest != null) {
                location.getWorld().strikeLightning(nearest.getLocation());
                nearest.damage(1000, player);
                godSpeak("§6✦ God: §c*extends one finger* §e" + nearest.getName() + " §chas been... dealt with.");
            } else {
                godSpeak("§6✦ God: §7There is no worthy target nearby.");
            }

        // ── Thunderstorm ──────────────────────────────────────────────
        } else if (lower.contains("thunder") || lower.contains("storm") || lower.contains("lightning") || lower.contains("rain")) {
            if (form == GodForm.POWER_50 || form == GodForm.POWER_75) {
                godSummonStorm(player);
                godSpeak("§6✦ God: §9*splits the sky open* §eLet it rain, then. The heavens answer my call.");
            } else {
                godSpeak("§6✦ God: §7Weather is beyond my reach at 25% power. Summon me stronger.");
            }

        // ── TNT / Blast ───────────────────────────────────────────────
        } else if (lower.contains("tnt") || lower.contains("explode") || lower.contains("explosion") || lower.contains("blast") || lower.contains("boom")) {
            if (form == GodForm.POWER_50 || form == GodForm.POWER_75) {
                godDetonateArea(player, form == GodForm.POWER_75);
                godSpeak("§6✦ God: §4*smiles slowly* §eBoom.");
            } else {
                godSpeak("§6✦ God: §7Explosions of that scale need 50% power. Summon me stronger.");
            }

        // ── Items ─────────────────────────────────────────────────────
        } else if (containsItemRequest(lower)) {
            handleItemRequest(player, lower, message);

        // ── Lore ─────────────────────────────────────────────────────
        } else if (lower.contains("who are you") || lower.contains("what are you")) {
            godSpeak("§6✦ God: §e*chuckles* I am the first light. Before your world, before your gems — §6I was.");

        } else if (lower.contains("why") && lower.contains("trial")) {
            godSpeak("§6✦ God: §eTo test whether mortals are worthy of witnessing divinity. §c§lYou did. §e...Interesting.");

        } else if (lower.contains("thank")) {
            godSpeak("§6✦ God: §e*nods* Gratitude noted, " + player.getName() + ". It is... §6refreshing.");

        } else if (lower.contains("bye") || lower.contains("farewell") || lower.contains("goodbye")) {
            godSpeak("§6✦ God: §e*bows slowly* Farewell, " + player.getName() + ".");
            new BukkitRunnable() { public void run() { despawn(); }}.runTaskLater(plugin, 80L);

        // ── Fallback ──────────────────────────────────────────────────
        } else {
            String[] responses = {
                "§6✦ God: §e*tilts head* " + message + "... The cosmos has no simple answer for that.",
                "§6✦ God: §7*gazes into the distance* Even I ponder questions like that.",
                "§6✦ God: §eYou speak with boldness, " + player.getName() + ". Some things even gods keep secret.",
                "§6✦ God: §7*a long silence* ...Your question still surprises me.",
                "§6✦ God: §e*smiles faintly* Try asking for: §6heal, strength, speed, hearts, gem, blade, kill all, storm, blast, items, skill, or smite.",
            };
            godSpeak(responses[new Random().nextInt(responses.length)]);
        }
    }

    // ── Celestial Blade ──────────────────────────────────────────────────────

    private void giveCelestialBlade(Player player) {
        ItemStack blade = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = blade.getItemMeta();
        meta.setDisplayName("§6§lCelestial Blade");
        meta.setLore(List.of(
            "§7Forged from stardust and divine fire.",
            "§eRight Click§7: §6Celestial Smash §7(AoE blast + knockback)",
            "§eShift + Right Click§7: §6Star Storm §7(continuous lightning 5s)",
            "§eSneak§7: §6Nova Burst §7(explosion + particle supernova)",
            "§dPassive§7: Every hit ignites, knocks back & launches enemy",
            "",
            "§8§oGod's personal weapon. Handle with reverence.",
            "§8HeartsSMP Custom Item"
        ));
        // Max enchants
        blade.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.SHARPNESS, 20);
        blade.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.FIRE_ASPECT, 5);
        blade.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.KNOCKBACK, 5);
        blade.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.LOOTING, 5);
        blade.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 10);
        blade.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.MENDING, 1);
        meta.setUnbreakable(true);
        blade.setItemMeta(meta);
        player.getInventory().addItem(blade);
        player.sendMessage("§6§l✦ The Celestial Blade has been placed in your inventory!");
    }

    // ── God powers ───────────────────────────────────────────────────────────

    private int godKillNearby(Player player, double range) {
        int killed = 0;
        for (Entity e : player.getNearbyEntities(range, range, range)) {
            if (e instanceof LivingEntity le && e != player) {
                // Dramatic lightning + kill effect
                location.getWorld().strikeLightningEffect(e.getLocation());
                location.getWorld().spawnParticle(Particle.SOUL, e.getLocation(), 20, 0.5, 1, 0.5, 0.05);
                le.damage(10000, player);
                killed++;
            }
        }
        // Server-wide shockwave sound
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 1f, 0.5f);
        }
        return killed;
    }

    private void godSummonStorm(Player player) {
        World world = player.getWorld();
        // Permanent thunderstorm
        world.setStorm(true);
        world.setThundering(true);
        world.setWeatherDuration(Integer.MAX_VALUE);
        world.setThunderDuration(Integer.MAX_VALUE);

        // Immediate lightning barrage
        new BukkitRunnable() {
            int ticks = 0;
            public void run() {
                if (ticks >= 20) { cancel(); return; }
                ticks++;
                // Strike 3 random nearby locations
                for (int i = 0; i < 3; i++) {
                    double x = player.getLocation().getX() + (Math.random() - 0.5) * 40;
                    double z = player.getLocation().getZ() + (Math.random() - 0.5) * 40;
                    Location strike = new Location(world, x, world.getHighestBlockYAt((int)x, (int)z), z);
                    world.strikeLightning(strike);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);

        player.sendMessage("§9⛈ God has summoned a permanent thunderstorm upon this world!");
    }

    private void godDetonateArea(Player player, boolean megaBlast) {
        World world = player.getWorld();
        Location center = player.getLocation();

        if (megaBlast) {
            // 75% — multiple large explosions + TNT rain
            plugin.getServer().broadcastMessage("§4§l⚠ GOD DETONATES THE AREA AROUND " + player.getName() + "! ⚠");
            new BukkitRunnable() {
                int wave = 0;
                public void run() {
                    if (wave >= 8) { cancel(); return; }
                    wave++;
                    // Expanding ring of explosions
                    double radius = wave * 3;
                    for (int i = 0; i < 6; i++) {
                        double angle = (2 * Math.PI / 6) * i;
                        Location explodeLoc = center.clone().add(
                            Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
                        world.createExplosion(explodeLoc, 4f, false, false);
                        world.spawnParticle(Particle.FLAME, explodeLoc, 60, 1, 1, 1, 0.2);
                        world.strikeLightningEffect(explodeLoc);
                    }
                    // Central TNT for spectacle (no block damage)
                    world.createExplosion(center, 3f, false, false);
                    world.spawnParticle(Particle.FIREWORK, center, 100, 2, 2, 2, 0.5);
                    world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.5f);
                }
            }.runTaskTimer(plugin, 0L, 8L);

        } else {
            // 50% — single big explosion + TNT cluster
            plugin.getServer().broadcastMessage("§c§l⚠ GOD CALLS DOWN DESTRUCTION UPON " + player.getName() + "'s LOCATION! ⚠");
            world.createExplosion(center, 5f, false, false);
            world.spawnParticle(Particle.FIREWORK, center, 150, 3, 3, 3, 0.5);
            world.spawnParticle(Particle.FLAME, center, 100, 2, 2, 2, 0.3);
            world.strikeLightningEffect(center);
            world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.5f);
            world.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 0.5f);

            // Ring of TNT around player for spectacle
            for (int i = 0; i < 8; i++) {
                double angle = (2 * Math.PI / 8) * i;
                Location tntLoc = center.clone().add(Math.cos(angle) * 5, 1, Math.sin(angle) * 5);
                TNTPrimed tnt = world.spawn(tntLoc, TNTPrimed.class);
                tnt.setFuseTicks(30);
            }
        }
    }

    // ── Item request handling ─────────────────────────────────────────────────

    private boolean containsItemRequest(String lower) {
        return lower.contains("give me") || lower.contains("i want") || lower.contains("i need")
            || lower.contains("diamond") || lower.contains("netherite") || lower.contains("emerald")
            || lower.contains("gold") || lower.contains("iron") || lower.contains("obsidian")
            || lower.contains("ancient debris") || lower.contains("elytra") || lower.contains("totem")
            || lower.contains("beacon") || lower.contains("shulker") || lower.contains("apple");
    }

    private void handleItemRequest(Player player, String lower, String originalMessage) {
        int limit = ITEM_LIMITS.getOrDefault(form, 16);
        int requestedAmount = extractNumber(originalMessage);
        if (requestedAmount <= 0) requestedAmount = 64;
        int amount = Math.min(requestedAmount, limit);
        Material mat = detectMaterial(lower);

        if (mat == null) {
            godSpeak("§6✦ God: §7*furrows brow* I cannot conjure that. Ask for something real.");
            return;
        }
        if (form == GodForm.POWER_25 && isLuxuryItem(mat)) {
            godSpeak("§6✦ God: §7At 25% power, items of that magnitude are beyond my reach.");
            return;
        }
        int stackSize = mat.getMaxStackSize();
        int given = 0;
        while (given < amount) {
            int thisStack = Math.min(stackSize, amount - given);
            player.getInventory().addItem(new ItemStack(mat, thisStack));
            given += thisStack;
        }
        spawnGodBlessing(player);
        godSpeak("§6✦ God: §e*waves hand* " + amount + "x §6" + formatMaterialName(mat)
            + " §eis yours, " + player.getName() + "."
            + (requestedAmount > limit ? " §7(Capped at " + limit + " — even gods have standards.)" : ""));
    }

    private Material detectMaterial(String lower) {
        if (lower.contains("diamond block"))   return Material.DIAMOND_BLOCK;
        if (lower.contains("diamond"))         return Material.DIAMOND;
        if (lower.contains("netherite block")) return Material.NETHERITE_BLOCK;
        if (lower.contains("netherite ingot") || lower.contains("netherite")) return Material.NETHERITE_INGOT;
        if (lower.contains("ancient debris"))  return Material.ANCIENT_DEBRIS;
        if (lower.contains("emerald block"))   return Material.EMERALD_BLOCK;
        if (lower.contains("emerald"))         return Material.EMERALD;
        if (lower.contains("gold block"))      return Material.GOLD_BLOCK;
        if (lower.contains("gold ingot") || lower.contains("gold")) return Material.GOLD_INGOT;
        if (lower.contains("iron block"))      return Material.IRON_BLOCK;
        if (lower.contains("iron ingot") || lower.contains("iron")) return Material.IRON_INGOT;
        if (lower.contains("obsidian"))        return Material.OBSIDIAN;
        if (lower.contains("elytra"))          return Material.ELYTRA;
        if (lower.contains("totem"))           return Material.TOTEM_OF_UNDYING;
        if (lower.contains("beacon"))          return Material.BEACON;
        if (lower.contains("nether star"))     return Material.NETHER_STAR;
        if (lower.contains("shulker"))         return Material.SHULKER_BOX;
        if (lower.contains("enchanted golden apple") || lower.contains("god apple")) return Material.ENCHANTED_GOLDEN_APPLE;
        if (lower.contains("golden apple"))    return Material.GOLDEN_APPLE;
        if (lower.contains("end crystal"))     return Material.END_CRYSTAL;
        if (lower.contains("dragon egg"))      return Material.DRAGON_EGG;
        if (lower.contains("heart of the sea")) return Material.HEART_OF_THE_SEA;
        if (lower.contains("steak") || lower.contains("cooked beef")) return Material.COOKED_BEEF;
        if (lower.contains("bread") || lower.contains("food")) return Material.BREAD;
        if (lower.contains("apple"))           return Material.APPLE;
        return null;
    }

    private boolean isLuxuryItem(Material mat) {
        return switch (mat) {
            case NETHERITE_INGOT, NETHERITE_BLOCK, ANCIENT_DEBRIS,
                 ELYTRA, TOTEM_OF_UNDYING, BEACON, NETHER_STAR,
                 ENCHANTED_GOLDEN_APPLE, DRAGON_EGG, END_CRYSTAL,
                 HEART_OF_THE_SEA -> true;
            default -> false;
        };
    }

    private int extractNumber(String message) {
        for (String word : message.split("\\s+")) {
            try {
                int n = Integer.parseInt(word.replaceAll("[^0-9]", ""));
                if (n > 0) return n;
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private String formatMaterialName(Material mat) {
        String raw = mat.name().replace("_", " ").toLowerCase();
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void spawnGodBlessing(Player player) {
        player.getWorld().spawnParticle(Particle.END_ROD,  player.getLocation().add(0,1,0), 80, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation().add(0,1,0), 40, 0.4, 0.4, 0.4, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);
    }

    private Player getNearestOtherPlayer(Player source, double range) {
        Player nearest = null;
        double min = Double.MAX_VALUE;
        for (Entity e : source.getNearbyEntities(range, range, range)) {
            if (e instanceof Player p && p != source) {
                double d = p.getLocation().distanceSquared(source.getLocation());
                if (d < min) { min = d; nearest = p; }
            }
        }
        return nearest;
    }

    public void godSpeak(String message) {
        plugin.getServer().broadcastMessage(message);
        if (stand != null && !stand.isDead())
            location.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 0.4f, 1.8f);
    }

    public void despawn() {
        active = false;
        if (taskId != -1) { plugin.getServer().getScheduler().cancelTask(taskId); taskId = -1; }
        if (stand != null && !stand.isDead()) {
            stand.getWorld().spawnParticle(Particle.END_ROD, stand.getLocation(), 100, 1, 1, 1, 0.2);
            stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
            stand.remove();
            stand = null;
        }
    }

    public boolean isActive() { return active; }
    public GodForm getForm()  { return form; }
    public UUID getSummoner() { return summoner; }
    public ArmorStand getStand() { return stand; }
}
