package com.heartssmp.skills.epic;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class DragonscaleSkin extends Skill {
    public DragonscaleSkin() {
        super("dragonscale_skin", "Dragonscale Skin", SkillRarity.EPIC,
                "Ancient dragon scales cover your body. Near-invincible defense.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        int resistLevel = mastery / 5;
        if (resistLevel > 0 && !player.hasPotionEffect(PotionEffectType.RESISTANCE)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, resistLevel - 1, true, false));
        }
        if (mastery >= 8) {
            player.getWorld().spawnParticle(Particle.DRAGON_BREATH, player.getLocation(), 2, 0.3, 0.3, 0.3, 0.01);
        }
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§2🐉 Scale Harden§7: Temporarily reduce all damage by 60% for 8s.");
            case 2 -> player.sendMessage("§2🐉 Dragon Roar§7: Release a roar that knocks back all enemies and reduces their damage by 30% for 5s.");
            case 3 -> player.sendMessage("§2🐉 Flame Breath§7: Breathe a cone of dragonfire, dealing 10 damage.");
            case 4 -> player.sendMessage("§2🐉 Ancient Scales§7: Regenerate 2 hearts per 10s passively.");
            case 5 -> player.sendMessage("§6☠ ULTIMATE — Dragon Awakening§7: Transform into a full dragon for 15s — massively increased damage, defense, and flight.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Passive damage resistance (scales with mastery)"; }
    @Override public String getMove1Description() { return "Scale Harden — 60% dmg reduce 8s"; }
    @Override public String getMove2Description() { return "Dragon Roar — knockback + -30% enemy dmg"; }
    @Override public String getMove3Description() { return "Flame Breath — dragonfire cone 10 dmg"; }
    @Override public String getMove4Description() { return "Ancient Scales — 2 hearts regen/10s"; }
    @Override public String getMove5Description() { return "Dragon Awakening — full dragon form 15s"; }
}
