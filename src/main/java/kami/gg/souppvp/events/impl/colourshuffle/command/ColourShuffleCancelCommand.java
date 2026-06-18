package kami.gg.souppvp.events.impl.colourshuffle.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import org.bukkit.command.CommandSender;

public class ColourShuffleCancelCommand {

    @Command(name = "", desc = "cancel the active colour shuffle event")
    @Require("souppvp.admin")
    public void execute(@Sender CommandSender sender) {
        if (SoupPvP.getInstance().getColourShuffleHandler().getActiveEvent() == null) {
            sender.sendMessage(CC.translate("&cThere isn't an active Colour Shuffle event."));
            return;
        }
        SoupPvP.getInstance().getColourShuffleHandler().getActiveEvent().end();
        sender.sendMessage(CC.translate("&aColour Shuffle event cancelled."));
    }
}
