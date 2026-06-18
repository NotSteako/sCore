package kami.gg.souppvp.perk.menu.adjust.button;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.perk.Perk;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AdjustActivePerkSlotButton extends Button {

    private int tier;

    public AdjustActivePerkSlotButton(int tier){
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
        lore.add(CC.translate("&eAdjust your Tier " + tier + " perk below."));
        return new ItemBuilder(currentPerk.getIcon()).name(CC.translate("&a" + currentPerk.getName())).lore(lore).build();
    }

}
