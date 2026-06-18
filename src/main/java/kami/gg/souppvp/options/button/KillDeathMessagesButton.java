package kami.gg.souppvp.options.button;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.PlayerUtil;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class KillDeathMessagesButton extends Button {

    @Override
    public ItemStack getButtonItem(Player player) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        List<String> lore = new ArrayList<>();
        lore.add(CC.translate("&7Edit the visibility of the kills/deaths messages."));
        lore.add("");
        if (profile.getEnableKillDeathMessages()){
            lore.add(CC.translate("&f▸  &aEnabled"));
            lore.add(CC.translate("&f  &fDisabled"));
        }  else {
            lore.add(CC.translate("&f  &fEnabled"));
            lore.add(CC.translate("&f▸  &aDisabled"));
        }
        lore.add("");
        lore.add(CC.translate("&eClick to toggle!"));
        return new ItemBuilder(Material.BLAZE_POWDER).name(CC.translate("&bKill/Death Messages")).lore(lore).build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType.isLeftClick()){
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
            profile.setEnableKillDeathMessages(!profile.getEnableKillDeathMessages());
            PlayerUtil.playSound(player, Sound.UI_BUTTON_CLICK);
        }
    }

}
