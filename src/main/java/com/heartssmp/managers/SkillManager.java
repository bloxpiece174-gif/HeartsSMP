package com.heartssmp.managers;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import com.heartssmp.skills.*;
import com.heartssmp.skills.common.*;
import com.heartssmp.skills.uncommon.*;
import com.heartssmp.skills.epic.*;
import com.heartssmp.skills.legendary.*;
import com.heartssmp.skills.mythical.*;
import com.heartssmp.skills.divine.*;
import org.bukkit.entity.Player;

import java.util.*;

public class SkillManager {
    private final HeartsSMPPlugin plugin;
    private final Map<String, Skill> registry = new LinkedHashMap<>();

    public SkillManager(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
        registerAll();
    }

    private void register(Skill skill) { registry.put(skill.getId(), skill); }

    private void registerAll() {
        // Common
        register(new FrostStep());
        register(new InfernoFist());
        register(new IronSkin());
        register(new LightningReflexes());
        register(new NatureBloom());
        register(new ShadowCloak());
        register(new StoneGuard());
        register(new ThunderPunch());
        register(new VenomBite());
        register(new WindDash());
        // Uncommon
        register(new AbyssalClaw());
        register(new BloodRage());
        register(new CrystallineEdge());
        register(new HuntersMark());
        register(new MoltenCore());
        register(new PsychicWave());
        register(new SpectralShield());
        register(new TimeEcho());
        // Epic
        register(new DragonscaleSkin());
        register(new EarthShatter());
        register(new PhoenixRise());
        register(new SoulReaper());
        register(new StormCaller());
        register(new VoidStep());
        // Legendary
        register(new CelestialBarrage());
        register(new DivineSpeed());
        register(new HellstormGate());
        register(new MidnightSlaughter());
        // Mythical
        register(new OmegaForce());
        register(new TimeWarp());
        // Divine
        register(new GracefulEnlightenment());
    }

    public Skill getSkill(String id) { return registry.get(id); }
    public Collection<Skill> getAllSkills() { return registry.values(); }

    public void runPassiveTick(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        for (String skillId : data.getSkills()) {
            Skill skill = registry.get(skillId);
            if (skill != null) skill.onPassiveTick(player, data.getSkillMastery(skillId));
        }
    }

    public void onPlayerKill(Player killer, Player victim) {
        PlayerData data = plugin.getDataManager().get(killer.getUniqueId());
        if (data == null) return;
        for (String skillId : data.getSkills()) {
            Skill skill = registry.get(skillId);
            if (skill != null) skill.onPlayerKill(killer, victim, data.getSkillMastery(skillId));
        }
    }

    public void onKillMob(Player killer) {
        PlayerData data = plugin.getDataManager().get(killer.getUniqueId());
        if (data == null) return;
        data.addMobKill();
        // Check if player earns a new skill
        checkSkillUnlock(killer, data);
        plugin.getDataManager().save(killer.getUniqueId());
    }

    public void onKillPlayer(Player killer) {
        PlayerData data = plugin.getDataManager().get(killer.getUniqueId());
        if (data == null) return;
        data.addKill();
        checkSkillUnlock(killer, data);
        plugin.getDataManager().save(killer.getUniqueId());
    }

    private void checkSkillUnlock(Player player, PlayerData data) {
        int baseKills = plugin.getConfig().getInt("skills.kills-per-unlock", 10);
        int totalKills = data.getTotalCombinedKills();
        int threshold = data.getNextSkillKillThreshold(baseKills);
        if (totalKills >= threshold && data.getSkills().size() < getMaxSkillsByKills(totalKills, baseKills)) {
            grantRandomSkill(player, data);
        }
    }

    private int getMaxSkillsByKills(int totalKills, int baseKills) {
        return totalKills / baseKills;
    }

    private void announceUnlock(Player player, Skill skill) {
        player.sendMessage(plugin.prefix() + "§6§l⚔ New Skill Unlocked: " + skill.getFormattedName());
        for (int i = 1; i <= 5; i++) {
            skill.onMoveUnlock(player, i);
        }
    }

    private void grantRandomSkill(Player player, PlayerData data) {
        List<String> available = new ArrayList<>();
        for (String id : registry.keySet()) {
            if (!data.hasSkill(id) && !id.equals("graceful_enlightenment")) {
                available.add(id);
            }
        }
        if (available.isEmpty()) return;
        String picked = available.get(new Random().nextInt(available.size()));
        data.addSkill(picked);
        Skill skill = registry.get(picked);
        if (skill != null) announceUnlock(player, skill);
        plugin.getDivineTrialManager().checkMythicalCompletion(player, data);
        plugin.getDataManager().save(player.getUniqueId());
    }

    public void grantDivineSkill(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        if (data.hasSkill("graceful_enlightenment")) {
            player.sendMessage(plugin.prefix() + "§cYou already have Graceful Enlightenment!");
            return;
        }
        data.addSkill("graceful_enlightenment");
        Skill skill = registry.get("graceful_enlightenment");
        if (skill != null) announceUnlock(player, skill);
        data.setGodSummonsRemaining(3);
        plugin.getDataManager().save(player.getUniqueId());
    }

    public boolean upgradeMastery(Player player, String skillId) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return false;
        boolean upgraded = data.upgradeSkillMastery(skillId);
        if (upgraded) {
            plugin.getDataManager().save(player.getUniqueId());
            player.sendMessage(plugin.prefix() + "§aMastery upgraded! §7New mastery: §e"
                    + data.getSkillMastery(skillId) + "/15");
        }
        return upgraded;
    }
}
