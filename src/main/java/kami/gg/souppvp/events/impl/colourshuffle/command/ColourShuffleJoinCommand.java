package kami.gg.souppvp.events.impl.colourshuffle.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.colourshuffle.ColourShuffle;
import kami.gg.souppvp.events.impl.colourshuffle.ColourShuffleState;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class ColourShuffleJoinCommand {

    @Command(name = "join", desc = "join the active colour shuffle event")
    public void execute(@Sender Player player) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        ColourShuffle event = SoupPvP.getInstance().getColourShuffleHandler().getActiveEvent();

        if (!profile.getProfileState().equals(ProfileState.SPAWN)) {
            player.sendMessage(CC.translate("&cYou need to be at spawn to join."));
            return;
        }
        if (event == null) {
            player.sendMessage(CC.translate("&cThere isn't an active Colour Shuffle event."));
            return;
        }
        if (event.getState() != ColourShuffleState.WAITING) {
            player.sendMessage(CC.translate("&cThat event has already started."));
            return;
        }
        if (profile.getColourShuffleEvent() != null) {
            player.sendMessage(CC.translate("&cYou are already in a Colour Shuffle event."));
            return;
        }
        event.handleJoin(player);
    }
}
