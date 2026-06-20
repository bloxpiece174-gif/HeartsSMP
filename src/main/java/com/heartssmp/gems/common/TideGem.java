package com.heartssmp.gems.common;

import com.heartssmp.gems.Gem;
import com.heartssmp.gems.GemRarity;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class TideGem extends Gem {
    public TideGem() {
        super("COMMON_TIDE", "Tide Gem", GemRarity.COMMON,
                "A cool, blue gem humming with ocean energy. Bestows water breathing and swift movement.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 80, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 80, 0, true, false));
        if (mastery >= 2) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 0, true, false));
        }
    }

    @Override
    public String getSkillDescription(int masteryLevel) {
        return switch (masteryLevel) {
            case 1 -> "§9Tidal Pull§7: Pull an enemy toward you (20 blocks, 15s cd)";
            case 2 -> "§9Whirlpool§7: Create a water vortex trapping enemies in 5-block radius for 3s";
            case 3 -> "§9Tsunami§7: Summon a tidal wave knocking all enemies back 10 blocks and dealing 10 dmg";
            default -> "None";
        };
    }
}
