package com.heartssmp.skills.epic;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class SoulReaper extends Skill {
    public SoulReaper() {
        super("soul_reaper", "Soul Reaper", SkillRarity.EPIC,
                "Harvest the souls of the fallen. Grow stronger with every death.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        // No per-tick passive effect — this skill's passive is handled via on-hit/on-kill logic
    }

    @Override
    public void onPlayerKill(Player killer, Player victim, int mastery) {
        killer.getWorld().spawnParticle(Particle.SOUL, victim.getLocation(), 40, 0.5, 1, 0.5, 0.05);
        killer.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.7f, 0.6f);
        if (mastery >= 5) {
            killer.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 0, false, true));
        }
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§8💀 Soul Drain§7: Drain 3 hearts from target and add them to yourself.");
            case 2 -> player.sendMessage("§8💀 Spectral Army§7: Summon 3 soul phantoms to fight for you for 10s.");
            case 3 -> player.sendMessage("§8💀 Death Mark§7: Mark an enemy — if they die within 15s, you absorb their remaining life force.");
            case 4 -> player.sendMessage("§8💀 Reaper's Scythe§7: Launch a spectral scythe that passes through walls dealing 15 damage.");
            case 5 -> player.sendMessage("§0☠ ULTIMATE — Soul Harvest§7: Instantly kill all mobs within 20 blocks and absorb their souls for massive buffs for 30s.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Killing grants stacking strength buff for 10s"; }
    @Override public String getMove1Description() { return "Soul Drain — steal 3 hearts from target"; }
    @Override public String getMove2Description() { return "Spectral Army — 3 soul phantoms 10s"; }
    @Override public String getMove3Description() { return "Death Mark — absorb life force on death"; }
    @Override public String getMove4Description() { return "Reaper's Scythe — phase scythe 15 dmg"; }
    @Override public String getMove5Description() { return "Soul Harvest — kill mobs + massive buffs 30s"; }
}
