package com.heartssmp.gems.divine;

import com.heartssmp.gems.Gem;
import com.heartssmp.gems.GemRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class CelestiaGem extends Gem {
    public CelestiaGem() {
        super("DIVINE_CELESTIA", "Celestia Gem", GemRarity.DIVINE,
                "The rarest gem in existence. Fallen from the stars. Its bearer transcends mortality.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 80, 1, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 1, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 80, 2, true, false));

        if (mastery >= 2) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 80, 2, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 2, true, false));
        }
        if (mastery == 3) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 1, true, false));
        }

        int t = (int) (System.currentTimeMillis() / 400) % 3;
        if (t == 0) player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 5, 0.7, 0.5, 0.7, 0.07);
        else if (t == 1) player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation(), 3, 0.5, 0.5, 0.5, 0.1);
        else player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 2, 0.4, 0.4, 0.4, 0.02);
    }

    @Override
    public String getSkillDescription(int masteryLevel) {
        return switch (masteryLevel) {
            case 1 -> "§e★ Celestial Guard§7: Once per 60s, automatically block a fatal blow (heal to 4 hearts instead, with celestial explosion)";
            case 2 -> "§e★ Star Convergence§7: Summon a constellation above that rains light dealing 6 dmg/s to all enemies in 15 blocks for 10s (90s cd)";
            case 3 -> "§e★ DIVINE TRANSCENDENCE§7: Ascend to your true celestial form for 30s — unkillable, deal 8x damage, fly freely, heal all allies in 20 blocks for 2 hearts/s, and leave a trail of supernovae. Server-wide announcement. (120s cd)";
            default -> "None";
        };
    }
}
