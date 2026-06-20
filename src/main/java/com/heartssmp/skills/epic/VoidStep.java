package com.heartssmp.skills.epic;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class VoidStep extends Skill {
    public VoidStep() {
        super("void_step", "Void Step", SkillRarity.EPIC,
                "Step through the void itself. Appear anywhere, from nothing.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        if (mastery >= 7) {
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 3, 0.3, 0.5, 0.3, 0.3);
        }
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§8🌀 Blink§7: Teleport 12 blocks in your look direction instantly.");
            case 2 -> player.sendMessage("§8🌀 Void Swap§7: Swap positions with any player within 15 blocks.");
            case 3 -> player.sendMessage("§8🌀 Phase Through§7: Pass through solid blocks for 5s.");
            case 4 -> player.sendMessage("§8🌀 Dimensional Anchor§7: Lock enemy in place — they cannot teleport or move for 4s.");
            case 5 -> player.sendMessage("§0☠ ULTIMATE — Void Realm§7: Drag target into the void dimension for 8s — deal continuous damage before expelling them.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Every 30s automatically dodge one hit via mini-blink"; }
    @Override public String getMove1Description() { return "Blink — teleport 12 blocks forward"; }
    @Override public String getMove2Description() { return "Void Swap — swap positions 15 blocks"; }
    @Override public String getMove3Description() { return "Phase Through — walk through blocks 5s"; }
    @Override public String getMove4Description() { return "Dimensional Anchor — root + no-tp 4s"; }
    @Override public String getMove5Description() { return "Void Realm — dimension trap 8s DoT"; }
}
