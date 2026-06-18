package kami.gg.souppvp.mooshroom.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MooshroomCreateCommand {

    @Command(name = "create", desc = "create a named Mooshroom at your location", usage = "<name>")
    @Require("souppvp.mooshroom")
    public void execute(@Sender Player player, String name) {
        if (name == null || name.isEmpty()) {
            player.sendMessage(CC.translate("&cYou must provide a name."));
            return;
        }
        Location loc = player.getLocation();
        boolean ok = SoupPvP.getInstance().getMooshroomHandler().create(name, loc);
        if (!ok) {
            player.sendMessage(CC.translate("&cA Mooshroom named &e" + name + " &calready exists. Pick another name."));
            return;
        }
        player.sendMessage(CC.translate("&aMooshroom &e" + name + " &aspawned at your location."));
    }
}