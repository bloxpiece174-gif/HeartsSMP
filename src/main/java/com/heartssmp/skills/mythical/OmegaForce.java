package com.heartssmp.skills.mythical;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class OmegaForce extends Skill {
    public OmegaForce() {
        super("omega_force", "Omega Force", SkillRarity.MYTHICAL,
                "The pinnacle of physical power. A force of nature in human form.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        if (!player.hasPotionEffect(PotionEffectType.STRENGTH)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 80, mastery / 5, true, false));
        }
        if (!player.hasPotionEffect(PotionEffectType.RESISTANCE)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, mastery / 7, true, false));
        }
        if (mastery >= 10) {
            player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, player.getLocation(), 1, 0, 0, 0, 0);
        }
    }

    @Override
    public void onPlayerKill(Player killer, Player victim, int mastery) {
        victim.getWorld().createExplosion(victim.getLocation(), 2f, false, false);
        victim.getWorld().spawnParticle(Particle.EXPLOSION, victim.getLocation(), 10, 1, 1, 1, 0.5);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.5f);
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§d💥 Omega Strike§7: A single punch dealing 25 damage. Cannot be blocked. 10s cooldown.");
            case 2 -> player.sendMessage("§d💥 Shockwave Slam§7: Slam the ground creating a shockwave dealing 15 damage in 12-block radius.");
            case 3 -> player.sendMessage("§d💥 Unstoppable Charge§7: Charge through any obstacles, knocking back everything in your path for 30 blocks.");
            case 4 -> player.sendMessage("§d💥 Titan's Wrath§7: Triple all stats for 15s. Become an unstoppable juggernaut.");
            case 5 -> player.sendMessage("§5☠ ULTIMATE — OMEGA ANNIHILATION§7: Release your full power in a 40-block radius — everything takes 50 damage. Structures shake. The earth cracks. Nothing survives.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Passive Strength + Resistance (scales to Strength V / Resistance III)"; }
    @Override public String getMove1Description() { return "Omega Strike — 25 dmg unblockable, 10s cd"; }
    @Override public String getMove2Description() { return "Shockwave Slam — 15 dmg 12-block radius"; }
    @Override public String getMove3Description() { return "Unstoppable Charge — 30-block charge rampage"; }
    @Override public String getMove4Description() { return "Titan's Wrath — triple all stats 15s"; }
    @Override public String getMove5Description() { return "OMEGA ANNIHILATION — 50 dmg 40-block radius"; }
}
