package com.heartssmp.skills.common;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class IronSkin extends Skill {
    public IronSkin() {
        super("iron_skin", "Iron Skin", SkillRarity.COMMON,
                "Your skin hardens with each battle. Grants passive resistance.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        if (mastery >= 1) {
            int amplifier = Math.min((mastery - 1) / 3, 2);
            if (!player.hasPotionEffect(PotionEffectType.RESISTANCE)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, amplifier, true, false));
            }
        }
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§7🛡 Hardened Shell§7: Absorb 20% of incoming damage for 5 seconds on activation.");
            case 2 -> player.sendMessage("§7🛡 Iron Bash§7: Bash enemies away with your armored body, dealing 3 damage and knocking back.");
            case 3 -> player.sendMessage("§7🛡 Fortress Stance§7: Become immovable and reduce all damage by 50% for 4 seconds.");
            case 4 -> player.sendMessage("§7🛡 Reflective Armor§7: 15% chance to reflect melee damage back to attacker.");
            case 5 -> player.sendMessage("§8☠ ULTIMATE — Titanfall§7: Drop from the sky as iron, dealing massive AoE damage on landing.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Passive resistance (scales with mastery)"; }
    @Override public String getMove1Description() { return "Hardened Shell — 20% dmg absorb for 5s"; }
    @Override public String getMove2Description() { return "Iron Bash — knockback + 3 dmg"; }
    @Override public String getMove3Description() { return "Fortress Stance — 50% dmg reduce 4s"; }
    @Override public String getMove4Description() { return "Reflective Armor — 15% reflect melee"; }
    @Override public String getMove5Description() { return "Titanfall — AoE slam from above"; }
}
