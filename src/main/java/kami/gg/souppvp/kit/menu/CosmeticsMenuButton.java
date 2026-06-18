package kami.gg.souppvp.kit.menu;

import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class CosmeticsMenuButton extends Button {

    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.PLAYER_HEAD)
                .name(CC.translate("&d&lKit Cosmetics"))
                .lore(
                        "",
                        "&7Change the appearance",
                        "&7of your kit helmets.",
                        "",
                        "&eClick to open."
                )
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        new KitCosmeticsMenu().openMenu(player);
    }
}