package com.heartssmp.managers;

import com.heartssmp.HeartsSMPPlugin;
import com.heartssmp.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * 30 Custom Items players can unlock throughout the SMP.
 * Unlock via: kills, gem mastery, skill mastery, or admin grant.
 */
public class ItemManager {
    private final HeartsSMPPlugin plugin;

    // All 30 items defined here as static data
    private static final List<CustomItem> ITEMS = new ArrayList<>();

    static {
        // Tier 1 — Starter (1-10)
        ITEMS.add(new CustomItem("ember_shard",    "Ember Shard",        "§cA warm shard dealing extra fire damage",           Material.BLAZE_ROD,       1));
        ITEMS.add(new CustomItem("tide_pearl",     "Tide Pearl",         "§9A pearl of the ocean — throw to summon a water rush",Material.ENDER_PEARL,     1));
        ITEMS.add(new CustomItem("stone_core",     "Stone Core",         "§7A core of earth — passive Resistance I",           Material.COBBLESTONE,     1));
        ITEMS.add(new CustomItem("wind_feather",   "Wind Feather",       "§bA feather of wind — passive Speed II",             Material.FEATHER,         1));
        ITEMS.add(new CustomItem("shadow_mask",    "Shadow Mask",        "§8A mask of darkness — sneak for invisibility",      Material.CARVED_PUMPKIN,  1));
        ITEMS.add(new CustomItem("aurora_crown",   "Aurora Crown",       "§eA crown of light — passive Regen I",               Material.GOLDEN_HELMET,   1));
        ITEMS.add(new CustomItem("void_lens",      "Void Lens",          "§5A lens of the void — see players through walls",   Material.GLASS,           1));
        ITEMS.add(new CustomItem("celestia_dust",  "Celestia Dust",      "§eDivine dust — temporarily boosts all stats",       Material.GLOWSTONE_DUST,  1));
        ITEMS.add(new CustomItem("life_crystal",   "Life Crystal",       "§aA crystal of life — use to gain +1 life",          Material.PRISMARINE_CRYSTALS, 1));
        ITEMS.add(new CustomItem("heart_shard",    "Heart Shard",        "§cA shard of heart — use to gain +1 heart",          Material.RED_DYE,         1));

        // Tier 2 — Combat (11-20)
        ITEMS.add(new CustomItem("soul_blade",     "Soul Blade",         "§8A blade that harvests souls on kill",              Material.IRON_SWORD,      2));
        ITEMS.add(new CustomItem("flame_bow",      "Flame Bow",          "§cA bow that fires flaming arrows",                  Material.BOW,             2));
        ITEMS.add(new CustomItem("frost_shield",   "Frost Shield",       "§bA shield that slows attackers on block",           Material.SHIELD,          2));
        ITEMS.add(new CustomItem("venom_arrow",    "Venom Arrow",        "§2Arrows tipped with lethal poison",                 Material.TIPPED_ARROW,    2));
        ITEMS.add(new CustomItem("thunder_axe",    "Thunder Axe",        "§eAxe that strikes lightning on kill",               Material.IRON_AXE,        2));
        ITEMS.add(new CustomItem("dragon_scale",   "Dragon Scale",       "§6Scale armor piece — massive defense",              Material.DIAMOND_CHESTPLATE, 2));
        ITEMS.add(new CustomItem("void_dagger",    "Void Dagger",        "§5Dagger that ignores 50% armor",                    Material.STONE_SWORD,     2));
        ITEMS.add(new CustomItem("storm_staff",    "Storm Staff",        "§9Staff of storms — rightclick to summon lightning", Material.STICK,           2));
        ITEMS.add(new CustomItem("blood_gem_ring", "Blood Gem Ring",     "§4Ring — killing restores 1 heart",                  Material.RED_DYE,         2));
        ITEMS.add(new CustomItem("phase_boots",    "Phase Boots",        "§dBoots that let you walk through 1-block gaps",     Material.IRON_BOOTS,      2));

        // Tier 3 — Legendary Gear (21-30)
        ITEMS.add(new CustomItem("omega_gauntlet",  "Omega Gauntlet",    "§dThe gauntlet of omega — greatly boosts punch damage", Material.LEATHER_CHESTPLATE, 3));
        ITEMS.add(new CustomItem("chrono_watch",    "Chrono Watch",      "§6A watch of time — use to freeze nearby enemies 3s",  Material.CLOCK,           3));
        ITEMS.add(new CustomItem("celestial_blade", "Celestial Blade",   "§eA blade of celestial light — divine power",          Material.DIAMOND_SWORD,   3));
        ITEMS.add(new CustomItem("void_cloak",      "Void Cloak",        "§5A cloak of the void — 5s invincibility once per 2min", Material.LEATHER_CHESTPLATE, 3));
        ITEMS.add(new CustomItem("titan_hammer",    "Titan Hammer",      "§8A hammer of titans — ground slam on rightclick",    Material.IRON_SWORD,      3));
        ITEMS.add(new CustomItem("aurora_staff",    "Aurora Staff",      "§eAurora staff — heal nearby allies on rightclick",   Material.BLAZE_ROD,       3));
        ITEMS.add(new CustomItem("shadow_cloak",    "Shadow Cloak",      "§8Cloak of shadows — become invisible on sneak",     Material.LEATHER_CHESTPLATE, 3));
        ITEMS.add(new CustomItem("hell_core",       "Hellcore Fragment",  "§4Fragment of hell — burst fire on rightclick",       Material.NETHER_BRICK,    3));
        ITEMS.add(new CustomItem("star_fragment",   "Star Fragment",      "§eFallen star — absorb to get 3 random skill uses",   Material.NETHER_STAR,     3));
        ITEMS.add(new CustomItem("divine_scroll",   "Divine Scroll",      "§6Scroll of divinity — hint toward Divine Grace mission", Material.PAPER,        3));

        // Divine Trial — special quest item (placed as a LANTERN block at the altar corners)
        ITEMS.add(new CustomItem("golden_torch",    "Golden Torch",       "§6A torch blessed by the divine — marks sacred ground", Material.LANTERN,      3));
    }

    public ItemManager(HeartsSMPPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createItem(String id) {
        CustomItem ci = ITEMS.stream().filter(i -> i.id.equals(id)).findFirst().orElse(null);
        if (ci == null) return null;

        ItemStack stack = new ItemStack(ci.material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(ci.name, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
            Component.text(ci.description, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("Tier " + ci.tier, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false),
            Component.text("HeartsSMP Custom Item", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        if (ci.tier == 3) {
            meta.addEnchant(Enchantment.MENDING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    public void giveItem(Player player, String id) {
        ItemStack item = createItem(id);
        if (item == null) {
            player.sendMessage(plugin.prefix() + "§cUnknown item: " + id);
            return;
        }
        player.getInventory().addItem(item);
        CustomItem ci = ITEMS.stream().filter(i -> i.id.equals(id)).findFirst().orElse(null);
        player.sendMessage(plugin.prefix() + "§aYou received: §e" + (ci != null ? ci.name : id));
    }

    public List<CustomItem> getAllItems() {
        return Collections.unmodifiableList(ITEMS);
    }

    public static class CustomItem {
        public final String id;
        public final String name;
        public final String description;
        public final Material material;
        public final int tier;

        public CustomItem(String id, String name, String description, Material material, int tier) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.material = material;
            this.tier = tier;
        }
    }
}
