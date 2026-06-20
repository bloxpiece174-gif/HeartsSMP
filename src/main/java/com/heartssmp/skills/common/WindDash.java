package com.heartssmp.skills.common;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class WindDash extends Skill {
    public WindDash() {
        super("wind_dash", "Wind Dash", SkillRarity.COMMON,
                "Channel wind to dash forward rapidly. Higher mastery = faster dash.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        if (mastery >= 3) {
            player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 1, 0.2, 0, 0.2, 0.01);
        }
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§b💨 Gust Dash§7: Dash forward 5 blocks instantly. 8s cooldown.");
            case 2 -> player.sendMessage("§b💨 Wind Cloak§7: Become invisible briefly as you dash through enemies.");
            case 3 -> player.sendMessage("§b💨 Cyclone Spin§7: Spin at high speed, knocking enemies away in a 3-block radius.");
            case 4 -> player.sendMessage("§b💨 Tempest Step§7: Leave a trail of damaging wind behind you for 3s.");
            case 5 -> player.sendMessage("§3☠ ULTIMATE — Eye of the Storm§7: Become the eye of a tornado, dragging enemies inward and dealing massive damage.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Slightly increased movement speed (scales with mastery)"; }
    @Override public String getMove1Description() { return "Gust Dash — dash 5 blocks, 8s cd"; }
    @Override public String getMove2Description() { return "Wind Cloak — invisible on dash"; }
    @Override public String getMove3Description() { return "Cyclone Spin — knockback in 3 blocks"; }
    @Override public String getMove4Description() { return "Tempest Step — wind trail damage"; }
    @Override public String getMove5Description() { return "Eye of the Storm — tornado AoE ultimate"; }
}
