package kami.gg.souppvp.bounty.button;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.perk.Perk;
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
import java.util.Random;

public class BountyPlayerButton extends Button {

    private Player player;

    public BountyPlayerButton(Player player){
        this.player = player;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        List<String> lore = new ArrayList<>();
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(this.player.getUniqueId());
        Perk tricksterPerk = SoupPvP.getInstance().getPerksHandler().getPerkByName("Trickster");
        lore.add("");
        if (SoupPvP.getInstance().getPerksHandler().getPerkByName(profile.getActivePerks().get(0)) == tricksterPerk){
            lore.add(CC.translate("&fBounty: &b" + new Random().nextInt(1001)));
        } else {
            lore.add(CC.translate("&fBounty: &b" + profile.getBounty()));
        }
        Perk incognitoPerk = SoupPvP.getInstance().getPerksHandler().getPerkByName(profile.getActivePerks().get(2));
        if (SoupPvP.getInstance().getPerksHandler().getPerkByName(profile.getActivePerks().get(2)) != incognitoPerk){
            lore.add(CC.translate("&fCurrent Killstreak: &b" + profile.getCurrentKit().getName()));
        }
        lore.add(CC.translate("&fCurrent Kit: &b" + profile.getCurrentKit().getName()));
        if (SoupPvP.getInstance().getPerksHandler().getPerkByName(profile.getActivePerks().get(0)) == tricksterPerk){
            lore.add(CC.translate("&fCurrent Health: &f" + new Random().nextInt(11) + "&4❤"));
        } else {
            lore.add(CC.translate("&fCurrent Health: &f" + this.player.getHealth()/2 + "&4❤"));
        }
        lore.add(CC.translate("&fDistance: &b" + player.getLocation().distance(this.player.getLocation())));
        lore.add("");
        return new ItemBuilder(Material.PLAYER_HEAD).name(CC.translate("&b" + player.getName())).lore(lore).durability(3).build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {

    }
}
