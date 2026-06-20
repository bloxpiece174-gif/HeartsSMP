package com.heartssmp.skills;

import org.bukkit.entity.Player;

public abstract class Skill {
    protected final String id;
    protected final String displayName;
    protected final SkillRarity rarity;
    protected final String description;

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

    // Called when skill is unlocked
    public void onUnlock(Player player) {}

    // Called passively (every few ticks) while player is online
    public void onPassiveTick(Player player, int mastery) {}

    // Called when player activates the skill manually (if it's active)
    public void onActivate(Player player, int mastery) {}

    // Called when player kills another player while holding this skill
    public void onPlayerKill(Player killer, Player victim, int mastery) {}

    // Called when mastery increases
    public void onMasteryUp(Player player, int newMastery) {
        int moveUnlock = newMastery / 3;
        if (newMastery % 3 == 0 && moveUnlock >= 1) {
            player.sendMessage("§6[Mastery] §eNew move unlocked for §b" + displayName + "§e at mastery §a" + newMastery + "§e!");
            onMoveUnlock(player, moveUnlock);
        }
    }

    // Override to handle move unlocks at mastery 3, 6, 9, 12, 15
    public void onMoveUnlock(Player player, int moveIndex) {}

    public String getFormattedName() {
        return rarity.getColorCode() + displayName;
    }

    public String getMovesDescription(int mastery) {
        StringBuilder sb = new StringBuilder();
        sb.append("§7Passive: ").append(getPassiveDescription(mastery)).append("\n");
        if (mastery >= 3) sb.append("§aMove 1: ").append(getMove1Description()).append("\n");
        if (mastery >= 6) sb.append("§bMove 2: ").append(getMove2Description()).append("\n");
        if (mastery >= 9) sb.append("§dMove 3: ").append(getMove3Description()).append("\n");
        if (mastery >= 12) sb.append("§6Move 4: ").append(getMove4Description()).append("\n");
        if (mastery >= 15) sb.append("§cMove 5 (ULTIMATE): ").append(getMove5Description()).append("\n");
        return sb.toString();
    }

    public String getPassiveDescription(int mastery) { return "No passive yet"; }
    public String getMove1Description() { return "Unlocked at mastery 3"; }
    public String getMove2Description() { return "Unlocked at mastery 6"; }
    public String getMove3Description() { return "Unlocked at mastery 9"; }
    public String getMove4Description() { return "Unlocked at mastery 12"; }
    public String getMove5Description() { return "Unlocked at mastery 15"; }
}
