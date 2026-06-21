package com.heartssmp.skills.uncommon;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class HuntersMark extends Skill {
    public HuntersMark() {
        super("hunters_mark", "Hunter's Mark", SkillRarity.UNCOMMON,
                "Mark your prey. They cannot hide. They cannot run.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        // No per-tick passive effect — this skill's passive is handled via on-hit/on-kill logic
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§6🎯 Mark Target§7: Mark a player — deal +25% damage to them for 12s.");
            case 2 -> player.sendMessage("§6🎯 Tracking Arrow§7: Fire an arrow that reveals target's location for 15s.");
            case 3 -> player.sendMessage("§6🎯 Expose Weakness§7: Marked target's defense is reduced by 30%.");
            case 4 -> player.sendMessage("§6🎯 Relentless Pursuit§7: Gain Speed III when moving toward marked target.");
            case 5 -> player.sendMessage("§6☠ ULTIMATE — Execution§7: Instantly deal 50% of marked target's max HP as damage.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "See targets' health bars at all times"; }
    @Override public String getMove1Description() { return "Mark Target — +25% dmg to marked 12s"; }
    @Override public String getMove2Description() { return "Tracking Arrow — reveal target 15s"; }
    @Override public String getMove3Description() { return "Expose Weakness — -30% target defense"; }
    @Override public String getMove4Description() { return "Relentless Pursuit — Speed III toward marked"; }
    @Override public String getMove5Description() { return "Execution — deal 50% of target max HP"; }
}
