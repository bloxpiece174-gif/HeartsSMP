package com.heartssmp.quest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 * Procedurally builds the floating Divine Palace arena around a fixed center point.
 * Built lazily the first time anyone is teleported in, then left in place
 * (subsequent trials reuse the same structure).
 *
 * Layout (relative to center, all on the same Y unless noted):
 *  - A circular quartz/purpur platform (radius 20) as the main floor.
 *  - 4 tall corner towers (purpur pillar) marking the arena bounds, used as the combat gauntlet ring.
 *  - A parkour causeway of floating blocks leading up and away from the main floor (+24 Y climb).
 *  - A small puzzle room at the top with 4 unlit brazier (campfire) positions around a sigil floor.
 */
public class PalaceBuilder {

    public static void buildIfNeeded(World world, Location center) {
        Location marker = center.clone().add(0, -1, 0);
        if (marker.getBlock().getType() == Material.QUARTZ_BLOCK) {
            return; // already built
        }
        build(world, center);
    }

    private static void build(World world, Location center) {
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        // Main circular floor, radius 20
        int radius = 20;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist <= radius) {
                    Material mat = (dist > radius - 2) ? Material.PURPUR_BLOCK : Material.QUARTZ_BLOCK;
                    world.getBlockAt(cx + dx, cy - 1, cz + dz).setType(mat);
                }
            }
        }

        // 4 corner towers at the cardinal edges of the circle
        int[][] towerOffsets = {{radius - 1, 0}, {-(radius - 1), 0}, {0, radius - 1}, {0, -(radius - 1)}};
        for (int[] off : towerOffsets) {
            for (int h = 0; h < 12; h++) {
                world.getBlockAt(cx + off[0], cy + h, cz + off[1]).setType(Material.PURPUR_PILLAR);
            }
            world.getBlockAt(cx + off[0], cy + 12, cz + off[1]).setType(Material.END_ROD);
        }

        // Parkour causeway: a zig-zag line of floating end-stone blocks climbing 24 blocks up and out
        int px = cx;
        int pz = cz + radius + 4;
        int py = cy;
        boolean toggle = false;
        for (int step = 0; step < 24; step++) {
            world.getBlockAt(px, py, pz).setType(Material.END_STONE);
            px += toggle ? 2 : -1;
            pz += 2;
            py += 1;
            toggle = !toggle;
        }
        // Small landing platform at the top of the parkour
        Location puzzleCenter = new Location(world, px, py, pz);
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                world.getBlockAt(puzzleCenter.getBlockX() + dx, puzzleCenter.getBlockY() - 1,
                        puzzleCenter.getBlockZ() + dz).setType(Material.PURPUR_BLOCK);
            }
        }
        // Mark puzzle brazier spots with UNLIT campfires at the 4 corners of the landing platform.
        // Campfires default to lit=true when placed via code, so we must explicitly force them unlit
        // or the player will have nothing left to "light" during the puzzle task.
        int[][] brazierOffsets = {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int[] off : brazierOffsets) {
            org.bukkit.block.Block brazier = world.getBlockAt(puzzleCenter.getBlockX() + off[0], puzzleCenter.getBlockY(),
                    puzzleCenter.getBlockZ() + off[1]);
            brazier.setType(Material.CAMPFIRE);
            if (brazier.getBlockData() instanceof org.bukkit.block.data.Lightable lightable) {
                lightable.setLit(false);
                brazier.setBlockData(lightable);
            }
        }

        // Glowstone sigil in the middle of the puzzle floor for atmosphere
        world.getBlockAt(puzzleCenter.getBlockX(), puzzleCenter.getBlockY() - 1, puzzleCenter.getBlockZ())
                .setType(Material.SEA_LANTERN);
    }

    /** Mirrors the math in build() so other classes can find the puzzle landing point without rebuilding. */
    public static Location getPuzzleCenter(Location center) {
        int px = center.getBlockX();
        int pz = center.getBlockZ() + 24;
        int py = center.getBlockY();
        boolean toggle = false;
        for (int step = 0; step < 24; step++) {
            px += toggle ? 2 : -1;
            pz += 2;
            py += 1;
            toggle = !toggle;
        }
        return new Location(center.getWorld(), px, py, pz);
    }
}
