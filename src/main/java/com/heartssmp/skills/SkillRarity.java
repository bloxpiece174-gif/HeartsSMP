package com.heartssmp.skills;

import net.kyori.adventure.text.format.TextColor;

public enum SkillRarity {
    COMMON("§f", "Common", TextColor.color(0xFFFFFF)),
    UNCOMMON("§a", "Uncommon", TextColor.color(0x55FF55)),
    EPIC("§5", "Epic", TextColor.color(0xAA00AA)),
    LEGENDARY("§6", "Legendary", TextColor.color(0xFFAA00)),
    MYTHICAL("§d", "Mythical", TextColor.color(0xFF55FF)),
    DIVINE("§e", "Divine Grace", TextColor.color(0xFFFF55));

    private final String colorCode;
    private final String displayName;
    private final TextColor textColor;

    SkillRarity(String colorCode, String displayName, TextColor textColor) {
        this.colorCode = colorCode;
        this.displayName = displayName;
        this.textColor = textColor;
    }

    public String getColorCode() { return colorCode; }
    public String getDisplayName() { return displayName; }
    public TextColor getTextColor() { return textColor; }
}
