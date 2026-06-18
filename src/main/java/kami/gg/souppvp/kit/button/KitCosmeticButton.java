package kami.gg.souppvp.kit.button;

import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.menu.CosmeticSelectionMenu;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class KitCosmeticButton extends Button {

    private final Kit kit;

    public KitCosmeticButton(Kit kit) {
        this.kit = kit;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        return kit.getIcon();
    }

//    @Override
//    public void clicked(Player player, ClickType clickType) {
//        new CosmeticSelectionMenu(kit).openMenu(player);
//    }
}