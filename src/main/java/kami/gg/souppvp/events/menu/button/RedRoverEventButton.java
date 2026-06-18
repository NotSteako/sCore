package kami.gg.souppvp.events.menu.button;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.redrover.RedRover;
import kami.gg.souppvp.events.impl.redrover.RedRoverState;
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

public class RedRoverEventButton extends Button {

    @Override
    public ItemStack getButtonItem(Player player) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        List<String> lore = new ArrayList<>();
        lore.add(CC.translate("&7Captain-drafted team event."));
        lore.add(CC.translate("&72 random captains pick their teams,"));
        lore.add(CC.translate("&7then fight 1v1 'winner stays on' style."));
        lore.add(CC.translate("&7Last team standing wins!"));
        lore.add("");

        RedRover active = SoupPvP.getInstance().getRedRoverHandler().getActiveRedRover();
        if (active != null) {
            lore.add(CC.translate("&cOngoing Red Rover Event:"));
            lore.add(CC.translate("&7• &fHost: &c" + active.getHost().getUsername()));
            lore.add(CC.translate("&7• &fParticipants: &c" + active.getEventPlayers().size() + "&f/&c" + active.getMaxPlayers()));
            if (active.getState().equals(RedRoverState.WAITING))         lore.add(CC.translate("&7• &fState: &cWaiting..."));
            else if (active.getState().equals(RedRoverState.DRAFTING))   lore.add(CC.translate("&7• &fState: &cDrafting"));
            else                                                         lore.add(CC.translate("&7• &fState: &cFighting"));
            lore.add("");
            if (active.getEventPlayers().containsKey(player.getUniqueId())) lore.add(CC.translate("&eYou're in this event!"));
            else if (active.getState().equals(RedRoverState.WAITING))       lore.add(CC.translate("&eClick to join!"));
            else                                                            lore.add(CC.translate("&cAlready in progress."));
        } else {
            if (player.hasPermission("redrover.host")) lore.add(CC.translate("&eClick to host!"));
            else                                       lore.add(CC.translate("&cYou cannot host this event."));
        }
        return new ItemBuilder(Material.REDSTONE_BLOCK).name(CC.translate("&cRed Rover &7Event")).lore(lore).build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (!clickType.isLeftClick()) return;

        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (!profile.getProfileState().equals(ProfileState.SPAWN)) {
            playFail(player);
            player.sendMessage(CC.translate("&cYou can only do this at spawn."));
            return;
        }

        RedRover active = SoupPvP.getInstance().getRedRoverHandler().getActiveRedRover();

        if (active != null && active.getEventPlayers().containsKey(player.getUniqueId())) {
            PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK);
            return;
        }

        if (active == null) {
            if (!player.hasPermission("redrover.host")) {
                PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK);
                player.sendMessage(CC.translate("&cYou do not have permission to host a Red Rover event."));
                return;
            }
            SoupPvP.getInstance().getRedRoverHandler().setActiveRedRover(new RedRover(player));
            SoupPvP.getInstance().getRedRoverHandler().getActiveRedRover().handleJoin(player);
            PlayerUtil.playSound(player, Sound.UI_BUTTON_CLICK);
            return;
        }

        if (active.getState() != RedRoverState.WAITING) {
            PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK);
            player.sendMessage(CC.translate("&cThat red rover event is currently on-going and cannot be joined."));
            return;
        }
        active.handleJoin(player);
        PlayerUtil.playSound(player, Sound.UI_BUTTON_CLICK);
    }

}