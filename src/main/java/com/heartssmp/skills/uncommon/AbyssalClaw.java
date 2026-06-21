package com.heartssmp.skills.uncommon;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class AbyssalClaw extends Skill {
    public AbyssalClaw() {
        super("abyssal_claw", "Abyssal Claw", SkillRarity.UNCOMMON,
                "Claws forged in the void. Tear through armor and reality alike.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        // No per-tick passive effect — this skill's passive is handled via on-hit/on-kill logic
    }

    @Override
    public void onPlayerKill(Player killer, Player victim, int mastery) {
        killer.getWorld().spawnParticle(Particle.SMALL_FLAME, victim.getLocation(), 50, 1, 1, 1, 0.02);
        killer.getWorld().spawnParticle(Particle.SQUID_INK, victim.getLocation(), 30, 0.5, 1, 0.5, 0.05);
        killer.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 0.8f);
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§8🌑 Rend§7: Slash with void claws, dealing 7 damage and ignoring 30% armor.");
            case 2 -> player.sendMessage("§8🌑 Void Grip§7: Drag enemy toward you from 8 blocks away.");
            case 3 -> player.sendMessage("§8🌑 Abyssal Slash§7: Dash forward slashing everything in a line.");
            case 4 -> player.sendMessage("§8🌑 Corruption§7: Claws corrupt enemy, reducing their max health by 2 for 10s.");
            case 5 -> player.sendMessage("§0☠ ULTIMATE — Rift Tear§7: Tear open a rift that drags all nearby enemies inside for 5s of AoE damage.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Attacks ignore 10% armor (mastery scales to 25%)"; }
    @Override public String getMove1Description() { return "Rend — 7 dmg, 30% armor ignore"; }
    @Override public String getMove2Description() { return "Void Grip — pull enemy from 8 blocks"; }
    @Override public String getMove3Description() { return "Abyssal Slash — dash-slash line"; }
    @Override public String getMove4Description() { return "Corruption — reduce enemy max HP 10s"; }
    @Override public String getMove5Description() { return "Rift Tear — dimension trap AoE ultimate"; }
}
