package kami.gg.souppvp.perk.inherit.tier3;

import kami.gg.souppvp.perk.Perk;
import kami.gg.souppvp.util.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ConartistPerk extends Perk {

    @Override
    public String getName() {
        return "Conartist";
    }

    @Override
    public List<String> getDescription() {
        List<String> lore = new ArrayList<>();
        lore.add(CC.translate("&7There is a 50% chance you do not"));
        lore.add(CC.translate("&7drop any soup on death."));
        return lore;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Material.MUSHROOM_STEW);
    }

    @Override
    public int getCost() {
        return 500;
    }

    @Override
    public int getTier() {
        return 3;
    }

}
