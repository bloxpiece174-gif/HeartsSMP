package com.heartssmp;

import com.heartssmp.commands.*;
import com.heartssmp.data.DataManager;
import com.heartssmp.listeners.*;
import com.heartssmp.managers.*;
import com.heartssmp.quest.DivineTrialManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HeartsSMPPlugin extends JavaPlugin {

    private DataManager dataManager;
    private HeartManager heartManager;
    private LivesManager livesManager;
    private SkillManager skillManager;
    private GemManager gemManager;
    private ItemManager itemManager;
    private DivineTrialManager divineTrialManager;

    private int passiveTaskId = -1;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Managers
        dataManager = new DataManager(this);
        heartManager = new HeartManager(this);
        livesManager = new LivesManager(this);
        skillManager = new SkillManager(this);
        gemManager = new GemManager(this);
        itemManager = new ItemManager(this);
        divineTrialManager = new DivineTrialManager(this);

        // Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new DivineTrialListener(this), this);

        // Commands
        getCommand("sacrifice").setExecutor(new SacrificeCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("skills").setExecutor(new SkillsCommand(this));
        getCommand("gem").setExecutor(new GemCommand(this));
        getCommand("mastery").setExecutor(new MasteryCommand(this));
        getCommand("skillinfo").setExecutor(new SkillInfoCommand(this));
        getCommand("adminhearts").setExecutor(new AdminCommand(this, "hearts"));
        getCommand("adminlives").setExecutor(new AdminCommand(this, "lives"));
        getCommand("admingem").setExecutor(new AdminCommand(this, "gem"));
        getCommand("adminskill").setExecutor(new AdminCommand(this, "skill"));
        getCommand("adminitem").setExecutor(new AdminCommand(this, "item"));
        getCommand("adminunban").setExecutor(new AdminCommand(this, "unban"));
        getCommand("godsummon").setExecutor(new GodSummonCommand(this));

        // Passive tick every 3 seconds (60 ticks)
        passiveTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                skillManager.runPassiveTick(player);
                gemManager.runPassiveTick(player);
            }
        }, 60L, 60L);

        // Auto-save every 5 minutes
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            dataManager.saveAll();
        }, 6000L, 6000L);

        getLogger().info("HeartsSMP Plugin enabled! " + skillManager.getAllSkills().size() + " skills, "
                + gemManager.getAllGems().size() + " gems, " + itemManager.getAllItems().size() + " items loaded.");
    }

    @Override
    public void onDisable() {
        if (passiveTaskId != -1) getServer().getScheduler().cancelTask(passiveTaskId);
        dataManager.saveAll();
        getLogger().info("HeartsSMP Plugin disabled. All data saved.");
    }

    public String prefix() {
        return getConfig().getString("messages.prefix", "§8[§cHeartsSMP§8] §r");
    }

    public DataManager getDataManager() { return dataManager; }
    public HeartManager getHeartManager() { return heartManager; }
    public LivesManager getLivesManager() { return livesManager; }
    public SkillManager getSkillManager() { return skillManager; }
    public GemManager getGemManager() { return gemManager; }
    public ItemManager getItemManager() { return itemManager; }
    public DivineTrialManager getDivineTrialManager() { return divineTrialManager; }
}
