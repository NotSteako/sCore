package kami.gg.souppvp.kit;

import org.bukkit.Material;

public enum KitCategory {

    ALL("All", Material.CHEST),
    VALORANT("Valorant", Material.ENDER_EYE),
    MARVEL("Marvel", Material.NETHER_STAR),
    OVERWATCH("Overwatch", Material.BOW);
//    MEDIA("Media", Material.PAPER);

    private final String displayName;
    private final Material material;

    KitCategory(String displayName, Material material) {
        this.displayName = displayName;
        this.material = material;
    }



    public String getDisplayName() {
        return displayName;
    }

    public Material getMaterial() {
        return material;
    }
}