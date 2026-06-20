package com.heartssmp.gems;

public enum GemRarity {
    COMMON("§7", "Common"),
    UNCOMMON("§a", "Uncommon"),
    EPIC("§5", "Epic"),
    LEGENDARY("§6", "Legendary"),
    MYTHICAL("§d", "Mythical"),
    DIVINE("§e", "Divine Grace");

    private final String color;
    private final String displayName;

    GemRarity(String color, String displayName) {
        this.color = color;
        this.displayName = displayName;
    }

    public String getColor() { return color; }
    public String getDisplayName() { return displayName; }
}
