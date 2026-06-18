package kami.gg.souppvp.perk.menu.button;

import kami.gg.souppvp.perk.menu.adjust.AdjustPerksMenu;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EmptyPerkSlotButton extends Button {

    private int tier;

    public EmptyPerkSlotButton(int tier){
        this.tier = tier;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        List<String> lore = new ArrayList<>();
        lore.add(CC.translate("&7Your active Tier " + tier + " perk."));
        lore.add("");
        lore.add(CC.translate("&fCurrent Perk: &6None"));
        lore.add("");
        lore.add(CC.translate("&eClick here to select a Tier " + tier + " perk."));
        return new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).name(CC.translate("&cEmpty Perk Slot")).lore(lore).durability(14).build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType.isLeftClick()){
            playNeutral(player);
            new AdjustPerksMenu(tier).openMenu(player);
        }
    }
}
