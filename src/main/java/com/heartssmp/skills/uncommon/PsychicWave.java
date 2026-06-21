package com.heartssmp.skills.uncommon;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class PsychicWave extends Skill {
    public PsychicWave() {
        super("psychic_wave", "Psychic Wave", SkillRarity.UNCOMMON,
                "Your mind is a weapon. Crush, push, and distort with mental force.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        // No per-tick passive effect — this skill's passive is handled via on-hit/on-kill logic
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§d🔮 Mind Blast§7: Send a psychic shockwave that disorients enemies for 3s.");
            case 2 -> player.sendMessage("§d🔮 Telekinesis§7: Lift enemies into the air and slam them for fall damage.");
            case 3 -> player.sendMessage("§d🔮 Psionic Shield§7: Create a mental barrier that reflects 40% of damage.");
            case 4 -> player.sendMessage("§d🔮 Mind Control§7: Force an enemy to stand still for 5s.");
            case 5 -> player.sendMessage("§5☠ ULTIMATE — Psychic Collapse§7: Implode the minds of all nearby enemies, dealing 12 damage and confusing them for 8s.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "5% chance to stagger attackers mentally"; }
    @Override public String getMove1Description() { return "Mind Blast — disorient enemies 3s"; }
    @Override public String getMove2Description() { return "Telekinesis — lift + slam for fall dmg"; }
    @Override public String getMove3Description() { return "Psionic Shield — reflect 40% dmg"; }
    @Override public String getMove4Description() { return "Mind Control — freeze enemy 5s"; }
    @Override public String getMove5Description() { return "Psychic Collapse — 12 dmg + confuse 8s AoE"; }
}
