package com.heartssmp.skills.epic;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class StormCaller extends Skill {
    public StormCaller() {
        super("storm_caller", "Storm Caller", SkillRarity.EPIC,
                "Command the fury of storms. Thunder, wind, and rain answer your call.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        // No per-tick passive effect — this skill's passive is handled via on-hit/on-kill logic
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§9⛈ Summon Storm§7: Call a localized storm that deals 3 lightning hits to enemies within 8 blocks over 5s.");
            case 2 -> player.sendMessage("§9⛈ Gale Force§7: Unleash a wind blast that throws enemies 10 blocks into the air.");
            case 3 -> player.sendMessage("§9⛈ Thunder Aegis§7: Encase yourself in lightning — any melee attacker gets struck by lightning.");
            case 4 -> player.sendMessage("§9⛈ Eye of the Hurricane§7: Create a safe zone — allies take 50% less damage; enemies take 50% more.");
            case 5 -> player.sendMessage("§1☠ ULTIMATE — Apocalypse Storm§7: Summon a catastrophic storm in 20-block radius — lightning, wind, rain — for 12s of devastation.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "15% chance to strike lightning on killing blow"; }
    @Override public String getMove1Description() { return "Summon Storm — 3 lightning hits 8 blocks 5s"; }
    @Override public String getMove2Description() { return "Gale Force — launch enemies 10 blocks up"; }
    @Override public String getMove3Description() { return "Thunder Aegis — lightning retaliation shield"; }
    @Override public String getMove4Description() { return "Eye of Hurricane — ally buff/enemy debuff zone"; }
    @Override public String getMove5Description() { return "Apocalypse Storm — 20-block storm 12s"; }
}
