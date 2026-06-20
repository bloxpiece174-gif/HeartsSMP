package com.heartssmp.gems.common;

import com.heartssmp.gems.Gem;
import com.heartssmp.gems.GemRarity;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class StoneGem extends Gem {
    public StoneGem() {
        super("COMMON_STONE", "Stone Gem", GemRarity.COMMON,
                "A solid grey gem radiating earthen energy. Durability and steadiness above all.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        if (!player.hasPotionEffect(PotionEffectType.RESISTANCE)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 0, true, false));
        }
        if (mastery >= 3) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 1, true, false));
        }
    }

    @Override
    public String getSkillDescription(int masteryLevel) {
        return switch (masteryLevel) {
            case 1 -> "§8Earth Shield§7: Block 15% of incoming damage passively";
            case 2 -> "§8Rock Burst§7: Send rock shards flying in all directions (6 dmg each, 20s cd)";
            case 3 -> "§8Stone Giant§7: Double max HP and become immune to knockback for 15s (90s cd)";
            default -> "None";
        };
    }
}
