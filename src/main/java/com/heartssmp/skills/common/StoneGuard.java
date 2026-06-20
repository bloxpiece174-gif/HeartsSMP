package com.heartssmp.skills.common;

import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public class StoneGuard extends Skill {
    public StoneGuard() {
        super("stone_guard", "Stone Guard", SkillRarity.COMMON,
                "Earth's protection flows through you. Endurance and durability.");
    }

    @Override
    public void onPassiveTick(Player player, int mastery) {
        if (mastery >= 2 && !player.hasPotionEffect(PotionEffectType.SATURATION)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 40, 0, true, false));
        }
    }

    @Override
    public void onMoveUnlock(Player player, int moveIndex) {
        switch (moveIndex) {
            case 1 -> player.sendMessage("§8🪨 Rock Wall§7: Summon a stone barrier that blocks projectiles for 5s.");
            case 2 -> player.sendMessage("§8🪨 Earthen Grab§7: Roots enemy in place with stone for 3s.");
            case 3 -> player.sendMessage("§8🪨 Stone Rain§7: Hurl boulders at a target, dealing 6 damage.");
            case 4 -> player.sendMessage("§8🪨 Seismic Pulse§7: Send a shockwave through the ground, knocking enemies down.");
            case 5 -> player.sendMessage("§8☠ ULTIMATE — Golem Form§7: Transform into a stone golem with doubled health and massive strength for 10s.");
        }
    }

    @Override public String getPassiveDescription(int mastery) { return "Passive saturation regeneration"; }
    @Override public String getMove1Description() { return "Rock Wall — block projectiles 5s"; }
    @Override public String getMove2Description() { return "Earthen Grab — root enemy 3s"; }
    @Override public String getMove3Description() { return "Stone Rain — boulder barrage 6 dmg"; }
    @Override public String getMove4Description() { return "Seismic Pulse — ground shockwave"; }
    @Override public String getMove5Description() { return "Golem Form — double HP + strength 10s"; }
}
