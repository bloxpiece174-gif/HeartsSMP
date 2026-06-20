package com.heartssmp.managers;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import com.heartssmp.skills.Skill;
import com.heartssmp.skills.SkillRarity;
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
    private final List<String> skillOrder = new ArrayList<>();

    public SkillManager(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
        registerAll();
    }

    private void register(Skill skill) {
        registry.put(skill.getId(), skill);
        skillOrder.add(skill.getId());
    }

    private void registerAll() {
        // Common (10)
        register(new InfernoFist());
        register(new IronSkin());
        register(new WindDash());
        register(new VenomBite());
        register(new StoneGuard());
        register(new FrostStep());
        register(new LightningReflexes());
        register(new ShadowCloak());
        register(new ThunderPunch());
        register(new NatureBloom());

        // Uncommon (8)
        register(new BloodRage());
        register(new SpectralShield());
        register(new AbyssalClaw());
        register(new CrystallineEdge());
        register(new HuntersMark());
        register(new MoltenCore());
        register(new PsychicWave());
        register(new TimeEcho());

        // Epic (6)
        register(new PhoenixRise());
        register(new VoidStep());
        register(new DragonscaleSkin());
        register(new StormCaller());
        register(new SoulReaper());
        register(new EarthShatter());

        // Legendary (4)
        register(new DivineSpeed());
        register(new MidnightSlaughter());
        register(new CelestialBarrage());
        register(new HellstormGate());

        // Mythical (2)
        register(new OmegaForce());
        register(new TimeWarp());

        // Divine (1) — granted only via special mission
        register(new GracefulEnlightenment());
    }

    public Skill getSkill(String id) {
        return registry.get(id);
    }

    public Collection<Skill> getAllSkills() {
        return registry.values();
    }

    public List<Skill> getSkillsByRarity(SkillRarity rarity) {
        List<Skill> result = new ArrayList<>();
        for (Skill s : registry.values()) {
            if (s.getRarity() == rarity) result.add(s);
        }
        return result;
    }

    public void checkSkillUnlock(Player player, PlayerData data) {
        int baseKills = plugin.getConfig().getInt("skills.kills-per-skill", 250);
        int totalCombined = data.getTotalCombinedKills();
        int currentSkillCount = data.getSkills().size();

        // How many skills they SHOULD have earned from kills (excluding divine)
        int normalSkillCount = skillOrder.size() - 1; // minus divine
        int shouldHave = Math.min(totalCombined / baseKills, normalSkillCount);

        if (shouldHave > currentSkillCount) {
            grantNextSkill(player, data);
        }
    }

    private void grantNextSkill(Player player, PlayerData data) {
        List<String> current = data.getSkills();

        for (String skillId : skillOrder) {
            Skill skill = registry.get(skillId);
            if (skill == null) continue;
            // Don't auto-grant divine
            if (skill.getRarity() == SkillRarity.DIVINE) continue;
            if (!current.contains(skillId)) {
                data.addSkill(skillId);
                skill.onUnlock(player);
                plugin.getDataManager().save(player.getUniqueId());

                String rarity = skill.getRarity().getColorCode() + "[" + skill.getRarity().getDisplayName() + "]";
                player.sendMessage("§6§l⚔ SKILL UNLOCKED! " + rarity + " §r" + skill.getFormattedName());
                player.sendMessage("§7" + skill.getDescription());
                player.sendMessage("§7Total skills: §e" + data.getSkills().size() + " §7| Mastery starts at 1");

                plugin.getServer().broadcastMessage("§6[HeartsSMP] ⚔ " + player.getName() + " unlocked " + skill.getFormattedName() + " " + rarity);
                plugin.getDivineTrialManager().checkMythicalCompletion(player, data);
                return;
            }
        }
    }

    public boolean upgradeMastery(Player player, String skillId) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return false;
        if (!data.hasSkill(skillId)) {
            player.sendMessage(plugin.prefix() + "§cYou don't have the skill: §e" + skillId);
            return false;
        }

        int currentMastery = data.getSkillMastery(skillId);
        if (currentMastery >= 15) {
            player.sendMessage(plugin.prefix() + "§cSkill is already at max mastery §e(15)§c!");
            return false;
        }

        boolean upgraded = data.upgradeSkillMastery(skillId);
        if (upgraded) {
            int newMastery = data.getSkillMastery(skillId);
            Skill skill = registry.get(skillId);
            if (skill != null) {
                skill.onMasteryUp(player, newMastery);
            }
            player.sendMessage(plugin.prefix() + "§aSkill §b" + skillId + " §amastery upgraded to §e" + newMastery + "/15§a!");
            plugin.getDataManager().save(player.getUniqueId());
        }
        return upgraded;
    }

    public void onPlayerDeath(Player player, PlayerData data) {
        if (data.getSkills().isEmpty()) return;
        String lost = data.getSkills().get(data.getSkills().size() - 1);
        Skill skill = registry.get(lost);
        data.removeLastSkill();

        if (skill != null) {
            player.sendMessage(plugin.prefix() + "§c💀 You lost skill: " + skill.getFormattedName() + " §7on death!");
            plugin.getServer().broadcastMessage("§8[HeartsSMP] " + player.getName() + " lost §r" + skill.getFormattedName() + " §8on death.");
        }
    }

    public void runPassiveTick(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        for (String skillId : data.getSkills()) {
            Skill skill = registry.get(skillId);
            if (skill != null) {
                skill.onPassiveTick(player, data.getSkillMastery(skillId));
            }
        }
    }

    public void grantDivineSkill(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        String divineId = "graceful_enlightenment";
        if (data.hasSkill(divineId)) {
            player.sendMessage(plugin.prefix() + "§cYou already have Divine Grace!");
            return;
        }
        data.addSkill(divineId);
        Skill skill = registry.get(divineId);
        if (skill != null) skill.onUnlock(player);
        plugin.getDataManager().save(player.getUniqueId());
    }
}
