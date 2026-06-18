package kami.gg.souppvp.kit.cosmetic;

import lombok.Getter;
import org.bukkit.Color;

@Getter
public class CosmeticSkin {
    private final String id;
    private final String displayName;
    private final String textureHash;

    // Armour colours — nullable, falls back to defaults if not set
    private final Color chestplateColor;
    private final Color leggingsColor;
    private final Color bootsColor;

    // Original constructor — no colours (uses null, getArmor will fall back)
    public CosmeticSkin(String id, String displayName, String textureHash) {
        this(id, displayName, textureHash, null, null, null);
    }

    // Full constructor with outfit colours
    public CosmeticSkin(String id, String displayName, String textureHash,
                        Color chestplateColor, Color leggingsColor, Color bootsColor) {
        this.id = id;
        this.displayName = displayName;
        this.textureHash = textureHash;
        this.chestplateColor = chestplateColor;
        this.leggingsColor = leggingsColor;
        this.bootsColor = bootsColor;
    }

    public boolean hasOutfitColors() {
        return chestplateColor != null && leggingsColor != null && bootsColor != null;
    }

    // ... existing getters, plus:
    public Color getChestplateColor() { return chestplateColor; }
    public Color getLeggingsColor()   { return leggingsColor; }
    public Color getBootsColor()      { return bootsColor; }
}