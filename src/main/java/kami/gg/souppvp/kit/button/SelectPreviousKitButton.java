package kami.gg.souppvp.kit.button;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
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

public class SelectPreviousKitButton extends Button {

    private Profile profile;

    public SelectPreviousKitButton(Profile profile){
        this.profile = profile;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        Kit previousKit = profile.getPreviousKit();
        Kit currentKit = profile.getCurrentKit();
        List<String> lore = new ArrayList<>();
        lore.add(CC.translate("&7Receive your previous kit!"));
        lore.add("");
        if (profile.getPreviousKit() == null){
            lore.add(CC.translate("&fPrevious Kit: &cNone"));
        } else {
            lore.add(CC.translate("&fPrevious Kit: &r" + previousKit.getRarityType().getColor() + previousKit.getName()));
        }
        lore.add(CC.translate("&fCurrent Kit: &r" + currentKit.getRarityType().getColor() + currentKit.getName()));
        lore.add("");
        lore.add(CC.translate("&eClick to receive!"));
        return new ItemBuilder(Material.CLOCK).name(CC.translate("&bSelect Previous Kit")).lore(lore).build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType.isLeftClick()){
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
            Kit previousKit = profile.getPreviousKit();
            Kit currentKit = profile.getCurrentKit();
            if (previousKit == null){
                playFail(player);
                player.sendMessage(CC.translate("&cYou don't have a previous kit."));
                return;
            }
            PlayerUtil.playSound(player, Sound.UI_BUTTON_CLICK);
            profile.setCurrentKit(previousKit);
            profile.setPreviousKit(currentKit);
            player.sendMessage(CC.translate("&aSuccessfully equipped the &r" + previousKit.getRarityType().getColor() + previousKit.getName() + "&a kit."));
        }
    }

}
