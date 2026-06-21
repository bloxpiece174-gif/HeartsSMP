package com.heartssmp.god;

import com.heartssmp.HeartsSMPPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;

public class DivineWorldManager {
    private final HeartsSMPPlugin plugin;
    private boolean divineWorldActive = false;

    public DivineWorldManager(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isDivineWorldActive() { return divineWorldActive; }

    public void activateDivineWorld() {
        if (divineWorldActive) return;
        divineWorldActive = true;

        World world = plugin.getServer().getWorlds().get(0);

        // Server wide announcement
        plugin.getServer().broadcastMessage("");
        plugin.getServer().broadcastMessage("§4§l✦ ════════════════════════════════════════════════ ✦");
        plugin.getServer().broadcastMessage("§6§k§lXX§r §4§l        THE DIVINE WORLD DESCENDS        §6§k§lXX§r");
        plugin.getServer().broadcastMessage("§e      God has claimed this realm. Nothing is the same.");
        plugin.getServer().broadcastMessage("§7      A new chapter begins. Survive. Or perish.");
        plugin.getServer().broadcastMessage("§4§l✦ ════════════════════════════════════════════════ ✦");
        plugin.getServer().broadcastMessage("");

        // Dramatic effects on all players
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 0.3f);
            p.sendTitle("§4§lTHE DIVINE WORLD", "§6God has transformed this realm", 20, 80, 20);
        }

        // Change world settings
        new BukkitRunnable() {
            public void run() {
                // Set permanent thunder & dark sky
                world.setStorm(true);
                world.setThundering(true);
                world.setWeatherDuration(Integer.MAX_VALUE);
                world.setThunderDuration(Integer.MAX_VALUE);

                // Set time to midnight
                world.setTime(18000);
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

                // Spawn gold/white structures around spawn
                buildDivineAltar(world, world.getSpawnLocation());

                // Continuous divine world effects
                startDivineWorldTick();

                // Announce the storyline
                new BukkitRunnable() {
                    public void run() {
                        plugin.getServer().broadcastMessage("§6✦ §e§lTHE DIVINE WORLD STORYLINE §6✦");
                        plugin.getServer().broadcastMessage("§7 God has descended. The world's rules have changed.");
                        plugin.getServer().broadcastMessage("§7 Divine monsters now roam the land.");
                        plugin.getServer().broadcastMessage("§7 Find the §6Golden Altar §7at spawn to begin the new chapter.");
                        plugin.getServer().broadcastMessage("§7 Only those with §6Divine power §7can survive what comes next.");
                    }
                }.runTaskLater(plugin, 200L);
            }
        }.runTaskLater(plugin, 100L);
    }

    private void buildDivineAltar(World world, Location center) {
        // Build a gold/white altar at spawn
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        // Platform — gold blocks
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                world.getBlockAt(cx + x, cy - 1, cz + z).setType(Material.GOLD_BLOCK);
            }
        }

        // Center — beacon
        world.getBlockAt(cx, cy, cz).setType(Material.BEACON);

        // Pillars — quartz
        int[][] corners = {{-5, -5}, {5, -5}, {-5, 5}, {5, 5}};
        for (int[] corner : corners) {
            for (int y = 0; y <= 5; y++) {
                world.getBlockAt(cx + corner[0], cy + y, cz + corner[1]).setType(Material.QUARTZ_PILLAR);
            }
            // Gold on top
            world.getBlockAt(cx + corner[0], cy + 6, cz + corner[1]).setType(Material.GOLD_BLOCK);
            // Lantern on top
            world.getBlockAt(cx + corner[0], cy + 7, cz + corner[1]).setType(Material.LANTERN);
        }

        // Ring of end rods
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            int rx = (int) (cx + 4 * Math.cos(angle));
            int rz = (int) (cz + 4 * Math.sin(angle));
            world.getBlockAt(rx, cy, rz).setType(Material.END_ROD);
            world.getBlockAt(rx, cy + 1, rz).setType(Material.END_ROD);
        }

        // Lightning to mark the altar
        world.strikeLightningEffect(center);
    }

    private void startDivineWorldTick() {
        new BukkitRunnable() {
            public void run() {
                if (!divineWorldActive) { cancel(); return; }
                World world = plugin.getServer().getWorlds().get(0);

                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    // Ambient divine particles on all players
                    p.getWorld().spawnParticle(Particle.END_ROD,
                            p.getLocation().add(0, 2, 0), 2, 0.5, 0.3, 0.5, 0.01);

                    // Random divine lightning nearby
                    if (Math.random() < 0.02) {
                        double offsetX = (Math.random() - 0.5) * 40;
                        double offsetZ = (Math.random() - 0.5) * 40;
                        Location strike = p.getLocation().add(offsetX, 0, offsetZ);
                        world.strikeLightningEffect(strike);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 40L);
    }
}
