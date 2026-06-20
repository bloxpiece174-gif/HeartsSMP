package com.heartssmp.gems.legendary;

import com.heartssmp.gems.Gem;
import com.heartssmp.gems.GemRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class AuroraGem extends Gem {
    public AuroraGem() {
        super("LEGENDARY_AURORA", "Aurora Gem", GemRarity.LEGENDARY,
                "A radiant gem shimmering with northern lights. Grants divine energy and healing power.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        if (player.getHealth() < player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue() * 0.6) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, mastery - 1, true, false));
        }
        if (mastery >= 2) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 80, 1, true, false));
        }
        if (mastery >= 3) {
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 3, 0.5, 0.3, 0.5, 0.05);
        }
    }

    @Override
    public String getSkillDescription(int masteryLevel) {
        return switch (masteryLevel) {
            case 1 -> "§e Aurora Heal§7: Heal 5 hearts when below 30% HP (60s cd)";
            case 2 -> "§e Radiant Burst§7: Heal yourself for 4 hearts and deal 6 dmg to all enemies in 8 blocks (45s cd)";
            case 3 -> "§e Light of Aurora§7: Resurrect yourself upon death with 8 hearts (once per life, server broadcast)";
            default -> "None";
        };
    }
}
