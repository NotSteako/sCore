package kami.gg.souppvp.mooshroom.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.util.CC;
import org.bukkit.command.CommandSender;

public class MooshroomCommand {

    @Command(name = "", desc = "mooshroom command help")
    @Require("souppvp.mooshroom")
    public void usage(@Sender CommandSender sender) {
        sender.sendMessage(CC.translate("&d&lMooshroom Commands"));
        sender.sendMessage(CC.translate("&7/mooshroom create <name> &8- &fspawn a Mooshroom at your location"));
        sender.sendMessage(CC.translate("&7/mooshroom remove <name> &8- &fdelete that Mooshroom"));
        sender.sendMessage(CC.translate("&7/mooshroom list         &8- &flist all Mooshrooms"));
    }
}