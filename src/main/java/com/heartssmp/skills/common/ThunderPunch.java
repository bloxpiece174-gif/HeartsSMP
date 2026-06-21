package com.heartssmp.skills.common;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class ThunderPunch extends Skill {
    public ThunderPunch() {
        super("thunder_punch", "Thunder Punch", SkillRarity.COMMON,
                "Channel lightning into your fists. Devastating shock attacks.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        // No per-tick passive effect — this skill's passive is handled via on-hit/on-kill logic
    }

    @Override
    public void onPlayerKill(Player killer, Player victim, int mastery) {
        victim.getWorld().strikeLightningEffect(victim.getLocation());
        victim.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, victim.getLocation(), 40, 0.5, 1, 0.5, 0.1);
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§e⚡ Shock Fist§7: Next punch deals 5 lightning damage and stuns for 1s.");
            case 2 -> player.sendMessage("§e⚡ Thunder Clap§7: Clap sends shockwave dealing 4 damage in a cone.");
            case 3 -> player.sendMessage("§e⚡ Overcharge§7: Overload your fists with lightning, doubling punch damage for 5s.");
            case 4 -> player.sendMessage("§e⚡ Ball Lightning§7: Launch a ball of lightning that bounces between enemies.");
            case 5 -> player.sendMessage("§e☠ ULTIMATE — God Fist§7: One devastating punch that deals 20 damage, stuns for 3s and sends lightning everywhere.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Chance to strike lightning on hit"; }
    @Override public String getMove1Description() { return "Shock Fist — 5 lightning dmg + 1s stun"; }
    @Override public String getMove2Description() { return "Thunder Clap — shockwave cone 4 dmg"; }
    @Override public String getMove3Description() { return "Overcharge — double punch dmg 5s"; }
    @Override public String getMove4Description() { return "Ball Lightning — bouncing lightning"; }
    @Override public String getMove5Description() { return "God Fist — 20 dmg + 3s stun ultimate"; }
}
