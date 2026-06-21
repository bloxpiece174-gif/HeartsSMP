package com.heartssmp.skills.legendary;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class HellstormGate extends Skill {
    public HellstormGate() {
        super("hellstorm_gate", "Hellstorm Gate", SkillRarity.LEGENDARY,
                "Open a gate to hell itself. Demonic power beyond comprehension.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        // No per-tick passive effect — this skill's passive is handled via on-hit/on-kill logic
    }

    @Override
    public void onPlayerKill(Player killer, Player victim, int mastery) {
        victim.getWorld().spawnParticle(Particle.FLAME, victim.getLocation(), 100, 1, 1.5, 1, 0.1);
        victim.getWorld().spawnParticle(Particle.SOUL, victim.getLocation(), 30, 0.5, 1, 0.5, 0.05);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.6f, 0.5f);
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§c🔥 Hellfire Bolt§7: Launch a bolt from hell dealing 14 damage and leaving a trail of hellfire for 5s.");
            case 2 -> player.sendMessage("§c🔥 Gate of Demons§7: Summon 5 demonic entities to attack enemies for 12s.");
            case 3 -> player.sendMessage("§c🔥 Infernal Field§7: Create a zone of hellfire — 8 damage per second for 8s to all enemies inside.");
            case 4 -> player.sendMessage("§c🔥 Brimstone Rain§7: Call down brimstone from above — AoE, 20s duration, massive damage.");
            case 5 -> player.sendMessage("§4☠ ULTIMATE — Hell's Opening§7: Rip open a gate to hell in a 30-block radius for 10s — devastating damage, summons, and fire everywhere. You become immune.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Kills have chance to summon a demonic entity ally"; }
    @Override public String getMove1Description() { return "Hellfire Bolt — 14 dmg + hellfire trail"; }
    @Override public String getMove2Description() { return "Gate of Demons — 5 demon summons 12s"; }
    @Override public String getMove3Description() { return "Infernal Field — 8 dps hellfire zone 8s"; }
    @Override public String getMove4Description() { return "Brimstone Rain — massive AoE 20s"; }
    @Override public String getMove5Description() { return "Hell's Opening — 30-block catastrophe 10s"; }
}
