package com.heartssmp.skills.common;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.*;

public class InfernoFist extends Skill {
    public InfernoFist() {
        super("inferno_fist", "Inferno Fist", SkillRarity.COMMON,
                "Your punches ignite enemies. Higher mastery = more fire damage.");
    }

    @Override
    public void onPlayerKill(Player killer, Player victim, int mastery) {
        World w = killer.getWorld();
        w.spawnParticle(Particle.FLAME, victim.getLocation(), 30 + mastery * 5, 0.5, 1, 0.5, 0.05);
        w.playSound(victim.getLocation(), Sound.ENTITY_BLAZE_DEATH, 1, 1);
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        if (mastery >= 5) {
            player.getWorld().spawnParticle(Particle.SMALL_FLAME, player.getLocation(), 2, 0.3, 0, 0.3, 0);
        }
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§c🔥 Flame Punch§7: Your attacks deal +2 fire damage and set enemies ablaze for 3s.");
            case 2 -> player.sendMessage("§c🔥 Ember Ring§7: Release a ring of fire around you, dealing 4 damage to nearby enemies.");
            case 3 -> player.sendMessage("§c🔥 Magma Strike§7: Channel lava energy into a devastating ground slam.");
            case 4 -> player.sendMessage("§c🔥 Inferno Aura§7: Passively ignite enemies within 5 blocks.");
            case 5 -> player.sendMessage("§4☠ ULTIMATE — Volcanic Wrath§7: Summon a rain of fireballs in a 10-block radius.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Attacks have chance to ignite (scales with mastery)"; }
    @Override public String getMove1Description() { return "Flame Punch — +2 fire dmg, ignite 3s"; }
    @Override public String getMove2Description() { return "Ember Ring — fire ring, 4 dmg nearby"; }
    @Override public String getMove3Description() { return "Magma Strike — ground slam"; }
    @Override public String getMove4Description() { return "Inferno Aura — ignite within 5 blocks"; }
    @Override public String getMove5Description() { return "Volcanic Wrath — fireball rain 10-block radius"; }
}
