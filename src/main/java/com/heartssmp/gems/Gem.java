package com.heartssmp.gems;

import org.bukkit.entity.Player;

public abstract class Gem {
    protected final String id;
    protected final String displayName;
    protected final GemRarity rarity;
    protected final String description;
    protected final int maxMastery = 3;

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

    // Passive tick applied while equipped
    public void onPassiveTick(Player player, int mastery) {}

    // Called when the gem's kill requirement is met
    public void onKillMilestone(Player player, int milestone, int mastery) {}

    // Called on mastery upgrade
    public void onMasteryUp(Player player, int newMastery) {
        player.sendMessage("§a[Gem] Your §b" + displayName + " §agem reached mastery §e" + newMastery + "/3§a!");
        if (newMastery <= 3) {
            player.sendMessage("§7New gem skill unlocked: §b" + getSkillDescription(newMastery));
        }
    }

    public String getSkillDescription(int masteryLevel) {
        return switch (masteryLevel) {
            case 1 -> "Skill 1 (see /gem for details)";
            case 2 -> "Skill 2 (see /gem for details)";
            case 3 -> "Skill 3 — Ultimate (see /gem for details)";
            default -> "None";
        };
    }

    // Kill thresholds to reach next gem mastery
    public int getKillsForMastery(int mastery) {
        return switch (mastery) {
            case 1 -> 50;
            case 2 -> 150;
            case 3 -> 400;
            default -> Integer.MAX_VALUE;
        };
    }

    public String getFormattedName() {
        return rarity.getColor() + displayName;
    }

    public String getFullInfo(int mastery) {
        return "§7Gem: " + getFormattedName() + "\n"
                + "§7Rarity: " + rarity.getColor() + rarity.getDisplayName() + "\n"
                + "§7Description: §f" + description + "\n"
                + "§7Mastery: §e" + mastery + "/3\n"
                + "§7Skill 1: " + getSkillDescription(1) + "\n"
                + "§7Skill 2 (Mastery 2): " + (mastery >= 2 ? getSkillDescription(2) : "§8Locked") + "\n"
                + "§7Skill 3 (Mastery 3): " + (mastery >= 3 ? getSkillDescription(3) : "§8Locked");
    }
}
