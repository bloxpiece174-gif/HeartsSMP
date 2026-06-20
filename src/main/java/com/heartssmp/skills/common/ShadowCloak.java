package com.heartssmp.skills.common;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class ShadowCloak extends Skill {
    public ShadowCloak() {
        super("shadow_cloak", "Shadow Cloak", SkillRarity.COMMON,
                "Melt into darkness. Become invisible and strike from the shadows.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        if (mastery >= 4) {
            player.getWorld().spawnParticle(Particle.SQUID_INK, player.getLocation(), 1, 0.2, 0.5, 0.2, 0);
        }
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§8🌑 Shadow Step§7: Turn invisible for 4s. 12s cooldown.");
            case 2 -> player.sendMessage("§8🌑 Darkness Wave§7: Blind all enemies in 6-block radius for 3s.");
            case 3 -> player.sendMessage("§8🌑 Shadowbind§7: Root an enemy in their own shadow for 3s.");
            case 4 -> player.sendMessage("§8🌑 Dark Echo§7: Create a shadow clone that distracts enemies.");
            case 5 -> player.sendMessage("§0☠ ULTIMATE — Void Consumption§7: Enter a shadow realm for 6s, becoming untargetable and appearing behind any target.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Reduced visibility while sneaking"; }
    @Override public String getMove1Description() { return "Shadow Step — invisible 4s, 12s cd"; }
    @Override public String getMove2Description() { return "Darkness Wave — blind 6-block radius 3s"; }
    @Override public String getMove3Description() { return "Shadowbind — root in shadow 3s"; }
    @Override public String getMove4Description() { return "Dark Echo — shadow clone decoy"; }
    @Override public String getMove5Description() { return "Void Consumption — untargetable shadow realm 6s"; }
}
