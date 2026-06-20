package com.heartssmp.skills.legendary;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class DivineSpeed extends Skill {
    public DivineSpeed() {
        super("divine_speed", "Divine Speed", SkillRarity.LEGENDARY,
                "Move faster than the eye can track. A blur of unstoppable force.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        int level = Math.min(mastery / 4, 3);
        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, level, true, false));
        }
        if (mastery >= 10) {
            player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation(), 1, 0, 0, 0, 0);
        }
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§6✨ Afterimage§7: Move so fast you leave afterimages that confuse enemies for 5s.");
            case 2 -> player.sendMessage("§6✨ Sonic Boom§7: Dash at supersonic speed, dealing 12 damage to everything you pass through.");
            case 3 -> player.sendMessage("§6✨ Flash Step§7: Instantly appear behind target anywhere in 20-block range.");
            case 4 -> player.sendMessage("§6✨ Rapid Fire§7: Attack 3x per second for 6s, each hit dealing normal damage.");
            case 5 -> player.sendMessage("§6☠ ULTIMATE — Godspeed§7: Become invisible and move at 10x speed for 10s, hitting every enemy in your path for 8 damage.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Permanent high-tier Speed aura (scales to Speed IV)"; }
    @Override public String getMove1Description() { return "Afterimage — confuse with speed clones 5s"; }
    @Override public String getMove2Description() { return "Sonic Boom — dash through all enemies 12 dmg"; }
    @Override public String getMove3Description() { return "Flash Step — instant teleport behind target"; }
    @Override public String getMove4Description() { return "Rapid Fire — 3 attacks/sec for 6s"; }
    @Override public String getMove5Description() { return "Godspeed — invisible 10x speed, 8 dmg all hit"; }
}
