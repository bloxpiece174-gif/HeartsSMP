package com.heartssmp.skills.legendary;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class CelestialBarrage extends Skill {
    public CelestialBarrage() {
        super("celestial_barrage", "Celestial Barrage", SkillRarity.LEGENDARY,
                "Rain destruction from the heavens. Your enemies pray for mercy.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        // No per-tick passive effect — this skill's passive is handled via on-hit/on-kill logic
    }

    @Override
    public void onPlayerKill(Player killer, Player victim, int mastery) {
        for (int i = 0; i < mastery / 3 + 2; i++) {
            victim.getWorld().strikeLightningEffect(victim.getLocation().add(
                    (Math.random() - 0.5) * 3, 0, (Math.random() - 0.5) * 3));
        }
        victim.getWorld().spawnParticle(Particle.FIREWORK, victim.getLocation(), 80, 1, 1, 1, 0.3);
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§e☀ Star Fall§7: Call 5 meteors to rain down in a 10-block area dealing 8 dmg each.");
            case 2 -> player.sendMessage("§e☀ Solar Flare§7: Emit a blinding burst of solar energy, dealing 10 damage and blinding all in 8 blocks.");
            case 3 -> player.sendMessage("§e☀ Celestial Lance§7: Throw a spear of pure starlight that pierces through everything for 18 damage.");
            case 4 -> player.sendMessage("§e☀ Constellation Burst§7: Summon star-patterns that orbit you, each dealing 4 damage to anything they touch.");
            case 5 -> player.sendMessage("§6☠ ULTIMATE — Divine Judgment§7: Open the heavens and call down a laser of pure divine light dealing 40 damage to a target. Cannot miss.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Kill blows call lightning strikes (more at higher mastery)"; }
    @Override public String getMove1Description() { return "Star Fall — 5 meteors, 8 dmg each"; }
    @Override public String getMove2Description() { return "Solar Flare — 10 dmg + blind 8 blocks"; }
    @Override public String getMove3Description() { return "Celestial Lance — pierce spear 18 dmg"; }
    @Override public String getMove4Description() { return "Constellation Burst — orbiting star dmg"; }
    @Override public String getMove5Description() { return "Divine Judgment — 40 dmg divine laser, no miss"; }
}
