package com.heartssmp.skills.uncommon;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class MoltenCore extends Skill {
    public MoltenCore() {
        super("molten_core", "Molten Core", SkillRarity.UNCOMMON,
                "Your core burns like a volcano. Devastating heat-based power.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        if (mastery >= 4) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 0, true, false));
        }
        if (mastery >= 8) {
            player.getWorld().spawnParticle(Particle.LAVA, player.getLocation(), 1, 0.3, 0, 0.3, 0);
        }
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§6🌋 Lava Burst§7: Explode lava around you, dealing 5 dmg and setting enemies on fire.");
            case 2 -> player.sendMessage("§6🌋 Magma Shield§7: Coat yourself in magma — attackers take 3 fire damage per hit.");
            case 3 -> player.sendMessage("§6🌋 Eruption§7: Trigger a volcanic eruption at target location, raining fire for 4s.");
            case 4 -> player.sendMessage("§6🌋 Overheat§7: Release all heat at once — massive AoE fire damage to all nearby.");
            case 5 -> player.sendMessage("§c☠ ULTIMATE — Supervolcano§7: Trigger a catastrophic eruption that devastates a 15-block radius for 8s.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Fire resistance at mastery 4+"; }
    @Override public String getMove1Description() { return "Lava Burst — 5 dmg + fire AoE"; }
    @Override public String getMove2Description() { return "Magma Shield — 3 fire dmg to attackers"; }
    @Override public String getMove3Description() { return "Eruption — fire rain 4s at target"; }
    @Override public String getMove4Description() { return "Overheat — massive heat AoE release"; }
    @Override public String getMove5Description() { return "Supervolcano — 15-block devastation 8s"; }
}
