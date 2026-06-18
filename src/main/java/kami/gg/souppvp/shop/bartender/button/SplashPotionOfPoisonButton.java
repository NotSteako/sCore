package kami.gg.souppvp.shop.bartender.button;

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

public class SplashPotionOfPoisonButton extends Button {

    private Integer costCredits;

    public SplashPotionOfPoisonButton(Integer costCredits){
        this.costCredits = costCredits;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(CC.translate("&fPrice: &b" + costCredits));
        lore.add("");
        if (profile.getCredits() >= costCredits){
            lore.add(CC.translate("&eClick to purchase!"));
        } else {
            lore.add(CC.translate("&cInsufficient Credits!"));
        }
        return new ItemBuilder(Material.POTION).name(CC.translate("&bSplash Potion Of Poison")).lore(lore).durability(16420).build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType.isLeftClick()){
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
            if (SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player)){
                PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK);
                player.sendMessage(CC.translate("&cYou can't do this in spawn."));
            } else {
                if (profile.getCredits() >= costCredits){
                    if (player.getInventory().firstEmpty() == -1){
                        playFail(player);
                        player.sendMessage(CC.translate("&cYour inventory is full!"));
                        return;
                    }
                    player.getInventory().addItem(new ItemStack(Material.POTION,1,(short) 16420));
                    PlayerUtil.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP);
                    profile.setCredits(profile.getCredits() - costCredits);
                    player.sendMessage(CC.translate("&aSuccessfully bought the &bSplash Potion Of Poison&a."));
                } else {
                    PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK);
                }
            }
        }
    }

}
