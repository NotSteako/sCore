package kami.gg.souppvp.events.impl.sumo.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class SumoSetSpawnCommand {

	@Command(name = "setspawn", desc = "set sumo event locations", usage = "<position>")
	@Require("souppvp.sumosetspawn")
	public void execute(@Sender Player player, String position) {
        switch (position) {
            case "a":
                SoupPvP.getInstance().getSumoHandler().setSpawnA(player.getLocation());
                player.sendMessage(CC.translate("&aSuccessfully updated the sumo system's spawn a location."));
                SoupPvP.getInstance().getSumoHandler().save();
                break;
            case "b":
                SoupPvP.getInstance().getSumoHandler().setSpawnB(player.getLocation());
                player.sendMessage(CC.translate("&aSuccessfully updated the sumo system's spawn b location."));
                SoupPvP.getInstance().getSumoHandler().save();
                break;
            case "spec":
                SoupPvP.getInstance().getSumoHandler().setSpectatorSpawn(player.getLocation());
                player.sendMessage(CC.translate("&aSuccessfully updated the sumo system's spectator spawn location."));
                SoupPvP.getInstance().getSumoHandler().save();
                break;
            default:
                player.sendMessage(CC.translate("&cAvailable Positions: a,b,spec"));
                break;
        }
	}

}
