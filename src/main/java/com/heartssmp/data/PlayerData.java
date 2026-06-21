package com.heartssmp.data;

import java.util.*;

public class PlayerData {
    private final UUID uuid;
    private String playerName;

    private int hearts;
    private int lives;
    private int totalKills;
    private int mobKills;
    private boolean eliminated;

    private String gemId;

    private final List<String> skills = new ArrayList<>();
    private final Map<String, Integer> skillMastery = new HashMap<>();

    private final Map<String, Integer> gemKills = new HashMap<>();
    private final Map<String, Integer> gemSubmissions = new HashMap<>();
    private int gemMastery;

    // Divine Trial
    private String divineTrialStage = "NOT_STARTED";
    private int divineTrialTaskIndex = 0;
    private int godSummonsUsed = 0; // how many times god has been summoned (max 3)
    private int godSummonsRemaining = 0;

    // Divine World
    private boolean inDivineWorld = false;

    public PlayerData(UUID uuid, String playerName, int startingHearts, int startingLives) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.hearts = startingHearts;
        this.lives = startingLives;
        this.totalKills = 0;
        this.mobKills = 0;
        this.eliminated = false;
        this.gemId = null;
        this.gemMastery = 0;
    }

    public UUID getUuid() { return uuid; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String name) { this.playerName = name; }

    public int getHearts() { return hearts; }
    public void setHearts(int hearts) { this.hearts = hearts; }

    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }

    public int getTotalKills() { return totalKills; }
    public void addKill() { this.totalKills++; }

    public int getMobKills() { return mobKills; }
    public void addMobKill() { this.mobKills++; }

    public boolean isEliminated() { return eliminated; }
    public void setEliminated(boolean eliminated) { this.eliminated = eliminated; }

    public String getGemId() { return gemId; }
    public void setGemId(String gemId) { this.gemId = gemId; }

    public int getGemMastery() { return gemMastery; }
    public void setGemMastery(int mastery) { this.gemMastery = mastery; }
    public boolean upgradeGemMastery() {
        if (gemMastery >= 3) return false;
        gemMastery++;
        return true;
    }

    public int getGemKills(String category) { return gemKills.getOrDefault(category, 0); }
    public void addGemKills(String category, int amount) {
        gemKills.merge(category, amount, Integer::sum);
    }

    public int getGemSubmissions(String category) { return gemSubmissions.getOrDefault(category, 0); }
    public void addGemSubmissions(String category, int amount) {
        gemSubmissions.merge(category, amount, Integer::sum);
    }

    public List<String> getSkills() { return Collections.unmodifiableList(skills); }
    public boolean hasSkill(String skillId) { return skills.contains(skillId); }

    public void addSkill(String skillId) {
        if (!skills.contains(skillId)) {
            skills.add(skillId);
            skillMastery.put(skillId, 1);
        }
    }

    public void removeSkill(String skillId) {
        skills.remove(skillId);
        skillMastery.remove(skillId);
    }

    public void removeLastSkill() {
        if (!skills.isEmpty()) {
            String last = skills.get(skills.size() - 1);
            removeSkill(last);
        }
    }

    public int getSkillMastery(String skillId) { return skillMastery.getOrDefault(skillId, 0); }

    public boolean upgradeSkillMastery(String skillId) {
        if (!skills.contains(skillId)) return false;
        int current = skillMastery.getOrDefault(skillId, 1);
        if (current >= 15) return false;
        skillMastery.put(skillId, current + 1);
        return true;
    }

    public void maxSkillMastery(String skillId) {
        if (skills.contains(skillId)) skillMastery.put(skillId, 15);
    }

    public void maxAllSkillMastery() {
        for (String s : skills) skillMastery.put(s, 15);
    }

    public int getTotalCombinedKills() { return totalKills + mobKills; }

    public int getNextSkillKillThreshold(int baseKills) {
        return (skills.size() + 1) * baseKills;
    }

    // Divine Trial
    public String getDivineTrialStage() { return divineTrialStage; }
    public void setDivineTrialStage(String stage) { this.divineTrialStage = stage; }

    public int getDivineTrialTaskIndex() { return divineTrialTaskIndex; }
    public void setDivineTrialTaskIndex(int index) { this.divineTrialTaskIndex = index; }

    public int getGodSummonsRemaining() { return godSummonsRemaining; }
    public void setGodSummonsRemaining(int n) { this.godSummonsRemaining = n; }
    public boolean useGodSummon() {
        if (godSummonsRemaining <= 0) return false;
        godSummonsRemaining--;
        godSummonsUsed++;
        return true;
    }
    public int getGodSummonsUsed() { return godSummonsUsed; }
    public void setGodSummonsUsed(int n) { this.godSummonsUsed = n; }

    // Divine World
    public boolean isInDivineWorld() { return inDivineWorld; }
    public void setInDivineWorld(boolean b) { this.inDivineWorld = b; }
}
