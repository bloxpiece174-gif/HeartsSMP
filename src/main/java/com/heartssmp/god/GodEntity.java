package com.heartssmp.god;

import com.heartssmp.HeartsSMPPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class GodEntity {

    public enum GodForm {
        GUIDE,          // Divine trial guide — weak, friendly
        POWER_25,       // 25% power — first summon
        POWER_50,       // 50% power — second summon
        POWER_75        // 75% power — third/final summon
    }

    private final HeartsSMPPlugin plugin;
    private ArmorStand stand;
    private Location location;
    private GodForm form;
    private final UUID summoner;
    private boolean active = false;
    private int taskId = -1;
    private long spawnTime;
    private final Set<UUID> chatListeners = new HashSet<>();

    // God's chat request cooldown
    private final Map<UUID, Long> lastRequest = new HashMap<>();

    public GodEntity(HeartsSMPPlugin plugin, Location location, GodForm form, UUID summoner) {
        this.plugin = plugin;
        this.location = location;
        this.form = form;
        this.summoner = summoner;
    }

    public void spawn() {
        // Remove old stand if exists
        despawn();

        // Spawn ArmorStand as God's body
        stand = location.getWorld().spawn(location, ArmorStand.class, as -> {
            as.setCustomName(getGodTitle());
            as.setCustomNameVisible(true);
            as.setGravity(false);
            as.setVisible(true);
            as.setInvulnerable(true);
            as.setSmall(false);
            as.setBasePlate(false);
            as.setArms(true);

            // White & Gold appearance — use gold chestplate + white head
            ItemStack helmet = new ItemStack(Material.CARVED_PUMPKIN); // custom head placeholder
            ItemStack chest = new ItemStack(Material.GOLDEN_CHESTPLATE);
            ItemStack legs = new ItemStack(Material.GOLDEN_LEGGINGS);
            ItemStack boots = new ItemStack(Material.GOLDEN_BOOTS);

            // Enchant for glow
            chest.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 10);

            as.getEquipment().setHelmet(helmet);
            as.getEquipment().setChestplate(chest);
            as.getEquipment().setLeggings(legs);
            as.getEquipment().setBoots(boots);
        });

        active = true;
        spawnTime = System.currentTimeMillis();

        // Announce spawn
        broadcastSpawn();

        // Start floating animation + particle effects
        startFloatingAnimation();

        // Start 15 minute timer for forms POWER_25/50/75
        if (form != GodForm.GUIDE) {
            startSessionTimer();
        }
    }

    private String getGodTitle() {
        return switch (form) {
            case GUIDE -> "§e✦ §6§lThe Divine§e ✦ §7[Guide]";
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
                // God speaks
                new BukkitRunnable() {
                    public void run() {
                        if (!active) return;
                        godSpeak("§6✦ God: §e*manifests in a divine glow* I am here, mortal. I stand before you at §a25% §eof my true power. Choose your words wisely — you have §a15 minutes.");
                    }
                }.runTaskLater(plugin, 60L);
            }
            case POWER_50 -> {
                plugin.getServer().broadcastMessage("");
                plugin.getServer().broadcastMessage("§c§l✦ ═══════════════════════════════════ ✦");
                plugin.getServer().broadcastMessage("§6§l       GOD HAS BEEN SUMMONED [50%]       ");
                plugin.getServer().broadcastMessage("§c   The air trembles. God grows stronger...");
                plugin.getServer().broadcastMessage("§e   You have §a15 minutes §eto speak with God.");
                plugin.getServer().broadcastMessage("§c§l✦ ═══════════════════════════════════ ✦");
                plugin.getServer().broadcastMessage("");
                new BukkitRunnable() {
                    public void run() {
                        if (!active) return;
                        godSpeak("§6✦ God: §c*the ground shakes as I descend* I return at §c50% §cof my true might. The world feels my presence. Speak, mortal — what do you seek?");
                    }
                }.runTaskLater(plugin, 60L);
            }
            case POWER_75 -> {
                plugin.getServer().broadcastMessage("");
                plugin.getServer().broadcastMessage("§4§l✦ ═══════════════════════════════════════════ ✦");
                plugin.getServer().broadcastMessage("§6§k§lXX§r §4§l    !! GOD — FINAL SUMMON [75%] !!    §6§k§lXX");
                plugin.getServer().broadcastMessage("§4   THE DIVINE REACHES 75% POWER. TREMBLE.");
                plugin.getServer().broadcastMessage("§e   You have §a15 minutes §ewith God. Final audience.");
                plugin.getServer().broadcastMessage("§4§l✦ ═══════════════════════════════════════════ ✦");
                plugin.getServer().broadcastMessage("");
                // Shake all players
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 0));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 80, 0));
                }
                new BukkitRunnable() {
                    public void run() {
                        if (!active) return;
                        godSpeak("§6✦ God: §4*reality fractures around me* §6I manifest at §475% §6of my infinite power. This is your final audience with me, mortal. After this... the world will never be the same. Ask. Your. Question.");
                    }
                }.runTaskLater(plugin, 100L);
            }
        }
    }

    private void startFloatingAnimation() {
        final double[] yOffset = {0};
        final boolean[] goingUp = {true};
        final float[] yaw = {0};

        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!active || stand == null || stand.isDead()) return;

            // Float up and down
            if (goingUp[0]) {
                yOffset[0] += 0.03;
                if (yOffset[0] >= 0.5) goingUp[0] = false;
            } else {
                yOffset[0] -= 0.03;
                if (yOffset[0] <= 0) goingUp[0] = true;
            }

            // Rotate
            yaw[0] = (yaw[0] + 3) % 360;

            Location newLoc = location.clone().add(0, yOffset[0] + 1.5, 0);
            newLoc.setYaw(yaw[0]);
            stand.teleport(newLoc);

            // Particles based on form
            switch (form) {
                case GUIDE -> location.getWorld().spawnParticle(Particle.END_ROD, newLoc.add(0, 1, 0), 3, 0.5, 0.3, 0.5, 0.02);
                case POWER_25 -> {
                    location.getWorld().spawnParticle(Particle.END_ROD, newLoc.add(0, 1, 0), 4, 0.6, 0.4, 0.6, 0.02);
                    location.getWorld().spawnParticle(Particle.FIREWORK, newLoc, 2, 0.4, 0.4, 0.4, 0.05);
                }
                case POWER_50 -> {
                    location.getWorld().spawnParticle(Particle.END_ROD, newLoc.add(0, 1, 0), 6, 0.8, 0.5, 0.8, 0.03);
                    location.getWorld().spawnParticle(Particle.FLAME, newLoc, 3, 0.3, 0.3, 0.3, 0.02);
                    location.getWorld().spawnParticle(Particle.FIREWORK, newLoc, 3, 0.5, 0.5, 0.5, 0.05);
                }
                case POWER_75 -> {
                    location.getWorld().spawnParticle(Particle.END_ROD, newLoc.add(0, 1, 0), 10, 1, 0.6, 1, 0.04);
                    location.getWorld().spawnParticle(Particle.FLAME, newLoc, 5, 0.5, 0.4, 0.5, 0.03);
                    location.getWorld().spawnParticle(Particle.PORTAL, newLoc, 8, 0.8, 0.8, 0.8, 0.1);
                    location.getWorld().spawnParticle(Particle.DRAGON_BREATH, newLoc, 3, 0.3, 0.3, 0.3, 0.02);
                    // Lightning occasionally
                    if (Math.random() < 0.05) {
                        location.getWorld().strikeLightningEffect(newLoc);
                    }
                }
            }
        }, 0L, 2L);
    }

    private void startSessionTimer() {
        // 15 minute countdown
        new BukkitRunnable() {
            public void run() {
                if (!active) { cancel(); return; }
                godSpeak("§6✦ God: §e*glances at the divine hourglass* §710 minutes remain in our audience.");
            }
        }.runTaskLater(plugin, 5 * 60 * 20L); // 5 min mark

        new BukkitRunnable() {
            public void run() {
                if (!active) { cancel(); return; }
                godSpeak("§6✦ God: §c5 minutes remain. Choose your final requests wisely, mortal.");
            }
        }.runTaskLater(plugin, 10 * 60 * 20L); // 10 min mark

        new BukkitRunnable() {
            public void run() {
                if (!active) { cancel(); return; }
                godSpeak("§6✦ God: §4§lOne minute. My patience thins. Speak now or hold your peace for eternity.");
            }
        }.runTaskLater(plugin, 14 * 60 * 20L); // 14 min mark

        // 15 min — session ends
        new BukkitRunnable() {
            public void run() {
                if (!active) { cancel(); return; }

                // Check if this was the 3rd summon (POWER_75)
                if (form == GodForm.POWER_75) {
                    endFinalAudience();
                } else {
                    godSpeak("§6✦ God: §7*fades into divine light* Our time is done. The thread between worlds grows thin. Farewell, mortal... for now.");
                    new BukkitRunnable() {
                        public void run() { despawn(); }
                    }.runTaskLater(plugin, 60L);
                }
            }
        }.runTaskLater(plugin, 15 * 60 * 20L);
    }

    private void endFinalAudience() {
        Player summonerPlayer = plugin.getServer().getPlayer(summoner);

        plugin.getServer().broadcastMessage("");
        plugin.getServer().broadcastMessage("§4§l✦ ═══════════════════════════════════════════ ✦");
        plugin.getServer().broadcastMessage("§6§l     THE FINAL AUDIENCE HAS ENDED     ");
        plugin.getServer().broadcastMessage("§4   God takes back what was given...");
        plugin.getServer().broadcastMessage("§4§l✦ ═══════════════════════════════════════════ ✦");
        plugin.getServer().broadcastMessage("");

        // God speaks final words
        godSpeak("§6✦ God: §4§l*the sky darkens* You have spoken with me thrice, mortal. The divine debt is now due. I take back my gift... but I leave you breathing. §6The world... is mine now.");

        new BukkitRunnable() {
            public void run() {
                // Strip all lives except 1 from summoner
                if (summonerPlayer != null && summonerPlayer.isOnline()) {
                    com.heartssmp.data.PlayerData data = plugin.getDataManager().get(summonerPlayer.getUniqueId());
                    if (data != null) {
                        data.setLives(1);
                        data.setHearts(plugin.getConfig().getInt("hearts.starting", 10));
                        plugin.getDataManager().save(summonerPlayer.getUniqueId());
                        summonerPlayer.sendMessage("§4§l☠ God has stripped you to your last life. Survive what comes next...");
                    }
                }

                // Transform world to Divine World
                plugin.getDivineWorldManager().activateDivineWorld();

                despawn();
            }
        }.runTaskLater(plugin, 100L);
    }

    // Called when a player types in chat during God's session
    public void handleChatRequest(Player player, String message) {
        if (!active || form == GodForm.GUIDE) return;

        // Cooldown 10 seconds per player
        long now = System.currentTimeMillis();
        if (now - lastRequest.getOrDefault(player.getUniqueId(), 0L) < 10_000) {
            player.sendMessage("§7§o[God listens but does not speak twice so quickly...]");
            return;
        }
        lastRequest.put(player.getUniqueId(), now);

        String lower = message.toLowerCase();

        // Parse request and do action
        if (lower.contains("heal") || lower.contains("health")) {
            double maxHp = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(maxHp);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 4));
            spawnGodBlessing(player);
            godSpeak("§6✦ God: §e*places a divine hand upon " + player.getName() + "* Your wounds are beneath my concern. §aHealed.");

        } else if (lower.contains("strength") || lower.contains("power") || lower.contains("strong")) {
            int tier = form == GodForm.POWER_75 ? 4 : form == GodForm.POWER_50 ? 2 : 1;
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 60 * 5, tier));
            spawnGodBlessing(player);
            godSpeak("§6✦ God: §e*channels divine energy into " + player.getName() + "* Strength §e" + (tier + 1) + " §eflows through your veins. Use it well.");

        } else if (lower.contains("speed") || lower.contains("fast")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 5, 3));
            spawnGodBlessing(player);
            godSpeak("§6✦ God: §e*snaps fingers* Time bends to my will. You move like divine wind now, " + player.getName() + ".");

        } else if (lower.contains("heart") || lower.contains("life") || lower.contains("lives")) {
            com.heartssmp.data.PlayerData data = plugin.getDataManager().get(player.getUniqueId());
            if (data != null) {
                int bonus = form == GodForm.POWER_75 ? 3 : form == GodForm.POWER_50 ? 2 : 1;
                plugin.getHeartManager().addHearts(player, bonus);
                spawnGodBlessing(player);
                godSpeak("§6✦ God: §e*breathes life into " + player.getName() + "* §c+" + bonus + " hearts§e granted. Do not waste this gift.");
            }

        } else if (lower.contains("gem") || lower.contains("divine gem") || lower.contains("celestia")) {
            if (form == GodForm.POWER_75) {
                com.heartssmp.data.PlayerData data = plugin.getDataManager().get(player.getUniqueId());
                if (data != null) {
                    data.setGemId("DIVINE_CELESTIA");
                    data.setGemMastery(3);
                    plugin.getDataManager().save(player.getUniqueId());
                    spawnGodBlessing(player);
                    godSpeak("§6✦ God: §e*tears a fragment of divinity itself* The §6Celestia Gem§e at full mastery. §c§lOnly because you asked at my peak power.");
                }
            } else {
                godSpeak("§6✦ God: §7...That power is beyond what I offer at " + (form == GodForm.POWER_25 ? "25" : "50") + "% strength. Summon me at full power and ask again.");
            }

        } else if (lower.contains("skill") || lower.contains("grace") || lower.contains("enlighten")) {
            if (form == GodForm.POWER_50 || form == GodForm.POWER_75) {
                plugin.getSkillManager().grantDivineSkill(player);
                com.heartssmp.data.PlayerData data = plugin.getDataManager().get(player.getUniqueId());
                if (data != null) data.maxSkillMastery("graceful_enlightenment");
                spawnGodBlessing(player);
                godSpeak("§6✦ God: §e*sighs* Very well. §6Graceful Enlightenment§e — my signature divine art. Use it as I would: with grace.");
            } else {
                godSpeak("§6✦ God: §7At 25% power I cannot grant divine skills. My essence is too thin. Summon me again when you are worthy.");
            }

        } else if (lower.contains("who are you") || lower.contains("what are you") || lower.contains("tell me about")) {
            godSpeak("§6✦ God: §e*chuckles softly* I am the first light, mortal. Before your world existed, before your gems were forged, before the HeartsSMP was conceived — §6I was. §eI am the architect of your trials, the one who granted gems their power. And now... I stand before you.");

        } else if (lower.contains("why") && lower.contains("trial")) {
            godSpeak("§6✦ God: §eTo test whether mortals are worthy of witnessing divinity. Most never reach the altar. §c§lYou did. §e...Interesting.");

        } else if (lower.contains("kill") || lower.contains("smite") || lower.contains("destroy")) {
            // God smites nearest enemy player
            Player nearest = getNearestOtherPlayer(player, 30);
            if (nearest != null && nearest != player) {
                location.getWorld().strikeLightning(nearest.getLocation());
                nearest.damage(1000, player);
                godSpeak("§6✦ God: §c*extends one finger* §e" + nearest.getName() + " §chas been... dealt with.");
            } else {
                godSpeak("§6✦ God: §7There is no worthy target nearby. My power does not waste itself.");
            }

        } else if (lower.contains("thank") || lower.contains("thanks") || lower.contains("grateful")) {
            godSpeak("§6✦ God: §e*nods with ancient gravity* Gratitude noted, " + player.getName() + ". Few mortals remember to be thankful. It is... §6refreshing.");

        } else if (lower.contains("bye") || lower.contains("farewell") || lower.contains("goodbye") || lower.contains("leave")) {
            godSpeak("§6✦ God: §e*bows slowly* Farewell, " + player.getName() + ". May your path be lit by divine light. Until our threads cross again...");
            new BukkitRunnable() {
                public void run() { despawn(); }
            }.runTaskLater(plugin, 80L);

        } else {
            // General philosophical God response
            String[] responses = {
                "§6✦ God: §e*tilts head* " + message + "... An interesting thought, mortal. The cosmos has no simple answer for that.",
                "§6✦ God: §7*gazes into the distance* The question you ask touches the edge of divine understanding. Even I ponder it.",
                "§6✦ God: §eYou speak with boldness, " + player.getName() + ". I appreciate that. But some things even gods keep secret.",
                "§6✦ God: §7*a long silence* ...I have lived since before this world's first sunrise. Your question still surprises me.",
                "§6✦ God: §e*smiles faintly* Ask me something I can §6grant§e, mortal. Philosophy I offer freely. Power — that costs something.",
            };
            godSpeak(responses[new Random().nextInt(responses.length)]);
        }
    }

    private void spawnGodBlessing(Player player) {
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 80, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 1, 0), 40, 0.4, 0.4, 0.4, 0.1);
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
        // Play sound at God's location
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
            // Despawn effect
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
