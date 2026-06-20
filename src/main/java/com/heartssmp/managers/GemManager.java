package com.heartssmp.managers;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import com.heartssmp.gems.*;
import com.heartssmp.gems.common.*;
import com.heartssmp.gems.uncommon.*;
import com.heartssmp.gems.epic.*;
import com.heartssmp.gems.legendary.*;
import com.heartssmp.gems.mythical.*;
import com.heartssmp.gems.divine.*;
import org.bukkit.entity.Player;

import java.util.*;

public class GemManager {
    private final HeartsSMPPlugin plugin;
    private final Map<String, Gem> registry = new LinkedHashMap<>();
    private final Map<String, Integer> weights = new LinkedHashMap<>();

    public GemManager(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
        registerAll();
    }

    private void register(Gem gem, int weight) {
        registry.put(gem.getId(), gem);
        weights.put(gem.getId(), weight);
    }

    private void registerAll() {
        register(new EmberGem(), getWeight("gems.starter-weights.COMMON_EMBER", 25));
        register(new TideGem(),  getWeight("gems.starter-weights.COMMON_TIDE",  22));
        register(new StoneGem(), getWeight("gems.starter-weights.COMMON_STONE", 20));
        register(new GaleGem(),  getWeight("gems.starter-weights.UNCOMMON_GALE", 12));
        register(new ShadowGem(), getWeight("gems.starter-weights.EPIC_SHADOW", 8));
        register(new AuroraGem(), getWeight("gems.starter-weights.LEGENDARY_AURORA", 6));
        register(new VoidGem(),  getWeight("gems.starter-weights.MYTHICAL_VOID", 4));
        register(new CelestiaGem(), getWeight("gems.starter-weights.DIVINE_CELESTIA", 3));
    }

    private int getWeight(String path, int def) {
        return plugin.getConfig().getInt(path, def);
    }

    public Gem getGem(String id) {
        return registry.get(id);
    }

    public void assignStarterGem(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null || data.getGemId() != null) return;

        String rolled = rollGem();
        data.setGemId(rolled);
        data.setGemMastery(1);
        plugin.getDataManager().save(player.getUniqueId());

        Gem gem = registry.get(rolled);
        if (gem == null) return;

        player.sendMessage("§e✨ Welcome to HeartsSMP! You have received your starter gem:");
        player.sendMessage(gem.getRarity().getColor() + "✦ " + gem.getDisplayName()
                + " §8[" + gem.getRarity().getDisplayName() + "]");
        player.sendMessage("§7" + gem.getDescription());
        player.sendMessage(gem.getRarity().getColor() + "Skill 1: " + gem.getSkillDescription(1));
    }

    private String rollGem() {
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();
        int roll = new Random().nextInt(totalWeight);
        int cumulative = 0;
        for (Map.Entry<String, Integer> entry : weights.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) return entry.getKey();
        }
        return "COMMON_EMBER";
    }

    public void applyGemEffect(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null || data.getGemId() == null) return;
        Gem gem = registry.get(data.getGemId());
        if (gem != null) {
            gem.onPassiveTick(player, data.getGemMastery());
        }
    }

    public void runPassiveTick(Player player) {
        applyGemEffect(player);
    }

    public boolean upgradeGemMastery(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null || data.getGemId() == null) {
            player.sendMessage(plugin.prefix() + "§cYou don't have a gem!");
            return false;
        }
        Gem gem = registry.get(data.getGemId());
        if (gem == null) return false;

        int current = data.getGemMastery();
        if (current >= 3) {
            player.sendMessage(plugin.prefix() + "§cYour gem is already at max mastery (3)!");
            return false;
        }
        data.setGemMastery(current + 1);
        gem.onMasteryUp(player, data.getGemMastery());
        plugin.getDataManager().save(player.getUniqueId());
        return true;
    }

    public String getGemInfo(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null || data.getGemId() == null) return "§cNo gem assigned.";
        Gem gem = registry.get(data.getGemId());
        if (gem == null) return "§cUnknown gem.";
        return gem.getFullInfo(data.getGemMastery());
    }

    public Collection<Gem> getAllGems() {
        return registry.values();
    }
}
