package kami.gg.souppvp.events.impl.fourcorners.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import org.bukkit.command.CommandSender;

public class FourCornersCancelCommand {

    @Command(name = "cancel", desc = "cancel 4Corners event")
    @Require("souppvp.fourcornerscancel")
    public void execute(@Sender CommandSender sender) {
        if (SoupPvP.getInstance().getFourCornersHandler().getActiveEvent() == null) {
            sender.sendMessage(CC.translate("&cThere isn't an active 4Corners event.")); return;
        }
        SoupPvP.getInstance().getFourCornersHandler().getActiveEvent().end();
    }
}