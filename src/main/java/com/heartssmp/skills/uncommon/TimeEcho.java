package com.heartssmp.skills.uncommon;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.entity.Player;

public class TimeEcho extends Skill {
    public TimeEcho() {
        super("time_echo", "Time Echo", SkillRarity.UNCOMMON,
                "Bend time to your will. Rewind, pause, and accelerate moments.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        // No per-tick passive effect — this skill's passive is handled via on-hit/on-kill logic
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§e⏳ Echo Rewind§7: Rewind your position 3s into the past. 20s cooldown.");
            case 2 -> player.sendMessage("§e⏳ Time Slow§7: Slow all nearby enemies by 80% for 4s.");
            case 3 -> player.sendMessage("§e⏳ Temporal Strike§7: Attack that connects twice — once now, once 2s later.");
            case 4 -> player.sendMessage("§e⏳ Haste Field§7: Create a field that doubles your attack speed for 8s.");
            case 5 -> player.sendMessage("§6☠ ULTIMATE — Time Stop§7: Freeze ALL players in a 15-block radius for 5s while you move freely.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "5% chance to phase-dodge by stepping 0.5s back"; }
    @Override public String getMove1Description() { return "Echo Rewind — rewind position 3s, 20s cd"; }
    @Override public String getMove2Description() { return "Time Slow — 80% slow enemies 4s"; }
    @Override public String getMove3Description() { return "Temporal Strike — double-hit across time"; }
    @Override public String getMove4Description() { return "Haste Field — double attack speed 8s"; }
    @Override public String getMove5Description() { return "Time Stop — freeze all 15-block radius 5s"; }
}
