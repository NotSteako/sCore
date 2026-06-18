package kami.gg.souppvp.events.impl.colourshuffle.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class ColourShuffleLeaveCommand {

    @Command(name = "leave", desc = "leave the colour shuffle event")
    public void execute(@Sender Player player) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (profile.getColourShuffleEvent() == null) {
            player.sendMessage(CC.translate("&cYou are not in a Colour Shuffle event."));
            return;
        }
        profile.getColourShuffleEvent().handleLeave(player);
    }
}
