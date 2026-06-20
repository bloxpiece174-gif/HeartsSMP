package com.heartssmp.skills.uncommon;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class SpectralShield extends Skill {
    public SpectralShield() {
        super("spectral_shield", "Spectral Shield", SkillRarity.UNCOMMON,
                "A ghost barrier absorbs damage and disorients attackers.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        if (mastery >= 5) {
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 2, 0.5, 0.8, 0.5, 0.02);
        }
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§f👻 Phase Barrier§7: Absorb next 3 hits completely. 20s cooldown.");
            case 2 -> player.sendMessage("§f👻 Haunt§7: Teleport behind an attacker and deal 5 damage.");
            case 3 -> player.sendMessage("§f👻 Spectral Detonation§7: Release the absorbed energy as an AoE blast.");
            case 4 -> player.sendMessage("§f👻 Ghost Walk§7: Phase through blocks for 3s, become untargetable.");
            case 5 -> player.sendMessage("§7☠ ULTIMATE — Haunted Ground§7: Turn the ground into a spectral field that deals continuous damage and slows.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "5% chance to phase-dodge attacks"; }
    @Override public String getMove1Description() { return "Phase Barrier — absorb 3 hits, 20s cd"; }
    @Override public String getMove2Description() { return "Haunt — teleport behind + 5 dmg"; }
    @Override public String getMove3Description() { return "Spectral Detonation — AoE energy blast"; }
    @Override public String getMove4Description() { return "Ghost Walk — phase through blocks 3s"; }
    @Override public String getMove5Description() { return "Haunted Ground — spectral DoT field"; }
}
