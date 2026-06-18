package kami.gg.souppvp.options.button;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.tier.Tiers;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.PlayerUtil;
import kami.gg.souppvp.util.TaskUtil;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.menus.ConfirmMenu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ResetStatisticsButton extends Button {

    private Integer price;

    public ResetStatisticsButton(Integer price){
        this.price = price;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        List<String> lore = new ArrayList<>();
        lore.add(CC.translate("&7This procedure will reset every soup pvp statistics related."));
        lore.add(CC.translate("&7Hence, this will not affect your network rank, punishments, etc."));
        lore.add("");
        lore.add(CC.translate("&fPrice: &b" + price));
        lore.add("");
        if (profile.getCredits() >= price){
            lore.add(CC.translate("&eClick to reset!"));
        } else {
            lore.add(CC.translate("&cInsufficient Credits!"));
        }
        return new ItemBuilder(Material.COMPARATOR).name(CC.translate("&bReset Statistics")).lore(lore).build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType.isLeftClick()){
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
            if (SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player)){
                if (profile.getCredits() >= price){
                    playNeutral(player);
                    new ConfirmMenu("Select a procedure action", data -> {
                        if (data){
                            List<String> unlockedKits = new ArrayList<>();
                            unlockedKits.add("Default");
                            TaskUtil.runLater(player::closeInventory, 1L);
                            PlayerUtil.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP);
                            profile.setCurrentKit(SoupPvP.getInstance().getKitsHandler().getKitByName("Default"));
                            profile.setPreviousKit(SoupPvP.getInstance().getKitsHandler().getKitByName("Default"));
                            profile.setUnlockedKits(unlockedKits);
                            profile.setKills(0);
                            profile.setDeaths(0);
                            profile.setCredits(0);
                            profile.setExperiences(0);
                            profile.setTier(Tiers.ZERO);
                            profile.setCurrentKillstreak(0);
                            profile.setHighestKillstreak(0);
                            List<String> activePerks = new ArrayList<>();
                            activePerks.add("None");
                            activePerks.add("None");
                            activePerks.add("None");
                            profile.setActivePerks(activePerks);
                            List<String> unlockedPerks = new ArrayList<>();
                            profile.setUnlockedPerks(unlockedPerks);
                            profile.setTotalWagerGames(0);
                            profile.setWagersWon(0);
                            profile.setWagersLost(0);
                            player.sendMessage(CC.translate("&aSuccessfully reset your statistics."));
                        }
                    }).openMenu(player);
                } else {
                    PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK);
                }
            } else {
                player.sendMessage(CC.translate("&cYou can only do this in spawn."));
            }
        }
    }

}
