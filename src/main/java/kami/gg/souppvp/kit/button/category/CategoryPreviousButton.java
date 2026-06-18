package kami.gg.souppvp.kit.button.category;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.KitCategory;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class CategoryPreviousButton extends Button {

    private final KitCategory category;

    public CategoryPreviousButton(KitCategory category) {
        this.category = category;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.ARROW)
                .name(CC.RED + "Previous Page")
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {

        Profile profile = SoupPvP.getInstance()
                .getProfilesHandler()
                .getProfileByUUID(player.getUniqueId());

        int page = profile.getCategoryPage(category);

        if (page > 0) {
            profile.setCategoryPage(category, page - 1);
            playNeutral(player);
        }
    }
}