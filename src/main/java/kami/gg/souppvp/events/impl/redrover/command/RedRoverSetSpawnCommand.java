package kami.gg.souppvp.events.impl.redrover.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class RedRoverSetSpawnCommand {

    @Command(name = "setspawn", desc = "set red rover event locations", usage = "<position>")
    @Require("redrover.host")
    public void execute(@Sender Player player, String position) {
        switch (position.toLowerCase()) {
            case "a":
                SoupPvP.getInstance().getRedRoverHandler().setSpawnA(player.getLocation());
                player.sendMessage(CC.translate("&aSuccessfully updated the red rover system's spawn a location."));
                SoupPvP.getInstance().getRedRoverHandler().save();
                break;
            case "b":
                SoupPvP.getInstance().getRedRoverHandler().setSpawnB(player.getLocation());
                player.sendMessage(CC.translate("&aSuccessfully updated the red rover system's spawn b location."));
                SoupPvP.getInstance().getRedRoverHandler().save();
                break;
            case "spec":
                SoupPvP.getInstance().getRedRoverHandler().setSpectatorSpawn(player.getLocation());
                player.sendMessage(CC.translate("&aSuccessfully updated the red rover system's spectator spawn location."));
                SoupPvP.getInstance().getRedRoverHandler().save();
                break;
            default:
                player.sendMessage(CC.translate("&cAvailable Positions: a,b,spec"));
                break;
        }
    }

}