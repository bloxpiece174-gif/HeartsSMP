package com.heartssmp.skills.uncommon;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class CrystallineEdge extends Skill {
    public CrystallineEdge() {
        super("crystalline_edge", "Crystalline Edge", SkillRarity.UNCOMMON,
                "Blade of pure crystal — sharp, brilliant, and shattering.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        if (mastery >= 6) {
            player.getWorld().spawnParticle(Particle.VAULT_CONNECTION, player.getLocation(), 2, 0.3, 0.5, 0.3, 0.02);
        }
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§b💎 Crystal Slash§7: Strike dealing 8 damage, shards fly dealing 2 extra to nearby targets.");
            case 2 -> player.sendMessage("§b💎 Prism Barrier§7: Refract incoming projectiles back to senders for 4s.");
            case 3 -> player.sendMessage("§b💎 Diamond Lance§7: Launch a crystal spear that pierces through multiple enemies.");
            case 4 -> player.sendMessage("§b💎 Shatter§7: Detonate your crystal armor for massive AoE damage.");
            case 5 -> player.sendMessage("§3☠ ULTIMATE — Crystal Palace§7: Encase yourself and all enemies in a crystal dome — deal massive damage to those inside.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "10% chance attacks spawn crystal shards"; }
    @Override public String getMove1Description() { return "Crystal Slash — 8 dmg + 2 AoE shards"; }
    @Override public String getMove2Description() { return "Prism Barrier — reflect projectiles 4s"; }
    @Override public String getMove3Description() { return "Diamond Lance — pierce spear"; }
    @Override public String getMove4Description() { return "Shatter — detonate crystal AoE"; }
    @Override public String getMove5Description() { return "Crystal Palace — dome trap ultimate"; }
}
