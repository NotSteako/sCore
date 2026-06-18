package kami.gg.souppvp.events.impl.redrover.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import org.bukkit.command.CommandSender;

public class RedRoverCancelCommand {

    @Command(name = "cancel", desc = "cancel red rover event")
    @Require("redrover.host")
    public void execute(@Sender CommandSender sender) {
        if (SoupPvP.getInstance().getRedRoverHandler().getActiveRedRover() == null) {
            sender.sendMessage(CC.translate("&cThere isn't an active red rover event."));
            return;
        }
        SoupPvP.getInstance().getRedRoverHandler().getActiveRedRover().end();
    }

}