package kami.gg.souppvp.kit.button.category;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.KitCategory;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class CategoryRandomButton extends Button {

    private final KitCategory category;

    public CategoryRandomButton(KitCategory category) {
        this.category = category;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(category.getMaterial())
                .name(CC.GREEN + category.getDisplayName())
                .lore(
                        CC.GRAY + "Click to equip a",
                        CC.GRAY + "random kit from",
                        CC.GRAY + "this category."
                )
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {

        List<Kit> kits = SoupPvP.getInstance()
                .getKitsHandler()
                .getKits()
                .stream()
                .filter(kit -> kit.getCategory() == category)
                .collect(Collectors.toList());

        if (kits.isEmpty()) {
            return;
        }

        Kit randomKit = kits.get(
                ThreadLocalRandom.current().nextInt(kits.size())
        );

        randomKit.onSelect(player);
        playNeutral(player);
    }
}