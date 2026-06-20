package com.heartssmp.gems.common;

import com.heartssmp.gems.Gem;
import com.heartssmp.gems.GemRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class EmberGem extends Gem {
    public EmberGem() {
        super("COMMON_EMBER", "Ember Gem", GemRarity.COMMON,
                "A warm ember that crackles with heat. Provides fire resistance and minor combat boosts.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 0, true, false));
        if (mastery >= 2) {
            player.getWorld().spawnParticle(Particle.SMALL_FLAME, player.getLocation(), 1, 0.3, 0, 0.3, 0);
        }
    }

    @Override
    public String getSkillDescription(int masteryLevel) {
        return switch (masteryLevel) {
            case 1 -> "§6Flame Aura§7: Attackers take 1 fire damage per hit back";
            case 2 -> "§6Ember Burst§7: Release fire burst (8 dmg AoE, 30s cd)";
            case 3 -> "§6Inferno Awakening§7: Once per death, survive with 1 heart surrounded by a ring of fire";
            default -> "None";
        };
    }
}
