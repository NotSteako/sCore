package kami.gg.souppvp.events.impl.fourcorners.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.fourcorners.FourCorners;
import kami.gg.souppvp.events.impl.fourcorners.FourCornersState;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class FourCornersJoinCommand {

    @Command(name = "join", desc = "join 4Corners event")
    public void execute(@Sender Player player) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        FourCorners active = SoupPvP.getInstance().getFourCornersHandler().getActiveEvent();

        if (!profile.getProfileState().equals(ProfileState.SPAWN)) {
            player.sendMessage(CC.translate("&cYou cannot join the 4Corners event right now. You need to be at spawn.")); return;
        }
        if (active == null) { player.sendMessage(CC.translate("&cThere isn't an active 4Corners event.")); return; }
        if (active.getState() != FourCornersState.WAITING) {
            player.sendMessage(CC.translate("&cThat 4Corners event is currently on-going and cannot be joined.")); return;
        }
        if (profile.getFourCornersEvent() != null) {
            player.sendMessage(CC.translate("&cYou are already in a 4Corners event.")); return;
        }
        active.handleJoin(player);
    }
}
