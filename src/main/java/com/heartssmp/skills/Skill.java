package com.heartssmp.skills;

import org.bukkit.entity.Player;

public abstract class Skill {
    public abstract String getId();
    public abstract String getDisplayName();
    public abstract String getDescription();
    public abstract SkillRarity getRarity();
    public abstract int getMaxMastery();
    public abstract String getMasteryDescription(int mastery);
    public abstract void onPassiveTick(Player player, int mastery);
    public abstract void onUnlock(Player player);

    // Called when skill holder kills a player — override if needed
    public void onKill(Player killer, Player victim, int mastery) {}

    public String getFullInfo(int mastery) {
        StringBuilder sb = new StringBuilder();
        sb.append(getRarity().getColor()).append("⚔ ").append(getDisplayName())
          .append(" §8[").append(getRarity().getDisplayName()).append("]").append("\n");
        sb.append("§7").append(getDescription()).append("\n");
        sb.append("§eMastery: §f").append(mastery).append("/").append(getMaxMastery()).append("\n");
        sb.append(getRarity().getColor()).append(getMasteryDescription(mastery));
        return sb.toString();
    }
}
