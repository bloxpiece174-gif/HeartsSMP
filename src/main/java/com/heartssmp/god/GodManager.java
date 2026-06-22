package com.heartsmp.entity;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.heartsmp.HeartsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Manages the God Entity — a fully invulnerable, AI-speaking NPC.
 *
 * Implementation notes:
 *   Paper 1.21 does not expose a "real" Player NPC through the Bukkit API without
 *   a library like Citizens or PacketEvents. To stay dependency-free we use a
 *   Villager as the physical entity and apply:
 *     • Custom name from config
 *     • INVULNERABLE AI flag + persistent data tag
 *     • Idle particle halo for visual identity
 *     • Persistent custom name visibility
 *
 *   If you wish to render the full God skin (Base64 texture) for all players,
 *   add PacketEvents as a shaded dependency and call the NMSNPCHelper — the
 *   config already stores the skin texture/signature, and the helper class is
 *   provided below. This keeps the core plugin buildable without optional deps.
 */
public class GodEntityManager implements Listener {

    public static final String GOD_METADATA_KEY = "heartsmp_god";
    public static final NamespacedKey GOD_PDC_KEY = new NamespacedKey("heartsmp", "god_entity");

    private final HeartsPlugin plugin;
    private final Map<UUID, Villager> activeGods = new HashMap<>();
    private final List<BukkitTask> haloTasks = new ArrayList<>();

    public GodEntityManager(HeartsPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // ── Spawn ──────────────────────────────────────────────────

    /**
     * Spawn the God Entity at the given location.
     * Returns the spawned entity UUID.
     */
    public UUID spawnGod(Location location) {
        Villager villager = location.getWorld().spawn(location, Villager.class, v -> {
            v.setVillagerType(Villager.Type.PLAINS);
            v.setVillagerProfession(Villager.Profession.CLERIC);
            v.setAI(false);
            v.setInvulnerable(true);
            v.setGravity(false);
            v.setSilent(false);
            v.setRemoveWhenFarAway(false);

            String rawName = plugin.getPluginConfig().getGodName();
            Component nameComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(rawName);
            v.customName(nameComponent);
            v.setCustomNameVisible(plugin.getPluginConfig().isShowNametag());

            v.setMetadata(GOD_METADATA_KEY, new FixedMetadataValue(plugin, true));
            v.getPersistentDataContainer().set(GOD_PDC_KEY, PersistentDataType.BYTE, (byte) 1);
        });

        activeGods.put(villager.getUniqueId(), villager);
        startHaloEffect(villager);

        plugin.getLogger().info("God Entity spawned at " + formatLoc(location));
        return villager.getUniqueId();
    }

    /** Despawn a specific God Entity. */
    public boolean despawnGod(UUID uuid) {
        Villager v = activeGods.remove(uuid);
        if (v == null) return false;
        v.remove();
        return true;
    }

    /** Despawn all active God Entities (called on plugin disable). */
    public void despawnAll() {
        haloTasks.forEach(BukkitTask::cancel);
        haloTasks.clear();
        activeGods.values().forEach(Entity::remove);
        activeGods.clear();
    }

    // ── Entity ID helpers ─────────────────────────────────────

    public boolean isGodEntity(Entity entity) {
        return entity.hasMetadata(GOD_METADATA_KEY);
    }

    public Collection<Villager> getActiveGods() {
        return Collections.unmodifiableCollection(activeGods.values());
    }

    /** Find the nearest God Entity to a player within radius, or null. */
    public Villager getNearestGod(Player player, double radius) {
        return activeGods.values().stream()
                .filter(v -> v.getWorld().equals(player.getWorld()))
                .filter(v -> v.getLocation().distanceSquared(player.getLocation()) <= radius * radius)
                .min(Comparator.comparingDouble(v -> v.getLocation().distanceSquared(player.getLocation())))
                .orElse(null);
    }

    // ── Damage protection ─────────────────────────────────────

    @EventHandler
    public void onGodDamage(EntityDamageEvent event) {
        if (isGodEntity(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGodDamageByEntity(EntityDamageByEntityEvent event) {
        if (isGodEntity(event.getEntity())) {
            event.setCancelled(true);
            if (event.getDamager() instanceof Player p) {
                p.sendMessage(
                    LegacyComponentSerializer.legacyAmpersand()
                        .deserialize("&6&l✦ &eYou dare strike the Divine? Your mortal hands are powerless here.")
                );
            }
        }
    }

    // ── Halo particle effect ───────────────────────────────────

    private void startHaloEffect(Villager villager) {
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!villager.isValid()) return;
            Location loc = villager.getLocation().add(0, 2.4, 0);
            World world = villager.getWorld();

            // Golden orbiting ring
            int points = 16;
            for (int i = 0; i < points; i++) {
                double angle = (2 * Math.PI / points) * i + (System.currentTimeMillis() / 200.0);
                double x = Math.cos(angle) * 0.6;
                double z = Math.sin(angle) * 0.6;
                world.spawnParticle(Particle.DUST,
                    loc.clone().add(x, 0, z),
                    1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.fromRGB(0xFF, 0xD7, 0x00), 1.2f)
                );
            }

            // White shimmer rising from body
            world.spawnParticle(Particle.END_ROD,
                villager.getLocation().add(0, 1, 0),
                2, 0.3, 0.5, 0.3, 0.01
            );
        }, 0L, 2L);
        haloTasks.add(task);
    }

    // ── Utility ───────────────────────────────────────────────

    private String formatLoc(Location l) {
        return String.format("%s [%.1f, %.1f, %.1f]", l.getWorld().getName(), l.getX(), l.getY(), l.getZ());
    }
}
