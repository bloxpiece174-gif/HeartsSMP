package com.heartssmp.skills.epic;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class EarthShatter extends Skill {
    public EarthShatter() {
        super("earth_shatter", "Earth Shatter", SkillRarity.EPIC,
                "Shatter the very earth. Nothing stands after you hit the ground.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        // No per-tick passive effect — this skill's passive is handled via on-hit/on-kill logic
    }

    @Override
    public void onPlayerKill(Player killer, Player victim, int mastery) {
        victim.getWorld().createExplosion(victim.getLocation(), 0f, false, false);
        victim.getWorld().spawnParticle(Particle.BLOCK, victim.getLocation(), 100,
                1, 1, 1, 0.5, Material.DIRT.createBlockData());
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§8🪨 Ground Slam§7: Slam the ground, dealing 10 damage and knocking enemies 5 blocks up.");
            case 2 -> player.sendMessage("§8🪨 Tectonic Fissure§7: Create a long crack in the earth that deals 6 damage per second.");
            case 3 -> player.sendMessage("§8🪨 Boulder Throw§7: Hurl a massive boulder dealing 14 damage to the first enemy hit.");
            case 4 -> player.sendMessage("§8🪨 Continental Drift§7: Shift the ground beneath enemies' feet, making them lose footing and take fall damage.");
            case 5 -> player.sendMessage("§6☠ ULTIMATE — World Breaker§7: Cause a magnitude-10 earthquake in a 25-block radius — catastrophic damage, massive knockup, and 10s stun.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Attacks create small tremors, briefly slowing enemies"; }
    @Override public String getMove1Description() { return "Ground Slam — 10 dmg + 5-block knockup"; }
    @Override public String getMove2Description() { return "Tectonic Fissure — ground crack 6 dps"; }
    @Override public String getMove3Description() { return "Boulder Throw — 14 dmg projectile"; }
    @Override public String getMove4Description() { return "Continental Drift — destabilize footing"; }
    @Override public String getMove5Description() { return "World Breaker — 25-block earthquake 10s stun"; }
}
