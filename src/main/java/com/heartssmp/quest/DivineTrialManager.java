package com.heartssmp.quest;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Manages the full "Divine Trial" quest line:
 *
 * 1. Unlock BOTH mythical skills (omega_force + time_warp) -> receive a Map item.
 * 2. Travel to the altar coordinate -> build a 6x6 diamond platform centered on it,
 *    with a Golden Torch (custom item) at each of the 4 corners.
 * 3. Place a sign reading "I Want To Participate In Divine Trial".
 * 4. Weather force-changes to a thunderstorm, lightning strikes the player,
 *    screen flashes white (Blindness + sound), then they're teleported to the
 *    Divine Palace, far from spawn.
 * 5. Complete 3 tasks in order: Combat Gauntlet -> Sky Parkour -> Brazier Puzzle.
 * 6. "God" speaks to the player (scripted dialogue/story), grants Graceful
 *    Enlightenment (the Divine skill).
 * 7. ONLY the very first player ever to complete the trial also receives
 *    3 one-time "Summon God" charges, usable once they reach mastery 15
 *    on the divine skill.
 */
public class DivineTrialManager {

    public static final String STAGE_NOT_STARTED = "NOT_STARTED";
    public static final String STAGE_MAP_GIVEN = "MAP_GIVEN";
    public static final String STAGE_ALTAR_READY = "ALTAR_READY"; // platform+torches placed, awaiting sign
    public static final String STAGE_IN_TRIAL = "IN_TRIAL";
    public static final String STAGE_COMPLETED = "COMPLETED";

    private static final String DIVINE_SKILL_ID = "graceful_enlightenment";
    public static final String GOLDEN_TORCH_ID = "golden_torch";

    private final HeartsSMPPlugin plugin;

    // Players actively inside the palace running tasks: uuid -> trial session
    private final Map<UUID, TrialSession> activeSessions = new HashMap<>();

    // Tracks whether ANYONE has completed the trial yet on this server
    private boolean firstCompletionClaimed = false;

    public DivineTrialManager(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
        loadGlobalState();
    }

    // ---------------------------------------------------------------
    // Stage 1: Mythical skill check -> give the map
    // ---------------------------------------------------------------

    /** Call this whenever a player unlocks a skill or mastery changes. */
    public void checkMythicalCompletion(Player player, PlayerData data) {
        if (!data.getDivineTrialStage().equals(STAGE_NOT_STARTED)) return;
        if (data.hasSkill(DIVINE_SKILL_ID)) return;

        boolean hasOmega = data.hasSkill("omega_force");
        boolean hasTimeWarp = data.hasSkill("time_warp");

        if (hasOmega && hasTimeWarp) {
            giveMap(player, data);
        }
    }

    private void giveMap(Player player, PlayerData data) {
        data.setDivineTrialStage(STAGE_MAP_GIVEN);
        plugin.getDataManager().save(player.getUniqueId());

        ItemStack map = createMapItem();
        player.getInventory().addItem(map);

        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1.5f);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 60, 1, 1, 1, 0.1);

        player.sendMessage(" ");
        player.sendMessage("§e§l✨ You have mastered both Mythical skills...");
        player.sendMessage("§7A weight settles in your inventory. Something ancient has noticed you.");
        player.sendMessage("§7Check your inventory for the §6Map to the Divine Trial Chamber§7.");
        player.sendMessage(" ");

        plugin.getServer().broadcastMessage("§6[HeartsSMP] ✨ " + player.getName()
                + " has been deemed worthy... a path to the Divine Trial has opened.");
    }

    private ItemStack createMapItem() {
        ItemStack item = new ItemStack(Material.MAP);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Map to the Divine Trial Chamber", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        FileConfigCoords c = getAltarCoords();
        meta.lore(List.of(
                Component.text("A faint golden line leads toward:", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text(c.x + ", " + c.y + ", " + c.z, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false),
                Component.text("Build the altar there to begin.", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }

    // ---------------------------------------------------------------
    // Stage 2: Altar validation (6x6 diamond platform + 4 golden torches)
    // ---------------------------------------------------------------

    /** Call this when a player places a block or torch near the altar coordinate. */
    public void onPossibleAltarBlockPlace(Player player, Location placed) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        if (!data.getDivineTrialStage().equals(STAGE_MAP_GIVEN)) return;

        FileConfigCoords c = getAltarCoords();
        if (!placed.getWorld().getName().equals(c.world)) return;
        if (placed.distance(new Location(placed.getWorld(), c.x, c.y, c.z)) > 12) return;

        if (validateAltar(placed.getWorld(), c)) {
            data.setDivineTrialStage(STAGE_ALTAR_READY);
            plugin.getDataManager().save(player.getUniqueId());

            player.sendMessage(" ");
            player.sendMessage("§e§l✨ The altar hums with energy...");
            player.sendMessage("§7Place a sign nearby reading: §f\"I Want To Participate In Divine Trial\"");
            player.sendMessage(" ");
            player.playSound(placed, Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        }
    }

    /** Validates a flat 6x6 diamond-block platform centered on the altar coordinate, with golden torches on the 4 corners. */
    private boolean validateAltar(World world, FileConfigCoords c) {
        int half = 3; // 6x6 -> 3 blocks each side of center
        int baseX = c.x - half;
        int baseZ = c.z - half;
        int y = c.y;

        for (int dx = 0; dx < 6; dx++) {
            for (int dz = 0; dz < 6; dz++) {
                Block b = world.getBlockAt(baseX + dx, y, baseZ + dz);
                if (b.getType() != Material.DIAMOND_BLOCK) return false;
            }
        }

        // 4 corners, one block above the platform, must have a torch-like marker (golden torch = lantern on a fence, see helper item)
        int[][] corners = {
                {baseX, baseZ}, {baseX + 5, baseZ}, {baseX, baseZ + 5}, {baseX + 5, baseZ + 5}
        };
        for (int[] corner : corners) {
            Block above = world.getBlockAt(corner[0], y + 1, corner[1]);
            if (above.getType() != Material.LANTERN && above.getType() != Material.SOUL_LANTERN
                    && above.getType() != Material.TORCH) {
                return false;
            }
        }
        return true;
    }

    // ---------------------------------------------------------------
    // Stage 3: Sign placement -> triggers the storm sequence
    // ---------------------------------------------------------------

    public void onSignPlace(Player player, Sign sign, String[] lines) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        if (!data.getDivineTrialStage().equals(STAGE_ALTAR_READY)) return;

        FileConfigCoords c = getAltarCoords();
        if (!sign.getWorld().getName().equals(c.world)) return;
        if (sign.getLocation().distance(new Location(sign.getWorld(), c.x, c.y, c.z)) > 12) return;

        String joined = String.join(" ", lines).replaceAll("\\s+", " ").trim();
        if (!joined.equalsIgnoreCase("I Want To Participate In Divine Trial")) return;

        beginStormSequence(player, data);
    }

    private void beginStormSequence(Player player, PlayerData data) {
        data.setDivineTrialStage(STAGE_IN_TRIAL);
        plugin.getDataManager().save(player.getUniqueId());

        World world = player.getWorld();
        world.setStorm(true);
        world.setThundering(true);
        world.setWeatherDuration(200);
        world.setThunderDuration(200);

        player.sendMessage("§8§lThe sky darkens...");

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks += 20;
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                if (ticks == 40) {
                    // Strike lightning visually + damage-free at the player's location
                    player.getWorld().strikeLightningEffect(player.getLocation());
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.BLINDNESS, 70, 1, false, false));
                    player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2f, 1f);
                }
                if (ticks == 80) {
                    teleportToPalace(player);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void teleportToPalace(Player player) {
        FileConfigCoords p = getPalaceCoords();
        World world = Bukkit.getWorld(p.world);
        if (world == null) world = player.getWorld();
        Location dest = new Location(world, p.x + 0.5, p.y, p.z + 0.5);
        player.teleport(dest);

        player.removePotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS);
        player.playSound(dest, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 0.6f);
        player.sendMessage(" ");
        player.sendMessage("§e§lThe white fades...");
        player.sendMessage("§7You stand somewhere impossibly high. A palace of light and stone stretches before you.");
        player.sendMessage(" ");

        TrialSession session = new TrialSession(player.getUniqueId(), dest.clone());
        activeSessions.put(player.getUniqueId(), session);

        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data != null) {
            data.setDivineTrialTaskIndex(0);
            plugin.getDataManager().save(player.getUniqueId());
        }

        TrialTasks.startCombatTask(plugin, this, player, dest);
    }

    public TrialSession getSession(UUID uuid) {
        return activeSessions.get(uuid);
    }

    // ---------------------------------------------------------------
    // Task progression
    // ---------------------------------------------------------------

    public void advanceTask(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        TrialSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        int next = data.getDivineTrialTaskIndex() + 1;
        data.setDivineTrialTaskIndex(next);
        plugin.getDataManager().save(player.getUniqueId());

        switch (next) {
            case 1 -> {
                player.sendMessage("§a§l✓ Combat Gauntlet cleared!");
                TrialTasks.startParkourTask(plugin, this, player, session.origin);
            }
            case 2 -> {
                player.sendMessage("§a§l✓ The Ascent complete!");
                TrialTasks.startPuzzleTask(plugin, this, player, session.origin);
            }
            case 3 -> {
                player.sendMessage("§a§l✓ The braziers burn as one!");
                completeTrial(player, data);
            }
            default -> {}
        }
    }

    // ---------------------------------------------------------------
    // Final stage: God dialogue + reward
    // ---------------------------------------------------------------

    private void completeTrial(Player player, PlayerData data) {
        activeSessions.remove(player.getUniqueId());

        boolean isFirstEver = !firstCompletionClaimed;

        runGodDialogue(player, () -> {
            plugin.getSkillManager().grantDivineSkill(player);
            data.setDivineTrialStage(STAGE_COMPLETED);

            if (isFirstEver) {
                firstCompletionClaimed = true;
                saveGlobalState();
                int charges = plugin.getConfig().getInt("divine-trial.first-completer-summon-charges", 3);
                data.setGodSummonsRemaining(charges);
                player.sendMessage(" ");
                player.sendMessage("§d§l★ You are the FIRST to ever complete the Divine Trial! ★");
                player.sendMessage("§7Once you reach §emax mastery (15)§7 on Graceful Enlightenment, you alone");
                player.sendMessage("§7may §dsummon God to any location§7 — §d" + charges + " times§7, forever.");
                player.sendMessage(" ");
                plugin.getServer().broadcastMessage("§d[HeartsSMP] ★ " + player.getName()
                        + " is the FIRST mortal to ever complete the Divine Trial! ★");
            }

            plugin.getDataManager().save(player.getUniqueId());

            // Send player home-ish: back to the altar location instead of leaving them stranded in the void
            FileConfigCoords c = getAltarCoords();
            World w = Bukkit.getWorld(c.world);
            if (w != null) {
                player.teleport(new Location(w, c.x + 0.5, c.y + 2, c.z + 0.5));
            }
        });
    }

    /** Scripted "God is speaking to you" sequence, then runs onFinished. */
    private void runGodDialogue(Player player, Runnable onFinished) {
        List<String> lines = List.of(
                "§5§l[???] §r§dWho dares stand at the end of My trial...",
                "§5§l[???] §r§dAh. A mortal who tasted of every common gift, and still hungered.",
                "§5§l[???] §r§dLong ago, I too was bound by hunger like yours — for power, for purpose.",
                "§5§l[???] §r§dI was not always what you see now. I was a wanderer, same as you, who refused to stop climbing.",
                "§5§l[???] §r§dMany came before you. Their bones rest in My halls. You... you are still standing.",
                "§5§l[???] §r§dVery well. Rise, mortal. Carry what I carry — but never forget where you began.",
        );

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                if (index >= lines.size()) {
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 2f, 0.7f);
                    player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 200, 2, 2, 2, 0.3);
                    onFinished.run();
                    cancel();
                    return;
                }
                player.sendMessage(lines.get(index));
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.5f, 0.6f);
                index++;
            }
        }.runTaskTimer(plugin, 20L, 60L);
    }

    // ---------------------------------------------------------------
    // Summon God ability (first completer only, mastery 15 required)
    // ---------------------------------------------------------------

    public boolean summonGod(Player player, Location target) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return false;

        if (!data.hasSkill(DIVINE_SKILL_ID)) {
            player.sendMessage(plugin.prefix() + "§cYou do not carry Divine Grace.");
            return false;
        }
        if (data.getSkillMastery(DIVINE_SKILL_ID) < 15) {
            player.sendMessage(plugin.prefix() + "§cYou must reach max mastery (15) on Graceful Enlightenment first.");
            return false;
        }
        if (data.getGodSummonsRemaining() <= 0) {
            player.sendMessage(plugin.prefix() + "§cYou have no Summon God charges remaining.");
            return false;
        }

        data.useGodSummon();
        plugin.getDataManager().save(player.getUniqueId());

        target.getWorld().strikeLightningEffect(target);
        target.getWorld().spawnParticle(Particle.END_ROD, target, 300, 2, 3, 2, 0.3);
        target.getWorld().playSound(target, Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 0.7f);

        plugin.getServer().broadcastMessage("§d[HeartsSMP] ★ " + player.getName()
                + " has called upon God. The skies answer. (§5" + data.getGodSummonsRemaining() + " summons left§d)");
        return true;
    }

    // ---------------------------------------------------------------
    // Config / global state helpers
    // ---------------------------------------------------------------

    public FileConfigCoords getAltarCoords() {
        return new FileConfigCoords(
                plugin.getConfig().getString("divine-trial.altar-world", "world"),
                plugin.getConfig().getInt("divine-trial.altar-x", 500),
                plugin.getConfig().getInt("divine-trial.altar-y", 100),
                plugin.getConfig().getInt("divine-trial.altar-z", 500)
        );
    }

    public FileConfigCoords getPalaceCoords() {
        return new FileConfigCoords(
                plugin.getConfig().getString("divine-trial.palace-world", "world"),
                plugin.getConfig().getInt("divine-trial.palace-x", 10000),
                plugin.getConfig().getInt("divine-trial.palace-y", 250),
                plugin.getConfig().getInt("divine-trial.palace-z", 10000)
        );
    }

    private void loadGlobalState() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        java.io.File f = new java.io.File(plugin.getDataFolder(), "divine_trial_global.yml");
        if (!f.exists()) {
            firstCompletionClaimed = false;
            return;
        }
        org.bukkit.configuration.file.YamlConfiguration cfg =
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(f);
        firstCompletionClaimed = cfg.getBoolean("firstCompletionClaimed", false);
    }

    private void saveGlobalState() {
        java.io.File f = new java.io.File(plugin.getDataFolder(), "divine_trial_global.yml");
        org.bukkit.configuration.file.YamlConfiguration cfg = new org.bukkit.configuration.file.YamlConfiguration();
        cfg.set("firstCompletionClaimed", firstCompletionClaimed);
        try {
            cfg.save(f);
        } catch (java.io.IOException e) {
            plugin.getLogger().severe("Failed to save divine trial global state");
        }
    }

    public static class FileConfigCoords {
        public final String world;
        public final int x, y, z;

        public FileConfigCoords(String world, int x, int y, int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static class TrialSession {
        public final UUID playerUuid;
        public final Location origin; // where they were dropped into the palace
        public final long startedAt;

        public TrialSession(UUID playerUuid, Location origin) {
            this.playerUuid = playerUuid;
            this.origin = origin;
            this.startedAt = System.currentTimeMillis();
        }
    }
}
