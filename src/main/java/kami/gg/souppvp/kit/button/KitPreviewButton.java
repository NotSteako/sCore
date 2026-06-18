package kami.gg.souppvp.kit.button;

import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.menu.KitPreviewMenu;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class KitPreviewButton extends Button {

    private final Kit kit;

    public KitPreviewButton(Kit kit) {
        this.kit = kit;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§e" + kit.getName());
        meta.setLore(Arrays.asList(
                "",
                "§7§oRight-click to preview kit",
                ""
        ));

        item.setItemMeta(meta);
        return item;
    }

    public void clicked(Player player, int slot, ClickType clickType) {
        if (clickType == ClickType.RIGHT) {
            // Close current menu and open the preview
            player.closeInventory();
            new KitPreviewMenu(kit).openMenu(player);
        }
        // Left click does nothing (or you could keep existing behaviour)
    }
}