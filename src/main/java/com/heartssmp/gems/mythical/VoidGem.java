package com.heartssmp.gems.mythical;

import com.heartssmp.gems.Gem;
import com.heartssmp.gems.GemRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class VoidGem extends Gem {
    public VoidGem() {
        super("MYTHICAL_VOID", "Void Gem", GemRarity.MYTHICAL,
                "A gem pulled from the void between dimensions. Terrifying power — space bends around it.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 5, 0.5, 0.5, 0.5, 0.3);
        if (mastery >= 2) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0, true, false));
        }
    }

    @Override
    public String getSkillDescription(int masteryLevel) {
        return switch (masteryLevel) {
            case 1 -> "§5Void Tear§7: Rip a rift in space — teleport any enemy into the air (8 blocks up), 10s cd";
            case 2 -> "§5Dimension Pull§7: Collapse space — pull ALL entities in 15 blocks toward you for 5 dmg, 20s cd";
            case 3 -> "§5Void Collapse§7: Implode a 20-block sphere — enemies take 20 dmg and are stunned for 5s, 90s cd. Particles devour the area.";
            default -> "None";
        };
    }
}
