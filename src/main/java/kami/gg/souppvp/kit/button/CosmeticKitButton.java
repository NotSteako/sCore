package kami.gg.souppvp.kit.button;

import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.menu.CosmeticSkinMenu;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class CosmeticKitButton extends Button {

    private final Kit kit;

    public CosmeticKitButton(Kit kit) {
        this.kit = kit;
    }

    public ItemStack getButtonItem(Player player) {
        // Reuse whatever display logic KitButton uses — helmet, name, lore, etc.
        // Just swap the action below
        return kit.getIcon();
    }

    @Override
    public void clicked(Player player, ClickType clickType)  {
        // Open the skin picker for this specific kit
        new CosmeticSkinMenu(kit).openMenu(player);
    }
}