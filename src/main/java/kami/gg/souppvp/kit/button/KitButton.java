package kami.gg.souppvp.kit.button;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.PlayerUtil;
import kami.gg.souppvp.util.TaskUtil;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.menus.ConfirmMenu;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class KitButton extends Button {

    private Kit kit;

    public KitButton(Kit kit){
        this.kit = kit;
    }

    @Override
    public ItemStack getButtonItem(Player player) {

        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        List<String> lore = new ArrayList<>();

        boolean free = SoupPvP.getIsFreeKitsMode();

        boolean unlocked =
                free ||
                        profile.getUnlockedKits().contains(kit.getName()) ||
                        player.hasPermission("souppvp." + kit.getName().toLowerCase());

        lore.add(CC.MENU_BAR);
        for (String line : kit.getDescription()){
            lore.add(CC.translate(line));
        }
        lore.add(CC.MENU_BAR);
        lore.add("");
        if (SoupPvP.getIsFreeKitsMode()){
            lore.add(CC.translate("&fStatus: " + (unlocked ? "&aUnlocked" : "&cLocked")));
        } else {

            boolean hasPermission =
                    !player.isOp() &&
                            player.hasPermission("souppvp." + kit.getName().toLowerCase());

            String statusContext2 =
                    profile.getUnlockedKits().contains(kit.getName()) || hasPermission
                            ? "&aUnlocked"
                            : "&cLocked";

          //  String statusContext = profile.getUnlockedKits().contains(kit.getName()) || player.hasPermission("souppvp." + kit.getName().toLowerCase()) ? "&aUnlocked" : "&cLocked";
            lore.add(CC.translate("&fStatus: &r" + statusContext2));
            if (!profile.getUnlockedKits().contains(kit.getName())){
                lore.add(CC.translate("&fPrice: &c" + kit.getPrice()));
            }
        }
        lore.add(CC.translate("&fRarity: " + kit.getRarityType().getColor() + kit.getRarityType().getName()));
        lore.add("");
        if (SoupPvP.getIsFreeKitsMode()){
            lore.add(CC.translate("&eClick here to equip this kit."));
        } else {
            if (profile.getUnlockedKits().contains(kit.getName())){
                lore.add(CC.translate("&eClick here to equip this kit."));
            } else {
                if (profile.getCredits() >= kit.getPrice()){
                    lore.add(CC.translate("&eClick here to purchase this kit."));
                } else {
                    lore.add(CC.translate("&cInsufficient Credits!"));
                }
            }
        }
        return new ItemBuilder(kit.getIcon()).name(CC.translate(kit.getRarityType().getColor() + kit.getName())).lore(lore).build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType.isLeftClick()){
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
            if (SoupPvP.getIsFreeKitsMode()){
                PlayerUtil.playSound(player, Sound.UI_BUTTON_CLICK);
                profile.setPreviousKit(profile.getCurrentKit());
                profile.setCurrentKit(kit);

                player.sendMessage(CC.translate("&aSuccessfully equipped the &r" + kit.getRarityType().getColor() + kit.getName() + "&a kit."));
            } else {
                if (profile.getUnlockedKits().contains(kit.getName())){
                    PlayerUtil.playSound(player, Sound.UI_BUTTON_CLICK);
                    profile.setPreviousKit(profile.getCurrentKit());
                    profile.setCurrentKit(kit);

                    player.sendMessage(CC.translate("&aSuccessfully equipped the &r" + kit.getRarityType().getColor() + kit.getName() + "&a kit."));
                } else {
                    if (profile.getCredits() >= kit.getPrice()){
                        PlayerUtil.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP);
                        new ConfirmMenu("Select a procedure action", data -> {
                            if (data){
                                TaskUtil.runLater(player::closeInventory, 1L);
                                PlayerUtil.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP);
                                profile.setCredits(profile.getCredits() - kit.getPrice());
                                profile.getUnlockedKits().add(kit.getName());
                                profile.setCurrentKit(kit);
                                player.sendMessage(CC.translate("&aSuccessfully purchased the kit &r" + kit.getRarityType().getColor() + kit.getName() + " &afor &6" + kit.getPrice() + " &acredits."));
                                player.sendMessage(CC.translate("&aSuccessfully equipped the &r" + kit.getRarityType().getColor() + kit.getName() + "&a kit."));
                            }
                        }).openMenu(player);
                    } else {
                        PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK);
                    }
                }
            }
        }
    }

}
