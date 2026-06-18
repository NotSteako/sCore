package kami.gg.souppvp.kit.button;

import kami.gg.souppvp.kit.menu.KitCosmeticsMenu;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class BackToCosmeticsButton extends Button {

    @Override
    public ItemStack getButtonItem(Player player) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(CC.translate("&c&lBack"));
        meta.setLore(Arrays.asList(
                CC.translate("&7Return to Kit Skins")
        ));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        new KitCosmeticsMenu().openMenu(player);
    }
}