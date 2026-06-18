package kami.gg.souppvp.events.menu.button;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.Events;
import kami.gg.souppvp.events.impl.sumo.Sumo;
import kami.gg.souppvp.events.impl.sumo.SumoState;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
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

public class SumoEventButton extends Button {

    @Override
    public ItemStack getButtonItem(Player player) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        List<String> lore = new ArrayList<>();
        lore.add(CC.translate("&7Single elimination styled event."));
        lore.add(CC.translate("&7All players take turns fighting for the platform"));
        lore.add(CC.translate("&7while being spectated by other contestants."));
        lore.add(CC.translate("&7Win the event by not losing any of your matches."));
        lore.add("");
        if (SoupPvP.getInstance().getSumoHandler().getActiveSumo() != null){
            lore.add(CC.translate("&bOngoing Sumo Event:"));
            lore.add(CC.translate("&7• &fHost: &b" + SoupPvP.getInstance().getSumoHandler().getActiveSumo().getHost().getUsername()));
            lore.add(CC.translate("&7• &fParticipants: &b" + SoupPvP.getInstance().getSumoHandler().getActiveSumo().getEventPlayers().size() + "&f/&b" + SoupPvP.getInstance().getSumoHandler().getActiveSumo().getMaxPlayers()));
            if (SoupPvP.getInstance().getSumoHandler().getActiveSumo().getState().equals(SumoState.WAITING)){
                lore.add(CC.translate("&7• &fState: &bWaiting..."));
            } else {
                lore.add(CC.translate("&7• &fState: &bFighting"));
            }
            lore.add("");
            if (profile.isInEvent()){
                lore.add(CC.translate("&eYou're in this event!"));
            } else {
                lore.add(CC.translate("&eClick to join!"));
            }
        } else {
            lore.add(CC.translate("&eClick to host!"));
        }
        return new ItemBuilder(Events.SUMO.getMaterial()).name(CC.translate("&bSumo &7Event")).lore(lore).build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType.isLeftClick()){
            if (SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId()).getProfileState().equals(ProfileState.SPAWN)){
                if (player.hasPermission("souppvp.sumohost")){
                    if (SoupPvP.getInstance().getSumoHandler().getActiveSumo() != null){
                        PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK);
                    } else {
                        SoupPvP.getInstance().getSumoHandler().setActiveSumo(new Sumo(player));
                        SoupPvP.getInstance().getSumoHandler().getActiveSumo().handleJoin(player);
                        PlayerUtil.playSound(player, Sound.UI_BUTTON_CLICK);
                    }
                } else {
                    PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK);
                }
                Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
                if (!profile.isInEvent()){
                    SoupPvP.getInstance().getSumoHandler().getActiveSumo().handleJoin(player);
                    PlayerUtil.playSound(player, Sound.UI_BUTTON_CLICK);
                } else {
                    PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK);
                }
            } else {
                playFail(player);
                player.sendMessage(CC.translate("&cYou can only do this at spawn."));
            }
        }
    }

}
