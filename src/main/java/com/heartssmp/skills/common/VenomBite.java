package com.heartssmp.skills.common;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class VenomBite extends Skill {
    public VenomBite() {
        super("venom_bite", "Venom Bite", SkillRarity.COMMON,
                "Your strikes carry toxins. Enemies are slowly poisoned.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        // No per-tick passive effect — this skill's passive is handled via on-hit/on-kill logic
    }

    @Override
    public void onPlayerKill(Player killer, Player victim, int mastery) {
        killer.getWorld().spawnParticle(Particle.FALLING_SPORE_BLOSSOM, victim.getLocation(), 20, 0.5, 1, 0.5, 0.02);
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§2☠ Toxic Strike§7: Your next hit poisons for 5s.");
            case 2 -> player.sendMessage("§2☠ Venom Cloud§7: Release a poison cloud in a 3-block radius.");
            case 3 -> player.sendMessage("§2☠ Neurotoxin§7: Slow and weaken the enemy alongside poison.");
            case 4 -> player.sendMessage("§2☠ Venomous Aura§7: Passively poison attackers who hit you.");
            case 5 -> player.sendMessage("§a☠ ULTIMATE — Plague Lord§7: Infect all nearby enemies with a lethal plague that spreads on hit.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Chance to poison on hit (scales with mastery)"; }
    @Override public String getMove1Description() { return "Toxic Strike — next hit poisons 5s"; }
    @Override public String getMove2Description() { return "Venom Cloud — poison cloud 3 blocks"; }
    @Override public String getMove3Description() { return "Neurotoxin — slow + weaken + poison"; }
    @Override public String getMove4Description() { return "Venomous Aura — poison attackers"; }
    @Override public String getMove5Description() { return "Plague Lord — spreading lethal infection"; }
}
