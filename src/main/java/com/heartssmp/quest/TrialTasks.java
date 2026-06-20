package com.heartssmp.quest;

import com.heartssmp.HeartsSMPPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Implements the 3 in-palace tasks:
 *  1. Combat Gauntlet — fight off 3 waves of tough vexes in the arena ring.
 *  2. The Ascent — parkour up the floating causeway without falling.
 *  3. Brazier Puzzle — light all 4 campfires within a time limit.
 */
public class TrialTasks {

    private static final Set<UUID> activeCombat = new HashSet<>();
    private static final Map<UUID, Set<Location>> litBraziers = new HashMap<>();

    // ---------------------------------------------------------------
    // Task 1: Combat Gauntlet
    // ---------------------------------------------------------------

    public static void startCombatTask(HeartsSMPPlugin plugin, DivineTrialManager quest, Player player, Location palaceCenter) {
        PalaceBuilder.buildIfNeeded(player.getWorld(), palaceCenter);

        player.sendMessage("§c§l⚔ TASK 1 — COMBAT GAUNTLET");
        player.sendMessage("§7Survive 3 waves of divine guardians. Stay within the ring.");

        activeCombat.add(player.getUniqueId());
        spawnWave(plugin, quest, player, palaceCenter, 1);
    }

    private static void spawnWave(HeartsSMPPlugin plugin, DivineTrialManager quest, Player player, Location center, int wave) {
        if (!player.isOnline() || !activeCombat.contains(player.getUniqueId())) return;

        player.sendMessage("§c§l⚔ Wave " + wave + "/3 incoming...");
        player.playSound(player.getLocation(), Sound.ENTITY_VEX_CHARGE, 1f, 0.8f);

        List<Mob> spawned = new ArrayList<>();
        int count = 2 + wave; // 3, 4, 5 mobs per wave
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI / count) * i;
            Location spawnLoc = center.clone().add(Math.cos(angle) * 8, 1, Math.sin(angle) * 8);
            Vex vex = (Vex) player.getWorld().spawnEntity(spawnLoc, EntityType.VEX);
            vex.setCustomName("§5Divine Guardian");
            vex.setCustomNameVisible(true);
            vex.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20 + (wave * 10));
            vex.setHealth(20 + (wave * 10));
            vex.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(4 + wave);
            spawned.add(vex);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    spawned.forEach(m -> { if (!m.isDead()) m.remove(); });
                    activeCombat.remove(player.getUniqueId());
                    cancel();
                    return;
                }
                spawned.removeIf(m -> m == null || m.isDead());
                if (spawned.isEmpty()) {
                    cancel();
                    if (wave < 3) {
                        spawnWave(plugin, quest, player, center, wave + 1);
                    } else {
                        activeCombat.remove(player.getUniqueId());
                        player.sendMessage("§a§l✓ All waves cleared!");
                        quest.advanceTask(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, 40L);
    }

    // ---------------------------------------------------------------
    // Task 2: The Ascent (parkour)
    // ---------------------------------------------------------------

    public static void startParkourTask(HeartsSMPPlugin plugin, DivineTrialManager quest, Player player, Location palaceCenter) {
        Location start = palaceCenter.clone().add(0, 1, 24);
        player.teleport(start);

        player.sendMessage(" ");
        player.sendMessage("§b§l⬆ TASK 2 — THE ASCENT");
        player.sendMessage("§7Climb the floating path. Falling resets you to the start.");
        player.sendMessage(" ");

        Location puzzleCenter = PalaceBuilder.getPuzzleCenter(palaceCenter);
        double targetY = puzzleCenter.getY();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                DivineTrialManager.TrialSession session = quest.getSession(player.getUniqueId());
                if (session == null) {
                    cancel();
                    return;
                }

                // Fell off the path
                if (player.getLocation().getY() < start.getY() - 5) {
                    player.teleport(start);
                    player.sendMessage("§c✘ You fell! Back to the start.");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1f, 0.7f);
                    return;
                }

                // Reached the top landing platform
                if (player.getLocation().getY() >= targetY - 1
                        && player.getLocation().distance(puzzleCenter) < 5) {
                    player.sendMessage("§a§l✓ You reached the summit!");
                    cancel();
                    quest.advanceTask(player);
                }
            }
        }.runTaskTimer(plugin, 20L, 10L);
    }

    // ---------------------------------------------------------------
    // Task 3: Brazier Puzzle
    // ---------------------------------------------------------------

    public static void startPuzzleTask(HeartsSMPPlugin plugin, DivineTrialManager quest, Player player, Location palaceCenter) {
        player.sendMessage(" ");
        player.sendMessage("§6§l🔥 TASK 3 — THE FOUR BRAZIERS");
        player.sendMessage("§7Light all 4 braziers around the sigil using flint & steel.");
        player.sendMessage(" ");

        // Reset all 4 braziers to unlit in case a previous player already lit them in this shared palace
        Location puzzleCenter = PalaceBuilder.getPuzzleCenter(palaceCenter);
        int[][] brazierOffsets = {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int[] off : brazierOffsets) {
            org.bukkit.block.Block brazier = puzzleCenter.getWorld().getBlockAt(
                    puzzleCenter.getBlockX() + off[0], puzzleCenter.getBlockY(), puzzleCenter.getBlockZ() + off[1]);
            if (brazier.getType() == Material.CAMPFIRE && brazier.getBlockData() instanceof org.bukkit.block.data.Lightable lightable) {
                lightable.setLit(false);
                brazier.setBlockData(lightable);
            }
        }

        litBraziers.put(player.getUniqueId(), new HashSet<>());
        player.getInventory().addItem(new ItemStack(Material.FLINT_AND_STEEL, 1));
    }

    /** Call from a block-ignite / interact handler when the player lights a campfire. */
    public static void onBrazierLit(HeartsSMPPlugin plugin, DivineTrialManager quest, Player player, Location brazierLoc) {
        UUID uuid = player.getUniqueId();
        DivineTrialManager.TrialSession session = quest.getSession(uuid);
        if (session == null) return;

        var data = plugin.getDataManager().get(uuid);
        if (data == null || data.getDivineTrialTaskIndex() != 2) return; // not on the puzzle task

        Set<Location> lit = litBraziers.computeIfAbsent(uuid, k -> new HashSet<>());
        Location key = brazierLoc.getBlock().getLocation();
        lit.add(key);

        player.sendMessage("§6🔥 Brazier lit! (§e" + lit.size() + "/4§6)");
        player.playSound(brazierLoc, Sound.ITEM_FIRECHARGE_USE, 1f, 1f);

        if (lit.size() >= 4) {
            brazierLoc.getWorld().spawnParticle(Particle.FLAME, brazierLoc, 50, 1, 1, 1, 0.05);
            litBraziers.remove(uuid);
            quest.advanceTask(player);
        }
    }
}
