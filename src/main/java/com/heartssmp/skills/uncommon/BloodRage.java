package com.heartssmp.skills.uncommon;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class BloodRage extends Skill {
    public BloodRage() {
        super("blood_rage", "Blood Rage", SkillRarity.UNCOMMON,
                "The lower your health, the stronger you become. Rage fuels your power.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        double hp = player.getHealth();
        double max = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        if (hp < max * 0.4) {
            int strengthLevel = mastery >= 9 ? 2 : mastery >= 4 ? 1 : 0;
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, strengthLevel, true, false));
            player.getWorld().spawnParticle(Particle.FALLING_WATER, player.getLocation(), 3, 0.3, 0.5, 0.3, 0);
        }
    }

    @Override
    public void onPlayerKill(Player killer, Player victim, int mastery) {
        killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, false, true));
        killer.getWorld().spawnParticle(Particle.CRIMSON_SPORE, killer.getLocation(), 30, 0.5, 1, 0.5, 0.05);
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§4🩸 Berserker Howl§7: Boost strength +2 for 8s at the cost of 1 heart.");
            case 2 -> player.sendMessage("§4🩸 Blood Frenzy§7: Enter a frenzy state where each hit restores 0.5 hearts.");
            case 3 -> player.sendMessage("§4🩸 Crimson Wave§7: Release a wave of blood energy dealing 8 damage in a line.");
            case 4 -> player.sendMessage("§4🩸 Death Hunger§7: Killing an enemy fully heals you instantly.");
            case 5 -> player.sendMessage("§c☠ ULTIMATE — Bloodlust§7: For 10s, every hit deals triple damage and heals you for 50% of damage dealt.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Gain Strength when below 40% HP"; }
    @Override public String getMove1Description() { return "Berserker Howl — Strength II for 8s (-1 heart)"; }
    @Override public String getMove2Description() { return "Blood Frenzy — steal 0.5 hearts per hit"; }
    @Override public String getMove3Description() { return "Crimson Wave — 8 dmg blood wave"; }
    @Override public String getMove4Description() { return "Death Hunger — full heal on kill"; }
    @Override public String getMove5Description() { return "Bloodlust — 10s triple dmg + 50% lifesteal"; }
}
