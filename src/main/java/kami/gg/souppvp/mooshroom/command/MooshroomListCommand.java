package kami.gg.souppvp.mooshroom.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.mooshroom.handler.MooshroomHandler;
import kami.gg.souppvp.util.CC;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class MooshroomListCommand {

    @Command(name = "list", desc = "list every named Mooshroom")
    @Require("souppvp.mooshroom")
    public void execute(@Sender CommandSender sender) {
        MooshroomHandler handler = SoupPvP.getInstance().getMooshroomHandler();
        Set<String> names = handler.getNames();
        if (names.isEmpty()) {
            sender.sendMessage(CC.translate("&cThere are no Mooshrooms placed."));
            return;
        }
        sender.sendMessage(CC.translate("&d&lMooshrooms &7(" + names.size() + ")"));
        for (String name : names) {
            Location loc = handler.getLocation(name);
            sender.sendMessage(CC.translate(
                    "&7- &f" + name + " &8(" + loc.getWorld().getName()
                            + " " + (int) loc.getX()
                            + ", " + (int) loc.getY()
                            + ", " + (int) loc.getZ() + ")"));
        }
    }
}