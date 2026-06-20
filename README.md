# HeartsSMP Plugin

A comprehensive Minecraft (Paper 1.21.1) SMP plugin with Hearts, Lives, Skills, and Gems systems.

## Requirements
- **Paper 1.21.1** (or any Spigot/Paper 1.21.x)
- **Java 17+**
- **Maven** (to build)

## Building
```bash
cd minecraft-plugin
mvn clean package
```
The output jar will be at `target/HeartsSMP-1.0.0.jar` — drop it in your server's `plugins/` folder.

---

## Systems Overview

### ❤ Hearts System
| Rule | Value |
|------|-------|
| Starting hearts | 10 |
| Max hearts | 30 |
| Kill a player → | +1 heart |
| Max health | hearts × 2 HP |

### 💀 Lives System
| Rule | Value |
|------|-------|
| Starting lives | 5 |
| Max lives | 10 |
| Die → | -1 life |
| Sacrifice 2 hearts → | +1 life |
| Min hearts to sacrifice | 5 hearts |

### 🚫 Elimination
- Lives reach 0 → **2-week ban** + server announcement
- Admins can unban with `/adminunban <player>`

---

## ⚔ Skills System (31 Total)

Every **250 kills** (player + mob kills combined) unlocks a new skill, in order of rarity.

### Rarity Tiers
| Rarity | Count | Color |
|--------|-------|-------|
| Common | 10 | White |
| Uncommon | 8 | Green |
| Epic | 6 | Purple |
| Legendary | 4 | Gold |
| Mythical | 2 | Pink |
| **Divine Grace** | **1** | **Yellow** |

### All 31 Skills
**Common:** Inferno Fist, Iron Skin, Wind Dash, Venom Bite, Stone Guard, Frost Step, Lightning Reflexes, Shadow Cloak, Thunder Punch, Nature Bloom

**Uncommon:** Blood Rage, Spectral Shield, Abyssal Claw, Crystalline Edge, Hunter's Mark, Molten Core, Psychic Wave, Time Echo

**Epic:** Phoenix Rise, Void Step, Dragonscale Skin, Storm Caller, Soul Reaper, Earth Shatter

**Legendary:** Divine Speed, Midnight Slaughter, Celestial Barrage, Hellstorm Gate

**Mythical:** Omega Force, Time Warp

**Divine (1):** ✨ Graceful Enlightenment — granted only via special mission (use `/adminskill <player> give graceful_enlightenment` after the player completes it)

### Mastery System
- Each skill has **15 mastery levels**
- Every **3 mastery** unlocks a new move (5 moves total per skill)
- Each skill also has a **passive buff** that scales with mastery
- **On death:** lose the **last skill** you earned

---

## 💎 Gems SMP (8 Gems)

On first join, players receive a **random starter gem** based on weighted luck:

| Gem | Rarity | Base Chance | Passive |
|-----|--------|-------------|---------|
| Ember Gem | Common | ~25% | Fire resistance |
| Tide Gem | Common | ~22% | Water breathing + speed |
| Stone Gem | Common | ~20% | Resistance |
| Gale Gem | Uncommon | ~12% | Speed + jump boost |
| Shadow Gem | Epic | ~8% | Stealth while sneaking |
| Aurora Gem | Legendary | ~6% | Regen + absorption |
| Void Gem | Mythical | ~4% | Portal particles |
| Celestia Gem | Divine | ~3% | All positive effects |

Each gem has **3 mastery levels** with **3 unique skills** unlocked progressively.

---

## 🎒 30 Custom Items
Items are grouped in 3 tiers:
- **Tier 1** (10 items): Ember Shard, Tide Pearl, Stone Core, Wind Feather, Shadow Mask, Aurora Crown, Void Lens, Celestia Dust, Life Crystal, Heart Shard
- **Tier 2** (10 items): Soul Blade, Flame Bow, Frost Shield, Venom Arrow, Thunder Axe, Dragon Scale, Void Dagger, Storm Staff, Blood Gem Ring, Phase Boots
- **Tier 3** (10 items): Omega Gauntlet, Chrono Watch, Celestial Blade, Void Cloak, Titan Hammer, Aurora Staff, Shadow Cloak, Hellcore Fragment, Star Fragment, Divine Scroll

Give items with: `/adminskill` or add custom give logic via `ItemManager#giveItem(player, id)`.

---

## Commands

| Command | Description |
|---------|-------------|
| `/sacrifice` | Sacrifice 2 hearts for +1 life (min 5 hearts) |
| `/stats [player]` | View hearts, lives, kills, gem, skills |
| `/skills` | List your skills and mastery |
| `/skillinfo <id>` | View skill details and move list |
| `/mastery <skill_id>` | Upgrade a skill's mastery |
| `/gem` | View your gem info and skills |

### Admin Commands (OP only)
| Command | Description |
|---------|-------------|
| `/adminhearts <player> <set\|add\|remove> <n>` | Modify hearts |
| `/adminlives <player> <set\|add\|remove> <n>` | Modify lives |
| `/admingem <player> <gem_id>` | Set a player's gem |
| `/adminskill <player> <give\|remove> <skill_id>` | Grant/remove skills |
| `/adminitem <player> <item_id> [amount]` | Give a custom item (e.g. `golden_torch`) |
| `/adminunban <player>` | Unban an eliminated player |
| `/godsummon [x y z]` | Summon God at a location — only usable by the first Divine Trial completer, max mastery, 3 lifetime charges |

---

## Config (config.yml)

```yaml
hearts:
  starting: 10
  maximum: 30
  kill-reward: 1

lives:
  starting: 5
  maximum: 10
  sacrifice-heart-cost: 2
  sacrifice-min-hearts: 5

elimination:
  ban-duration-weeks: 2

skills:
  kills-per-skill: 250

gems:
  starter-weights:
    COMMON_EMBER: 25
    COMMON_TIDE: 22
    COMMON_STONE: 20
    UNCOMMON_GALE: 12
    EPIC_SHADOW: 8
    LEGENDARY_AURORA: 6
    MYTHICAL_VOID: 4
    DIVINE_CELESTIA: 3
```

## ✨ Divine Trial Quest (Divine Grace Mission)
The Divine Grace skill (`graceful_enlightenment`) is earned through a full in-game quest — fully implemented, no admin manual-grant needed (though `/adminskill <player> give graceful_enlightenment` still works as a backup/testing shortcut).

**The flow:**
1. **Unlock both Mythical skills** (`omega_force` + `time_warp`, from kills or `/adminskill give`). You'll automatically receive a **Map to the Divine Trial Chamber** pointing to fixed coordinates set in `config.yml` (`divine-trial.altar-x/y/z`).
2. **Build the altar** at those coordinates: a flat **6x6 platform of Diamond Blocks**, with a **Golden Torch** (custom item, give via `/adminitem <player> golden_torch`, placed as a Lantern block) on each of the **4 corners**, one block above the platform.
3. **Place a sign** anywhere near the altar reading exactly: `I Want To Participate In Divine Trial`.
4. The sky turns to a **thunderstorm**, lightning strikes you, your screen flashes white — then you're teleported to the **Divine Palace**, a procedurally-built floating arena far from spawn (`divine-trial.palace-x/y/z`).
5. Complete **3 tasks in order**:
   - **Combat Gauntlet** — survive 3 escalating waves of Divine Guardians (Vexes).
   - **The Ascent** — parkour up a floating causeway (falling resets you to the start).
   - **The Four Braziers** — light all 4 unlit campfires around the summit sigil with flint & steel.
6. **God speaks to you** — a short scripted dialogue plays, then you're granted **Graceful Enlightenment** and teleported back near the altar.
7. **First-ever completer bonus:** the very first player on the server to finish the trial also receives **3 lifetime "Summon God" charges** (`/godsummon [x y z]`), usable only once they reach **mastery 15** on Graceful Enlightenment.

Configure altar/palace locations and summon charge count under `divine-trial:` in `config.yml`.

## Data Storage
Player data is stored in `plugins/HeartsSMP/playerdata/<uuid>.yml` — no database needed.
Auto-saves every 5 minutes and on player quit.
