package com.heartssmp.gems;

import org.bukkit.entity.Player;

public abstract class Gem {
    private final String id;
    private final String displayName;
    private final GemRarity rarity;
    private final String description;

    public Gem(String id, String displayName, GemRarity rarity, String description) {
        this.id = id;
        this.displayName = displayName;
        this.rarity = rarity;
        this.description = description;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public GemRarity getRarity() { return rarity; }
    public String getDescription() { return description; }

    public String getFormattedName() {
        return rarity.getColor() + displayName;
    }

    public abstract void onPassiveTick(Player player, int mastery);
    public abstract String getSkillDescription(int masteryLevel);

    // Called when gem holder kills a player — override in subclasses if needed
    public void onPlayerKill(Player killer, Player victim, int mastery) {}

    public String getFullInfo(int mastery) {
        StringBuilder sb = new StringBuilder();
        sb.append(getFormattedName())
          .append(" §8[").append(rarity.getDisplayName()).append("]").append("\n");
        sb.append("§7").append(description).append("\n");
        sb.append("§eMastery: §f").append(mastery).append("/3\n");
        for (int i = 1; i <= 3; i++) {
            String prefix = i <= mastery ? "§a✔ " : "§8✘ ";
            sb.append(prefix).append("Mastery ").append(i).append(": ")
              .append(rarity.getColor()).append(getSkillDescription(i)).append("\n");
        }
        return sb.toString();
    }
}
