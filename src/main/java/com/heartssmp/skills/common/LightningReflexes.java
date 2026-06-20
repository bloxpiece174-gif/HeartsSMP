package com.heartssmp.skills.common;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class LightningReflexes extends Skill {
    public LightningReflexes() {
        super("lightning_reflexes", "Lightning Reflexes", SkillRarity.COMMON,
                "React faster than anyone. Speed and agility define you.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        int level = Math.min(mastery / 3, 2);
        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, level, true, false));
        }
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§e⚡ Quick Strike§7: Attack 40% faster for 6s.");
            case 2 -> player.sendMessage("§e⚡ Thunder Blink§7: Teleport to target enemy instantly and strike.");
            case 3 -> player.sendMessage("§e⚡ Static Field§7: Electrify nearby enemies for 5s, dealing damage over time.");
            case 4 -> player.sendMessage("§e⚡ Chain Lightning§7: Attacks chain to 2 nearby enemies.");
            case 5 -> player.sendMessage("§e☠ ULTIMATE — Storm Surge§7: Move at blinding speed for 8s, dealing lightning damage on every hit.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Passive speed boost (scales with mastery)"; }
    @Override public String getMove1Description() { return "Quick Strike — 40% faster attacks 6s"; }
    @Override public String getMove2Description() { return "Thunder Blink — teleport + strike"; }
    @Override public String getMove3Description() { return "Static Field — electrify nearby 5s"; }
    @Override public String getMove4Description() { return "Chain Lightning — chain to 2 targets"; }
    @Override public String getMove5Description() { return "Storm Surge — lightning rampage 8s"; }
}
