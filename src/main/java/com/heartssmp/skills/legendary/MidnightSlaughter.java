package com.heartssmp.skills.legendary;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class MidnightSlaughter extends Skill {
    public MidnightSlaughter() {
        super("midnight_slaughter", "Midnight Slaughter", SkillRarity.LEGENDARY,
                "When darkness falls, you become death itself. Unstoppable at night.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        long time = player.getWorld().getTime();
        boolean isNight = time >= 12300 && time <= 23850;
        if (isNight) {
            if (!player.hasPotionEffect(PotionEffectType.STRENGTH)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 80, mastery >= 8 ? 2 : 1, true, false));
            }
            if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1, true, false));
            }
            if (mastery >= 6) {
                player.getWorld().spawnParticle(Particle.SQUID_INK, player.getLocation(), 2, 0.3, 0.5, 0.3, 0);
            }
        }
    }

    @Override
    public void onPlayerKill(Player killer, Player victim, int mastery) {
        killer.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, victim.getLocation(), 60, 0.5, 1, 0.5, 0.05);
        killer.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PHANTOM_DEATH, 0.8f, 0.7f);
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§0🌑 Shadow Execution§7: Triple damage on any enemy below 30% health at night.");
            case 2 -> player.sendMessage("§0🌑 Night Veil§7: Become completely invisible during night for 10s.");
            case 3 -> player.sendMessage("§0🌑 Midnight Surge§7: Instantly reset all cooldowns at midnight (once per night).");
            case 4 -> player.sendMessage("§0🌑 Blood Moon§7: Force the server to night-mode for 60s. All your stats doubled.");
            case 5 -> player.sendMessage("§8☠ ULTIMATE — Eternal Night§7: Plunge the world into darkness for 3 minutes. During this time, you are unkillable and deal 5x damage.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Strength + Speed at night (scales with mastery)"; }
    @Override public String getMove1Description() { return "Shadow Execution — 3x dmg on <30% HP night"; }
    @Override public String getMove2Description() { return "Night Veil — invisible at night 10s"; }
    @Override public String getMove3Description() { return "Midnight Surge — reset all cooldowns 1x/night"; }
    @Override public String getMove4Description() { return "Blood Moon — force night, double all stats"; }
    @Override public String getMove5Description() { return "Eternal Night — 3min darkness, unkillable, 5x dmg"; }
}
