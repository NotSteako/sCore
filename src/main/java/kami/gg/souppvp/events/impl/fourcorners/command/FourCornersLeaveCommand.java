package kami.gg.souppvp.events.impl.fourcorners.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.fourcorners.FourCorners;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class FourCornersLeaveCommand {

    @Command(name = "leave", desc = "leave 4Corners event")
    public void execute(@Sender Player player) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        FourCorners active = SoupPvP.getInstance().getFourCornersHandler().getActiveEvent();
        if (active == null) { player.sendMessage(CC.translate("&cThere isn't an active 4Corners event.")); return; }
        if (profile.getFourCornersEvent() == null || !active.getEventPlayers().containsKey(player.getUniqueId())) {
            player.sendMessage(CC.translate("&cYou are not apart of the active 4Corners event.")); return;
        }
        active.handleLeave(player);
    }
}