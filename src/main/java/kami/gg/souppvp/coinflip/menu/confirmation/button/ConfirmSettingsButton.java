package kami.gg.souppvp.coinflip.menu.confirmation.button;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.coinflip.events.WagerCreateEvent;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.TaskUtil;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ConfirmSettingsButton extends Button {

    private int amount;

    public ConfirmSettingsButton(int amount){
        this.amount = amount;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(CC.translate("&7Click to &a&lCONFIRM &7and &a&lPOST &7coinflip game!"));
        lore.add("");
        return new ItemBuilder(Material.INK_SAC).name(CC.translate("&a&lConfirm Settings")).lore(lore).durability(10).build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (!clickType.isLeftClick()) {
            return;
        }

        Profile profile = SoupPvP.getInstance()
                .getProfilesHandler()
                .getProfileByUUID(player.getUniqueId());

        WagerCreateEvent wagerCreateEvent =
                new WagerCreateEvent(player.getUniqueId(), amount);

        Bukkit.getPluginManager().callEvent(wagerCreateEvent);

        profile.setCredits(profile.getCredits() - amount);

        player.sendMessage(CC.translate(
                "&7The wager has been collected. (&a" + amount + " &acredits&7)"
        ));

        TaskUtil.runLater(() -> {
            Menu.currentlyOpenedMenus.remove(player.getName());
            player.closeInventory();
        }, 1L);
    }
}
