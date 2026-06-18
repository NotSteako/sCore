package kami.gg.souppvp.mooshroom.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import org.bukkit.command.CommandSender;

public class MooshroomRemoveCommand {

    @Command(name = "remove", desc = "remove a named Mooshroom", usage = "<name>")
    @Require("souppvp.mooshroom")
    public void execute(@Sender CommandSender sender, String name) {
        if (name == null || name.isEmpty()) {
            sender.sendMessage(CC.translate("&cYou must provide a name."));
            return;
        }
        boolean removed = SoupPvP.getInstance().getMooshroomHandler().remove(name);
        if (!removed) {
            sender.sendMessage(CC.translate("&cNo Mooshroom named &e" + name + " &cwas found."));
            return;
        }
        sender.sendMessage(CC.translate("&aMooshroom &e" + name + " &ahas been removed."));
    }
}