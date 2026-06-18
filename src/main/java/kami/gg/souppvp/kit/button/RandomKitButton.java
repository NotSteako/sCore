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
import java.util.Random;

public class RandomKitButton extends Button {

    @Override
    public ItemStack getButtonItem(Player player) {
        List<String> lore = new ArrayList<>();
        lore.add(CC.translate("&7Receive a random kit you own!"));
        lore.add("");
        lore.add(CC.translate("&eClick to randomize!"));
        return new ItemBuilder(Material.JUKEBOX).name(CC.translate("&bRandomise A Kit")).lore(lore).build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType.isLeftClick()){
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
            List<String> unlockedKits = profile.getUnlockedKits();
            Random random = new Random();
            String kitName = unlockedKits.get(random.nextInt(unlockedKits.size()));
            Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName(kitName);
            PlayerUtil.playSound(player, Sound.UI_BUTTON_CLICK);
            profile.setPreviousKit(profile.getCurrentKit());
            profile.setCurrentKit(kit);
            player.sendMessage(CC.translate("&aSuccessfully equipped the &r" + kit.getRarityType().getColor() + kit.getName() + "&a kit."));
        }
    }

}
