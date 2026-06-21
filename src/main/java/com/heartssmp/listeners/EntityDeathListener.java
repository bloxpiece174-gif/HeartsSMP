package com.heartssmp.listeners;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;

public class EntityDeathListener implements Listener {
    private final HeartsSMPPlugin plugin;

    public EntityDeathListener(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player killer)) return;
        PlayerData data = plugin.getDataManager().get(killer.getUniqueId());
        if (data == null) return;

        // ── SOUL REAPER — kill grants strength ─────────────────────────
        if (data.hasSkill("soul_reaper") && data.getSkillMastery("soul_reaper") >= 5) {
            killer.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 0, false, true));
            killer.getWorld().spawnParticle(Particle.SOUL, event.getEntity().getLocation(), 20, 0.5, 0.5, 0.5, 0.05);
        }

        // ── EARTH SHATTER — kill explosion ─────────────────────────────
        if (data.hasSkill("earth_shatter")) {
            killer.getWorld().createExplosion(event.getEntity().getLocation(), 0f, false, false);
            killer.getWorld().spawnParticle(Particle.BLOCK, event.getEntity().getLocation(), 80,
                    1, 0.5, 1, 0.3, Material.DIRT.createBlockData());
        }

        // ── STORM CALLER — 15% chance to strike lightning on kill ──────
        if (data.hasSkill("storm_caller") && Math.random() < 0.15) {
            killer.getWorld().strikeLightning(event.getEntity().getLocation());
        }

        // ── PHOENIX RISE — kill heals 1 heart at mastery 9+ ───────────
        if (data.hasSkill("phoenix_rise") && data.getSkillMastery("phoenix_rise") >= 9) {
            double maxHp = killer.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            killer.setHealth(Math.min(maxHp, killer.getHealth() + 2.0));
            killer.getWorld().spawnParticle(Particle.FLAME, killer.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.05);
        }

        // ── VOID STEP — kill grants brief invisibility at mastery 6+ ───
        if (data.hasSkill("void_step") && data.getSkillMastery("void_step") >= 6) {
            killer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, false, false));
        }

        // ── DRAGONSCALE — kill regen at mastery 12+ ────────────────────
        if (data.hasSkill("dragonscale_skin") && data.getSkillMastery("dragonscale_skin") >= 12) {
            killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1, false, true));
        }

        // ── PVP KILL — use existing HeartManager logic ─────────────────
        if (event.getEntity() instanceof Player) {
            plugin.getHeartManager().onKillPlayer(killer);
        } else {
            plugin.getHeartManager().onKillMob(killer);
        }
    }
}
