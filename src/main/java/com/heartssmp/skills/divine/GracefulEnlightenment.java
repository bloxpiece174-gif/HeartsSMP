package com.heartssmp.skills.divine;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class GracefulEnlightenment extends Skill {

    public GracefulEnlightenment() {
        super("graceful_enlightenment", "Graceful Enlightenment", SkillRarity.DIVINE,
                "The singular divine grace granted only to the worthy. Beyond all mortal limits.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        // Grants all positive potion effects at high levels
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 1, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0, true, false));

        if (mastery >= 10) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 2, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 1, true, false));
        }

        if (mastery == 15) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 3, true, false));
        }

        int particleType = (int)(System.currentTimeMillis() / 500) % 3;
        if (particleType == 0)
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 3, 0.5, 0.5, 0.5, 0.05);
        else if (particleType == 1)
            player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 2, 0.3, 0.3, 0.3, 0.01);
        else
            player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 0.5, 0), 2, 0.3, 0.5, 0.3, 0.05);
    }

    @Override
    public void onPlayerKill(Player killer, Player victim, int mastery) {
        victim.getWorld().spawnParticle(Particle.END_ROD, victim.getLocation(), 200, 1, 2, 1, 0.2);
        victim.getWorld().spawnParticle(Particle.FIREWORK, victim.getLocation(), 100, 1, 1, 1, 0.5);
        victim.getWorld().playSound(victim.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.5f);
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        if (moveIndex == 1) {
            // Divine grace fanfare, played once when the skill is first unlocked
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 300, 2, 2, 2, 0.3);
            player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 100, 1.5, 1.5, 1.5, 0.2);
            player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 2f, 0.8f);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 2f, 1.5f);
            player.getServer().broadcast(
                net.kyori.adventure.text.Component.text(
                    "✨ " + player.getName() + " has obtained DIVINE GRACE — Graceful Enlightenment! ✨",
                    net.kyori.adventure.text.format.TextColor.color(0xFFFF55)
                )
            );
        }

        switch (moveIndex) {
            case 1 -> player.sendMessage("§e✨ DIVINE GRACE I — Aura of Protection§7: All allies within 10 blocks gain Resistance II and Regeneration II permanently while you're nearby.");
            case 2 -> player.sendMessage("§e✨ DIVINE GRACE II — Celestial Smite§7: Call divine judgment on a target — dealing 30 damage, permanently reducing their max hearts by 1, and healing you for 10 hearts.");
            case 3 -> player.sendMessage("§e✨ DIVINE GRACE III — Enlightened Form§7: Transform into your divine form for 20s — you become immune, deal 10x damage, and your presence alone deals 5 damage/s to all enemies.");
            case 4 -> player.sendMessage("§e✨ DIVINE GRACE IV — Grace Ascendant§7: Grant divine blessings to all allies on the server — full health, all effects, and 5 extra hearts for 30s.");
            case 5 -> player.sendMessage("§e☠ DIVINE ULTIMATE — ABSOLUTE GRACE§7: You become a deity for 60 seconds. You are unkillable, deal 20x damage, fly freely, and all enemies within 50 blocks lose 5 hearts/s until the effect ends. The earth trembles. The server goes dark. Then light.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Permanent Str II, Speed II, Resistance, Regeneration (Mastery 15: + Absorption IV)"; }
    @Override public String getMove1Description() { return "Divine Grace I — ally Resistance II + Regen II aura"; }
    @Override public String getMove2Description() { return "Divine Grace II — 30 dmg + steal 1 max heart + heal 10"; }
    @Override public String getMove3Description() { return "Enlightened Form — immune + 10x dmg + 5 dps aura 20s"; }
    @Override public String getMove4Description() { return "Grace Ascendant — buff ALL allies serverwide"; }
    @Override public String getMove5Description() { return "ABSOLUTE GRACE — 60s deity form, 20x dmg, 50-block 5 hp/s drain"; }
}
