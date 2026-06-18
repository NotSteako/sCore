package kami.gg.souppvp.kit.button;

import kami.gg.souppvp.kit.KitCategory;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CategoryButton extends Button {

    private final Profile profile;

    public CategoryButton(Profile profile) {
        this.profile = profile;
    }

    @Override
    public ItemStack getButtonItem(Player player) {

        List<String> lore = new ArrayList<>();

        lore.add(CC.GRAY + "Click to change category");
        lore.add("");

        for (KitCategory category : KitCategory.values()) {
            boolean selected = profile.getSelectedCategory() == category;

            lore.add(
                    (selected ? CC.GREEN + "● " : CC.GRAY + "○ ")
                            + category.getDisplayName()
            );
        }

        return new ItemBuilder(Material.NAME_TAG)
                .name(CC.GREEN + "Category")
                .lore(lore)
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {

        KitCategory[] categories = KitCategory.values();

        int current = profile.getSelectedCategory().ordinal();
        int next = (current + 1) % categories.length;

        profile.setSelectedCategory(categories[next]);

        playNeutral(player);
    }
}