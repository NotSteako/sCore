package kami.gg.souppvp.events.impl.redrover.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.redrover.RedRover;
import kami.gg.souppvp.events.impl.redrover.RedRoverState;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class RedRoverJoinCommand {

    @Command(name = "join", desc = "join red rover event")
    public void execute(@Sender Player player) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        RedRover active = SoupPvP.getInstance().getRedRoverHandler().getActiveRedRover();

        if (!profile.getProfileState().equals(ProfileState.SPAWN)) {
            player.sendMessage(CC.translate("&cYou cannot join the red rover event right now. You need to be at spawn."));
            return;
        }
        if (active == null) { player.sendMessage(CC.translate("&cThere isn't an active red rover event.")); return; }
        if (active.getState() != RedRoverState.WAITING) {
            player.sendMessage(CC.translate("&cThat red rover event is currently on-going and cannot be joined."));
            return;
        }
        if (active.getEventPlayers().containsKey(player.getUniqueId())) {
            player.sendMessage(CC.translate("&cYou are already in the red rover event."));
            return;
        }
        active.handleJoin(player);
    }

}