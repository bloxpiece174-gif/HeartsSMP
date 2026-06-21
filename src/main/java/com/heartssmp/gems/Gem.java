package com.heartssmp.gems;

import org.bukkit.entity.Player;

public abstract class Gem {
    public abstract String getId();
    public abstract String getDisplayName();
    public abstract String getDescription();
    public abstract GemRarity getRarity();
    public abstract String getSkillDescription(int mastery);
    public abstract void onPassiveTick(Player player, int mastery);
    public abstract void onMasteryUp(Player player, int newMastery);

    // Called when gem holder kills a player — override in subclasses if needed
    public void onKill(Player killer, Player victim, int mastery) {}

    public String getFullInfo(int mastery) {
        StringBuilder sb = new StringBuilder();
        sb.append(getRarity().getColor()).append("✦ ").append(getDisplayName())
          .append(" §8[").append(getRarity().getDisplayName()).append("]").append("\n");
        sb.append("§7").append(getDescription()).append("\n");
        sb.append("§eMastery: §f").append(mastery).append("/3\n");
        for (int i = 1; i <= 3; i++) {
            String prefix = i <= mastery ? "§a✔ " : "§8✘ ";
            sb.append(prefix).append("Mastery ").append(i).append(": ")
              .append(getRarity().getColor()).append(getSkillDescription(i)).append("\n");
        }
        return sb.toString();
    }
}
