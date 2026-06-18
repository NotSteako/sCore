package kami.gg.souppvp.perk.menu.button;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.perk.Perk;
import kami.gg.souppvp.perk.menu.adjust.AdjustPerksMenu;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ActivePerkSlotButton extends Button {

    private int tier;

    public ActivePerkSlotButton(int tier){
        this.tier = tier;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        Perk currentPerk = SoupPvP.getInstance().getPerksHandler().getPerkByName(profile.getActivePerks().get(tier-1));
        List<String> lore = new ArrayList<>();
        lore.add(CC.translate("&7Your active Tier " + tier + " perk."));
        lore.add("");
        lore.add(CC.translate("&fCurrent Perk: &a" + currentPerk.getName()));
        lore.add("");
        lore.add(CC.translate("&eClick here to select a Tier " + tier + " perk."));
        return new ItemBuilder(currentPerk.getIcon()).name(CC.translate("&a" + currentPerk.getName())).lore(lore).build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType.isLeftClick()){
            playNeutral(player);
            new AdjustPerksMenu(tier).openMenu(player);
        }
    }

}
