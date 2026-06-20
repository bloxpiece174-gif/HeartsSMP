package com.heartssmp.gems.epic;

import com.heartssmp.gems.Gem;
import com.heartssmp.gems.GemRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class ShadowGem extends Gem {
    public ShadowGem() {
        super("EPIC_SHADOW", "Shadow Gem", GemRarity.EPIC,
                "A dark pulsing gem from the void. Empowers stealth and assassination.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        if (player.isSneaking()) {
            if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, true, false));
            }
        }
        if (mastery >= 2) {
            player.getWorld().spawnParticle(Particle.SQUID_INK, player.getLocation(), 1, 0.2, 0.3, 0.2, 0);
        }
    }

    @Override
    public String getSkillDescription(int masteryLevel) {
        return switch (masteryLevel) {
            case 1 -> "§8Shadow Cloak§7: Turn fully invisible while sneaking (always active)";
            case 2 -> "§8Death Stab§7: First hit from stealth deals 4x damage (once per stealth, 15s cd)";
            case 3 -> "§8Shadow Realm§7: Enter a shadow dimension making you invisible and intangible for 8s (60s cd)";
            default -> "None";
        };
    }
}
