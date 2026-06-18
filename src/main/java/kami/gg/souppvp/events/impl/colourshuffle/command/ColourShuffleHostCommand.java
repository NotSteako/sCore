package kami.gg.souppvp.events.impl.colourshuffle.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.colourshuffle.ColourShuffle;
import kami.gg.souppvp.events.impl.colourshuffle.task.ColourShuffleStartTask;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class ColourShuffleHostCommand {

    @Command(name = "host", desc = "host a colour shuffle event")
    @Require("souppvp.colourshufflehost")
    public void execute(@Sender Player player) {
        if (SoupPvP.getInstance().getColourShuffleHandler().getActiveEvent() != null) {
            player.sendMessage(CC.translate("&cThere is already an active Colour Shuffle event."));
            return;
        }
        if (!SoupPvP.getInstance().getColourShuffleHandler().isConfigured()) {
            player.sendMessage(CC.translate("&cColour Shuffle isn't fully configured. Use &e/colourshuffle setspawn&c, &e/colourshuffle setfloora&c, &e/colourshuffle setfloorb&c first."));
            return;
        }
        ColourShuffle event = new ColourShuffle(player);
        SoupPvP.getInstance().getColourShuffleHandler().setActiveEvent(event);
        event.setEventTask(new ColourShuffleStartTask(event));
        event.handleJoin(player);
    }
}
