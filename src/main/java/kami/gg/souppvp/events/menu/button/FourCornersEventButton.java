package kami.gg.souppvp.events.menu.button;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.Events;
import kami.gg.souppvp.events.impl.fourcorners.FourCorners;
import kami.gg.souppvp.events.impl.fourcorners.FourCornersState;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.PlayerUtil;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FourCornersEventButton extends Button {

    @Override
    public ItemStack getButtonItem(Player player) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        List<String> lore = new ArrayList<>();
        lore.add(CC.translate("&7Multi-elimination styled event."));
        lore.add(CC.translate("&7Rush to one of four colored corners before"));
        lore.add(CC.translate("&7the bridges drop. Only one corner survives"));
        lore.add(CC.translate("&7each round. Last player standing wins."));
        lore.add("");
        FourCorners active = SoupPvP.getInstance().getFourCornersHandler().getActiveEvent();
        if (active != null) {
            lore.add(CC.translate("&bOngoing 4Corners Event:"));
            lore.add(CC.translate("&7• &fHost: &b" + active.getHost().getUsername()));
            lore.add(CC.translate("&7• &fParticipants: &b" + active.getEventPlayers().size() + "&f/&b" + active.getMaxPlayers()));
            lore.add(active.getState().equals(FourCornersState.WAITING)
                    ? CC.translate("&7• &fState: &bWaiting...")
                    : CC.translate("&7• &fState: &bFighting"));
            lore.add("");
            lore.add(profile.getFourCornersEvent() != null
                    ? CC.translate("&eYou're in this event!")
                    : CC.translate("&eClick to join!"));
        } else {
            lore.add(CC.translate("&eClick to host!"));
        }
        return new ItemBuilder(Events.FOUR_CORNERS.getMaterial()).name(CC.translate("&b4Corners &7Event")).lore(lore).build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (!clickType.isLeftClick()) return;
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (!profile.getProfileState().equals(ProfileState.SPAWN)) {
            playFail(player); player.sendMessage(CC.translate("&cYou can only do this at spawn.")); return;
        }
        FourCorners active = SoupPvP.getInstance().getFourCornersHandler().getActiveEvent();
        if (active == null) {
            if (!player.hasPermission("souppvp.fourcornershost")) { PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK); return; }
            SoupPvP.getInstance().getFourCornersHandler().setActiveEvent(new FourCorners(player));
            SoupPvP.getInstance().getFourCornersHandler().getActiveEvent().handleJoin(player);
            PlayerUtil.playSound(player, Sound.UI_BUTTON_CLICK); return;
        }
        if (active.getState() != FourCornersState.WAITING) {
            PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK);
            player.sendMessage(CC.translate("&cThat 4Corners event is currently on-going and cannot be joined.")); return;
        }
        if (profile.getFourCornersEvent() != null) { PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK); return; }
        active.handleJoin(player); PlayerUtil.playSound(player, Sound.UI_BUTTON_CLICK);
    }
}