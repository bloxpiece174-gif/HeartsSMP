package com.heartssmp.skills.epic;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class PhoenixRise extends Skill {
    public PhoenixRise() {
        super("phoenix_rise", "Phoenix Rise", SkillRarity.EPIC,
                "You cannot be destroyed. Rise from the ashes with renewed fury.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        if (player.getHealth() < 4 && mastery >= 6) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 1, true, false));
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 5, 0.3, 0.5, 0.3, 0.05);
        }
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§6🔥 Rebirth Flame§7: If you would die, instead heal to 4 hearts. 120s cooldown.");
            case 2 -> player.sendMessage("§6🔥 Blazing Wings§7: Sprout fire wings — fly for 8s, immunity to fall damage.");
            case 3 -> player.sendMessage("§6🔥 Ash Storm§7: Exhale a storm of burning ash that blinds and burns all in a cone.");
            case 4 -> player.sendMessage("§6🔥 Eternal Flame§7: Leave a burning trail that heals you when walked over.");
            case 5 -> player.sendMessage("§c☠ ULTIMATE — Phoenix Ignition§7: Explode in a supernova of fire — instantly kill all burning enemies and heal to full.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Emergency regeneration at low HP (mastery 6+)"; }
    @Override public String getMove1Description() { return "Rebirth Flame — survive death once, 120s cd"; }
    @Override public String getMove2Description() { return "Blazing Wings — fly 8s + fall immunity"; }
    @Override public String getMove3Description() { return "Ash Storm — blind + burn cone"; }
    @Override public String getMove4Description() { return "Eternal Flame — healing fire trail"; }
    @Override public String getMove5Description() { return "Phoenix Ignition — supernova kill + full heal"; }
}
