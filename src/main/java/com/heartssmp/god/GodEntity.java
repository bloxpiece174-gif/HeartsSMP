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

    // God's chat request cooldown
    private final Map<UUID, Long> lastRequest = new HashMap<>();

    // Divine God head texture (base64) — golden divine deity skin
    private static final String GOD_TEXTURE =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3" +
        "RleHR1cmUvNzg2YzdiYTFhNTNmNTc1MWE2YzM1YjNiYjQyNzliZjFiNzI4YzI1N2RhY2RmYTA1ZDlm" +
        "YzIwZmJiMWRhMSJ9fX0=";

    // Max items God will give per request based on form
    private static final Map<GodForm, Integer> ITEM_LIMITS = Map.of(
        GodForm.POWER_25, 16,
        GodForm.POWER_50, 64,
        GodForm.POWER_75, 1728 // 27 stacks
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

            // ── Real divine head skin ──────────────────────────────
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            PlayerProfile profile = plugin.getServer().createProfile(UUID.randomUUID(), "God");
            profile.setProperty(new ProfileProperty("textures", GOD_TEXTURE));
            skullMeta.setPlayerProfile(profile);
            head.setItemMeta(skullMeta);

            // Gold armour, enchanted for glow
            ItemStack chest = new ItemStack(Material.GOLDEN_CHESTPLATE);
            ItemStack legs  = new ItemStack(Material.GOLDEN_LEGGINGS);
            ItemStack boots = new ItemStack(Material.GOLDEN_BOOTS);
            chest.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 10);
            legs.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 10);
            boots.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 10);

            // Hold a golden sword in right hand
            ItemStack sword = new ItemStack(Material.GOLDEN_SWORD);
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
                    godSpeak("§6✦ God: §e*manifests in a divine glow* I am here, mortal. I stand before you at §a25% §eof my true power. Choose your words wisely — you have §a15 minutes.");
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
                    godSpeak("§6✦ God: §c*the ground shakes as I descend* I return at §c50% §cof my true might. Speak, mortal — what do you seek?");
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
                    godSpeak("§6✦ God: §4*reality fractures around me* §6I manifest at §475% §6of my infinite power. This is your final audience with me. Ask. Your. Question.");
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
                case GUIDE -> location.getWorld().spawnParticle(Particle.END_ROD, newLoc.add(0,1,0), 3, 0.5, 0.3, 0.5, 0.02);
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
            godSpeak("§6✦ God: §e*glances at the divine hourglass* §710 minutes remain in our audience.");
        }}.runTaskLater(plugin, 5 * 60 * 20L);

        new BukkitRunnable() { public void run() {
            if (!active) { cancel(); return; }
            godSpeak("§6✦ God: §c5 minutes remain. Choose your final requests wisely, mortal.");
        }}.runTaskLater(plugin, 10 * 60 * 20L);

        new BukkitRunnable() { public void run() {
            if (!active) { cancel(); return; }
            godSpeak("§6✦ God: §4§lOne minute. My patience thins. Speak now or hold your peace for eternity.");
        }}.runTaskLater(plugin, 14 * 60 * 20L);

        new BukkitRunnable() { public void run() {
            if (!active) { cancel(); return; }
            if (form == GodForm.POWER_75) {
                endFinalAudience();
            } else {
                godSpeak("§6✦ God: §7*fades into divine light* Our time is done. Farewell, mortal... for now.");
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

        godSpeak("§6✦ God: §4§l*the sky darkens* You have spoken with me thrice, mortal. The divine debt is now due. §6The world... is mine now.");

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

        // ── Heal ────────────────────────────────────────────────────────────
        if (lower.contains("heal") || lower.contains("health")) {
            double maxHp = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(maxHp);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 4));
            spawnGodBlessing(player);
            godSpeak("§6✦ God: §e*places a divine hand upon " + player.getName() + "* Your wounds are beneath my concern. §aHealed.");

        // ── Strength ─────────────────────────────────────────────────────────
        } else if (lower.contains("strength") || lower.contains("power") || lower.contains("strong")) {
            int tier = form == GodForm.POWER_75 ? 4 : form == GodForm.POWER_50 ? 2 : 1;
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 60 * 5, tier));
            spawnGodBlessing(player);
            godSpeak("§6✦ God: §e*channels divine energy into " + player.getName() + "* Strength §e" + (tier + 1) + " §eflows through your veins.");

        // ── Speed ────────────────────────────────────────────────────────────
        } else if (lower.contains("speed") || lower.contains("fast")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 5, 3));
            spawnGodBlessing(player);
            godSpeak("§6✦ God: §e*snaps fingers* You move like divine wind now, " + player.getName() + ".");

        // ── Hearts / Lives ───────────────────────────────────────────────────
        } else if (lower.contains("heart") || lower.contains("life") || lower.contains("lives")) {
            com.heartssmp.data.PlayerData data = plugin.getDataManager().get(player.getUniqueId());
            if (data != null) {
                int bonus = form == GodForm.POWER_75 ? 3 : form == GodForm.POWER_50 ? 2 : 1;
                plugin.getHeartManager().addHearts(player, bonus);
                spawnGodBlessing(player);
                godSpeak("§6✦ God: §e*breathes life into " + player.getName() + "* §c+" + bonus + " hearts§e granted. Do not waste this gift.");
            }

        // ── Celestia Gem ─────────────────────────────────────────────────────
        } else if (lower.contains("gem") || lower.contains("celestia")) {
            if (form == GodForm.POWER_75) {
                com.heartssmp.data.PlayerData data = plugin.getDataManager().get(player.getUniqueId());
                if (data != null) {
                    data.setGemId("DIVINE_CELESTIA");
                    data.setGemMastery(3);
                    plugin.getDataManager().save(player.getUniqueId());
                    // ✅ FIX — actually give the physical gem item
                    plugin.getGemManager().giveGemItem(player, "DIVINE_CELESTIA");
                    spawnGodBlessing(player);
                    godSpeak("§6✦ God: §e*tears a fragment of divinity itself* The §6Celestia Gem§e at full mastery. §c§lOnly because you asked at my peak power.");
                }
            } else {
                godSpeak("§6✦ God: §7That power is beyond what I offer at " + (form == GodForm.POWER_25 ? "25" : "50") + "% strength. Summon me at full power.");
            }

        // ── Divine Skill ─────────────────────────────────────────────────────
        } else if (lower.contains("skill") || lower.contains("grace") || lower.contains("enlighten")) {
            if (form == GodForm.POWER_50 || form == GodForm.POWER_75) {
                plugin.getSkillManager().grantDivineSkill(player);
                com.heartssmp.data.PlayerData data = plugin.getDataManager().get(player.getUniqueId());
                if (data != null) data.maxSkillMastery("graceful_enlightenment");
                spawnGodBlessing(player);
                godSpeak("§6✦ God: §e*sighs* Very well. §6Graceful Enlightenment§e — my signature divine art. Use it as I would: with grace.");
            } else {
                godSpeak("§6✦ God: §7At 25% power I cannot grant divine skills. Summon me again when you are worthy.");
            }

        // ── Item granting ────────────────────────────────────────────────────
        } else if (containsItemRequest(lower)) {
            handleItemRequest(player, lower, message);

        // ── Smite ────────────────────────────────────────────────────────────
        } else if (lower.contains("kill") || lower.contains("smite") || lower.contains("destroy")) {
            Player nearest = getNearestOtherPlayer(player, 30);
            if (nearest != null) {
                location.getWorld().strikeLightning(nearest.getLocation());
                nearest.damage(1000, player);
                godSpeak("§6✦ God: §c*extends one finger* §e" + nearest.getName() + " §chas been... dealt with.");
            } else {
                godSpeak("§6✦ God: §7There is no worthy target nearby. My power does not waste itself.");
            }

        // ── Lore / identity ──────────────────────────────────────────────────
        } else if (lower.contains("who are you") || lower.contains("what are you")) {
            godSpeak("§6✦ God: §e*chuckles softly* I am the first light, mortal. Before your world existed, before your gems were forged — §6I was. §eI am the architect of your trials.");

        } else if (lower.contains("why") && lower.contains("trial")) {
            godSpeak("§6✦ God: §eTo test whether mortals are worthy of witnessing divinity. Most never reach the altar. §c§lYou did. §e...Interesting.");

        } else if (lower.contains("thank")) {
            godSpeak("§6✦ God: §e*nods with ancient gravity* Gratitude noted, " + player.getName() + ". It is... §6refreshing.");

        } else if (lower.contains("bye") || lower.contains("farewell") || lower.contains("goodbye")) {
            godSpeak("§6✦ God: §e*bows slowly* Farewell, " + player.getName() + ". May your path be lit by divine light.");
            new BukkitRunnable() { public void run() { despawn(); }}.runTaskLater(plugin, 80L);

        // ── Fallback ─────────────────────────────────────────────────────────
        } else {
            String[] responses = {
                "§6✦ God: §e*tilts head* " + message + "... The cosmos has no simple answer for that.",
                "§6✦ God: §7*gazes into the distance* The question you ask touches the edge of divine understanding.",
                "§6✦ God: §eYou speak with boldness, " + player.getName() + ". But some things even gods keep secret.",
                "§6✦ God: §7*a long silence* ...I have lived since before this world's first sunrise. Your question still surprises me.",
                "§6✦ God: §e*smiles faintly* Ask me something I can §6grant§e, mortal. Try: heal, strength, speed, hearts, gem, items, skill, or smite.",
            };
            godSpeak(responses[new Random().nextInt(responses.length)]);
        }
    }

    // ── Item request detection ───────────────────────────────────────────────

    private boolean containsItemRequest(String lower) {
        return lower.contains("give me") || lower.contains("i want") || lower.contains("i need")
            || lower.contains("diamond") || lower.contains("netherite") || lower.contains("emerald")
            || lower.contains("gold") || lower.contains("iron") || lower.contains("obsidian")
            || lower.contains("ancient debris") || lower.contains("elytra") || lower.contains("totem")
            || lower.contains("enchanted") || lower.contains("beacon") || lower.contains("shulker");
    }

    private void handleItemRequest(Player player, String lower, String originalMessage) {
        int limit = ITEM_LIMITS.getOrDefault(form, 16);

        // Try to detect a number in the message
        int requestedAmount = extractNumber(originalMessage);
        if (requestedAmount <= 0) requestedAmount = 64;

        // Cap to power-level limit
        int amount = Math.min(requestedAmount, limit);

        Material mat = detectMaterial(lower);

        if (mat == null) {
            godSpeak("§6✦ God: §7*furrows brow* I cannot conjure that which does not exist in your world. Ask for something real.");
            return;
        }

        // 25% refuses luxury items
        if (form == GodForm.POWER_25 && isLuxuryItem(mat)) {
            godSpeak("§6✦ God: §7At 25% of my power, items of that magnitude are beyond my reach. Summon me stronger.");
            return;
        }

        // Give items in stacks
        int stackSize = mat.getMaxStackSize();
        int given = 0;
        while (given < amount) {
            int thisStack = Math.min(stackSize, amount - given);
            player.getInventory().addItem(new ItemStack(mat, thisStack));
            given += thisStack;
        }

        spawnGodBlessing(player);
        godSpeak("§6✦ God: §e*waves hand* " + amount + "x §6" + formatMaterialName(mat)
            + " §eis yours, " + player.getName() + ". Consider it a divine gift."
            + (requestedAmount > limit ? " §7(I capped it at " + limit + " — even gods have standards.)" : ""));
    }

    private Material detectMaterial(String lower) {
        if (lower.contains("diamond block"))     return Material.DIAMOND_BLOCK;
        if (lower.contains("diamond"))           return Material.DIAMOND;
        if (lower.contains("netherite block"))   return Material.NETHERITE_BLOCK;
        if (lower.contains("netherite ingot"))   return Material.NETHERITE_INGOT;
        if (lower.contains("netherite"))         return Material.NETHERITE_INGOT;
        if (lower.contains("ancient debris"))    return Material.ANCIENT_DEBRIS;
        if (lower.contains("emerald block"))     return Material.EMERALD_BLOCK;
        if (lower.contains("emerald"))           return Material.EMERALD;
        if (lower.contains("gold block"))        return Material.GOLD_BLOCK;
        if (lower.contains("gold ingot") || lower.contains("gold"))   return Material.GOLD_INGOT;
        if (lower.contains("iron block"))        return Material.IRON_BLOCK;
        if (lower.contains("iron ingot") || lower.contains("iron"))   return Material.IRON_INGOT;
        if (lower.contains("obsidian"))          return Material.OBSIDIAN;
        if (lower.contains("crying obsidian"))   return Material.CRYING_OBSIDIAN;
        if (lower.contains("elytra"))            return Material.ELYTRA;
        if (lower.contains("totem"))             return Material.TOTEM_OF_UNDYING;
        if (lower.contains("beacon"))            return Material.BEACON;
        if (lower.contains("nether star"))       return Material.NETHER_STAR;
        if (lower.contains("shulker box") || lower.contains("shulker")) return Material.SHULKER_BOX;
        if (lower.contains("enchanted golden apple") || lower.contains("god apple")) return Material.ENCHANTED_GOLDEN_APPLE;
        if (lower.contains("golden apple"))      return Material.GOLDEN_APPLE;
        if (lower.contains("end crystal"))       return Material.END_CRYSTAL;
        if (lower.contains("experience bottle") || lower.contains("xp")) return Material.EXPERIENCE_BOTTLE;
        if (lower.contains("heart of the sea"))  return Material.HEART_OF_THE_SEA;
        if (lower.contains("dragon egg"))        return Material.DRAGON_EGG;
        if (lower.contains("cooked beef") || lower.contains("steak")) return Material.COOKED_BEEF;
        if (lower.contains("bread") || lower.contains("food"))        return Material.BREAD;
        if (lower.contains("apple"))             return Material.APPLE;
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
        String[] words = message.split("\\s+");
        for (String word : words) {
            try {
                int n = Integer.parseInt(word.replaceAll("[^0-9]", ""));
                if (n > 0) return n;
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private String formatMaterialName(Material mat) {
        return mat.name().replace("_", " ").toLowerCase()
            .substring(0, 1).toUpperCase()
            + mat.name().replace("_", " ").toLowerCase().substring(1);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

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
        if (stand != null && !stand.isDead()) {
            location.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 0.4f, 1.8f);
        }
    }

    public void despawn() {
        active = false;
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        if (stand != null && !stand.isDead()) {
            stand.getWorld().spawnParticle(Particle.END_ROD, stand.getLocation(), 100, 1, 1, 1, 0.2);
            stand.getWorld().playSound(stand.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
            stand.remove();
            stand = null;
        }
    }

    public boolean isActive() { return active; }
    public GodForm getForm() { return form; }
    public UUID getSummoner() { return summoner; }
    public ArmorStand getStand() { return stand; }
}
