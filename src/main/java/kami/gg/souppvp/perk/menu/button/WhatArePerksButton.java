package kami.gg.souppvp.perk.menu.button;

import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WhatArePerksButton extends Button {

    @Override
    public ItemStack getButtonItem(Player player) {
        List<String> lore = new ArrayList<>();
        lore.add(CC.translate("&7Perks are able to be equipped by using /perks."));
        lore.add(CC.translate("&7Each perk allows you to customize your playstyle to"));
        lore.add(CC.translate("&7benefit your combat skills. You can select three perks, one"));
        lore.add(CC.translate("&7from each tier level. We design these to prevent them from being too OP."));
        lore.add(CC.translate("&7Perks can only be purchased using in-game credits."));
        return new ItemBuilder(Material.PAPER).name(CC.translate("&bWhat are perks?")).lore(lore).build();
    }

}
