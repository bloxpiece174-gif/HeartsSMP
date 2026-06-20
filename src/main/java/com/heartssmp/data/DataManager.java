package com.heartssmp.data;

import com.heartssmp.HeartsSMPPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataManager {
    private final HeartsSMPPlugin plugin;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private File dataFolder;

    public DataManager(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) dataFolder.mkdirs();
    }

    public PlayerData getOrCreate(UUID uuid, String name) {
        if (!playerDataMap.containsKey(uuid)) {
            PlayerData loaded = load(uuid, name);
            playerDataMap.put(uuid, loaded);
        }
        return playerDataMap.get(uuid);
    }

    public PlayerData get(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public void save(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);
        if (data == null) return;

        File file = new File(dataFolder, uuid.toString() + ".yml");
        YamlConfiguration cfg = new YamlConfiguration();

        cfg.set("name", data.getPlayerName());
        cfg.set("hearts", data.getHearts());
        cfg.set("lives", data.getLives());
        cfg.set("totalKills", data.getTotalKills());
        cfg.set("mobKills", data.getMobKills());
        cfg.set("eliminated", data.isEliminated());
        cfg.set("gemId", data.getGemId());
        cfg.set("gemMastery", data.getGemMastery());
        cfg.set("skills", new ArrayList<>(data.getSkills()));
        cfg.set("divineTrialStage", data.getDivineTrialStage());
        cfg.set("divineTrialTaskIndex", data.getDivineTrialTaskIndex());
        cfg.set("godSummonsRemaining", data.getGodSummonsRemaining());

        Map<String, Integer> mastery = new HashMap<>();
        for (String skill : data.getSkills()) {
            mastery.put(skill, data.getSkillMastery(skill));
        }
        cfg.set("skillMastery", mastery);

        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save data for " + uuid);
        }
    }

    private PlayerData load(UUID uuid, String name) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        int startHearts = plugin.getConfig().getInt("hearts.starting", 10);
        int startLives = plugin.getConfig().getInt("lives.starting", 5);

        if (!file.exists()) {
            return new PlayerData(uuid, name, startHearts, startLives);
        }

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        PlayerData data = new PlayerData(uuid,
                cfg.getString("name", name),
                cfg.getInt("hearts", startHearts),
                cfg.getInt("lives", startLives));

        data.setEliminated(cfg.getBoolean("eliminated", false));
        data.setGemId(cfg.getString("gemId", null));
        data.setGemMastery(cfg.getInt("gemMastery", 0));
        data.setDivineTrialStage(cfg.getString("divineTrialStage", "NOT_STARTED"));
        data.setDivineTrialTaskIndex(cfg.getInt("divineTrialTaskIndex", 0));
        data.setGodSummonsRemaining(cfg.getInt("godSummonsRemaining", 0));

        List<String> skills = cfg.getStringList("skills");
        for (String skill : skills) {
            data.addSkill(skill);
        }

        if (cfg.isConfigurationSection("skillMastery")) {
            for (String skill : skills) {
                int m = cfg.getInt("skillMastery." + skill, 1);
                for (int i = 1; i < m; i++) {
                    data.upgradeSkillMastery(skill);
                }
            }
        }

        // Restore kill counts via reflection workaround — use field names directly
        int tKills = cfg.getInt("totalKills", 0);
        int mKills = cfg.getInt("mobKills", 0);
        for (int i = 0; i < tKills; i++) data.addKill();
        for (int i = 0; i < mKills; i++) data.addMobKill();

        return data;
    }

    public void saveAll() {
        for (UUID uuid : playerDataMap.keySet()) {
            save(uuid);
        }
    }

    public void unload(UUID uuid) {
        save(uuid);
        playerDataMap.remove(uuid);
    }

    public Collection<PlayerData> getAllLoaded() {
        return playerDataMap.values();
    }
}
