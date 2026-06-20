package com.heartssmp.skills.common;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class FrostStep extends Skill {
    public FrostStep() {
        super("frost_step", "Frost Step", SkillRarity.COMMON,
                "Leave trails of frost. Slow enemies who dare follow you.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation(), 1, 0.3, 0, 0.3, 0);
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§b❄ Ice Slide§7: Slide rapidly across the ground, leaving ice in your path.");
            case 2 -> player.sendMessage("§b❄ Frost Nova§7: Freeze all enemies in a 4-block radius for 2s.");
            case 3 -> player.sendMessage("§b❄ Blizzard Step§7: Create a localized snowstorm that blinds enemies.");
            case 4 -> player.sendMessage("§b❄ Glacial Armor§7: Encase yourself in ice that absorbs 3 hits.");
            case 5 -> player.sendMessage("§3☠ ULTIMATE — Absolute Zero§7: Flash-freeze an entire area, shattering enemies for massive damage.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Footsteps leave frost, slowing chasers"; }
    @Override public String getMove1Description() { return "Ice Slide — rapid icy glide"; }
    @Override public String getMove2Description() { return "Frost Nova — freeze 4 block radius 2s"; }
    @Override public String getMove3Description() { return "Blizzard Step — blind enemies"; }
    @Override public String getMove4Description() { return "Glacial Armor — ice shield 3 hits"; }
    @Override public String getMove5Description() { return "Absolute Zero — shatter-freeze ultimate"; }
}
