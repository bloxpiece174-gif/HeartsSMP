package com.heartssmp.skills;

import org.bukkit.entity.Player;

public abstract class Skill {
    private final String id;
    private final String displayName;
    private final SkillRarity rarity;
    private final String description;

    public Skill(String id, String displayName, SkillRarity rarity, String description) {
        this.id = id;
        this.displayName = displayName;
        this.rarity = rarity;
        this.description = description;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public SkillRarity getRarity() { return rarity; }
    public String getDescription() { return description; }

    public String getFormattedName() {
        return rarity.getColorCode() + displayName;
    }

    public abstract void onPassiveTick(Player player, int mastery);
    public abstract void onMoveUnlock(Player player, int moveIndex);

    // Called when skill holder kills a player — override if needed
    public void onPlayerKill(Player killer, Player victim, int mastery) {}

    public abstract String getPassiveDescription(int mastery);
    public abstract String getMove1Description();
    public abstract String getMove2Description();
    public abstract String getMove3Description();
    public abstract String getMove4Description();
    public abstract String getMove5Description();

    public String getMovesDescription(int mastery) {
        StringBuilder sb = new StringBuilder();
        sb.append("§ePassive§7: ").append(getPassiveDescription(mastery)).append("\n");
        sb.append(mastery >= 1 ? "§a✔ " : "§8✘ ").append("Move 1§7: ").append(getMove1Description()).append("\n");
        sb.append(mastery >= 4 ? "§a✔ " : "§8✘ ").append("Move 2§7: ").append(getMove2Description()).append("\n");
        sb.append(mastery >= 7 ? "§a✔ " : "§8✘ ").append("Move 3§7: ").append(getMove3Description()).append("\n");
        sb.append(mastery >= 10 ? "§a✔ " : "§8✘ ").append("Move 4§7: ").append(getMove4Description()).append("\n");
        sb.append(mastery >= 15 ? "§a✔ " : "§8✘ ").append("ULTIMATE§7: ").append(getMove5Description());
        return sb.toString();
    }

    public String getFullInfo(int mastery) {
        StringBuilder sb = new StringBuilder();
        sb.append(getFormattedName())
          .append(" §8[").append(rarity.getDisplayName()).append("]").append("\n");
        sb.append("§7").append(description).append("\n");
        sb.append("§eMastery: §f").append(mastery).append("/15\n");
        sb.append(getMovesDescription(mastery));
        return sb.toString();
    }
}
