package kami.gg.souppvp.coinflip.menu.confirmation.button;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.coinflip.CoinFlipState;
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

public class GameSettingsButton extends Button {

    private int amount;

    public GameSettingsButton(int amount){
        this.amount = amount;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(CC.translate("&e&lWager"));
        lore.add(CC.translate("&a" + amount + " credits"));
        lore.add("");
        lore.add(CC.translate("&7Click to &b&lEDIT &7wager amount!"));
        return new ItemBuilder(Material.WRITABLE_BOOK).name(CC.translate("&b&lGame Settings")).lore(lore).build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType.isLeftClick()){
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
            profile.setCoinFlipState(CoinFlipState.CREATING);
            player.closeInventory();
            player.sendMessage(CC.translate("&7Type an integer amount to set your &e&lwager &7amount."));
            player.sendMessage(CC.translate("&7To &c&lcancel&7, type anything else."));
        }
    }
}
