package com.heartssmp.skills.mythical;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class TimeWarp extends Skill {
    public TimeWarp() {
        super("time_warp", "Time Warp", SkillRarity.MYTHICAL,
                "You exist outside of time. Bend reality, rewrite outcomes, defy death itself.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        if (mastery >= 8) {
            player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, player.getLocation(), 3, 0.5, 0.5, 0.5, 0.05);
        }
    }

    @Override
    public void onPlayerKill(Player killer, Player victim, int mastery) {
        killer.getWorld().spawnParticle(Particle.REVERSE_PORTAL, victim.getLocation(), 80, 0.5, 1, 0.5, 0.1);
        killer.getWorld().playSound(victim.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 0.5f);
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§5⌛ Temporal Rewind§7: Rewind your health and position to what it was 10s ago. 60s cd.");
            case 2 -> player.sendMessage("§5⌛ Time Dilation§7: Create a zone where time is 10x slower for all enemies for 8s. You move normally.");
            case 3 -> player.sendMessage("§5⌛ Timeline Shift§7: Predict the future — avoid the next 3 attacks automatically.");
            case 4 -> player.sendMessage("§5⌛ Temporal Explosion§7: Detonate a time-bomb that deals damage retroactively — all damage to you in the past 30s is redirected to nearby enemies.");
            case 5 -> player.sendMessage("§d☠ ULTIMATE — CHRONOS COLLAPSE§7: Collapse time in a 50-block radius. ALL players are rewound 30 seconds in time (health, position). You are unaffected. Ultimate chaos.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "10% chance to phase-step any hit (mastery scales to 25%)"; }
    @Override public String getMove1Description() { return "Temporal Rewind — rewind HP + position 10s, 60s cd"; }
    @Override public String getMove2Description() { return "Time Dilation — 10x slow zone 8s"; }
    @Override public String getMove3Description() { return "Timeline Shift — dodge next 3 attacks"; }
    @Override public String getMove4Description() { return "Temporal Explosion — redirect past damage to enemies"; }
    @Override public String getMove5Description() { return "CHRONOS COLLAPSE — rewind all in 50 blocks 30s"; }
}
