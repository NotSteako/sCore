package kami.gg.souppvp.kit.button;

import kami.gg.souppvp.kit.menu.KitCosmeticsMenu;
import kami.gg.souppvp.kit.menu.KitsSelectMenu;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class BackToKitsButton extends Button {

    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.CHEST)
                .name(CC.translate("&aBack to Kits"))
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        new KitsSelectMenu().openMenu(player);
    }
}