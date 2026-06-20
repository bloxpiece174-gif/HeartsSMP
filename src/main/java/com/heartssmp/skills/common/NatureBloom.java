package com.heartssmp.skills.common;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class NatureBloom extends Skill {
    public NatureBloom() {
        super("nature_bloom", "Nature Bloom", SkillRarity.COMMON,
                "Harness the power of nature to heal and sustain yourself.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        if (mastery >= 3 && player.getHealth() < player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue() - 2) {
            if (!player.hasPotionEffect(PotionEffectType.REGENERATION)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0, true, false));
            }
        }
        if (mastery >= 6) {
            player.getWorld().spawnParticle(Particle.CHERRY_LEAVES, player.getLocation(), 2, 0.5, 0.5, 0.5, 0);
        }
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§a🌿 Healing Bloom§7: Instantly heal 4 hearts. 15s cooldown.");
            case 2 -> player.sendMessage("§a🌿 Vine Entangle§7: Entangle an enemy in vines, rooting for 4s.");
            case 3 -> player.sendMessage("§a🌿 Nature's Wrath§7: Command thorns — reflect ALL melee damage for 5s.");
            case 4 -> player.sendMessage("§a🌿 Forest Guardian§7: Summon a nature guardian that fights alongside you for 10s.");
            case 5 -> player.sendMessage("§2☠ ULTIMATE — World Tree§7: Summon a colossal tree that heals allies and roots all enemies in range.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Passive regeneration when hurt (mastery 3+)"; }
    @Override public String getMove1Description() { return "Healing Bloom — heal 4 hearts, 15s cd"; }
    @Override public String getMove2Description() { return "Vine Entangle — root enemy 4s"; }
    @Override public String getMove3Description() { return "Nature's Wrath — reflect melee 5s"; }
    @Override public String getMove4Description() { return "Forest Guardian — summon ally 10s"; }
    @Override public String getMove5Description() { return "World Tree — AoE heal + root ultimate"; }
}
