package kami.gg.souppvp.coinflip.button;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.coinflip.CoinFlipState;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.PlayerUtil;
import kami.gg.souppvp.util.TaskUtil;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CreateWagerButton extends Button {

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType.isLeftClick()){
            if (SoupPvP.getInstance().getCoinFlipsHandler().hasCoinFlipWager(player.getUniqueId())) {
                player.sendMessage(CC.translate("&cYou already have a coin flip wager available."));
                PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK);
            } else {
                Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
                profile.setCoinFlipState(CoinFlipState.CREATING);
                PlayerUtil.playSound(player, Sound.UI_BUTTON_CLICK);
                TaskUtil.runLater(player::closeInventory, 1L);
                player.sendMessage(CC.translate("&7Type an integer amount to set your &e&lwager &7amount."));
                player.sendMessage(CC.translate("&7To &c&lcancel&7, type anything else."));
            }
        }
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(CC.translate("&7Create to create your own custom game!"));
        lore.add("");
        return new ItemBuilder(Material.WRITABLE_BOOK).name(CC.translate("&b&lCreate Game")).lore(lore).build();
    }

}
