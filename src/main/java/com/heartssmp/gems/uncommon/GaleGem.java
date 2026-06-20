package com.heartssmp.gems.uncommon;

import com.heartssmp.gems.Gem;
import com.heartssmp.gems.GemRarity;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class GaleGem extends Gem {
    public GaleGem() {
        super("UNCOMMON_GALE", "Gale Gem", GemRarity.UNCOMMON,
                "A swirling wind crystal that grants unmatched agility and aerial mastery.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        int level = mastery >= 2 ? 1 : 0;
        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, level, true, false));
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 80, mastery, true, false));
    }

    @Override
    public String getSkillDescription(int masteryLevel) {
        return switch (masteryLevel) {
            case 1 -> "§b Wind Step§7: Double-jump (jump again mid-air), 5s cd";
            case 2 -> "§b Gale Dash§7: Dash horizontally 10 blocks in look direction, 10s cd";
            case 3 -> "§b Storm Flight§7: Fly freely for 20s, creating a windstorm that damages nearby enemies for 4 dmg/s";
            default -> "None";
        };
    }
}
