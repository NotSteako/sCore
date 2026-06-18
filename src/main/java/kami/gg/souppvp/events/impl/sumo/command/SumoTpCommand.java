package kami.gg.souppvp.events.impl.sumo.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class SumoTpCommand {

	@Command(name = "tp", desc = "tp to sumo event")
	@Require("souppvp.sumotp")
	public void execute(@Sender Player player) {
		player.teleport(SoupPvP.getInstance().getSumoHandler().getSpectatorSpawn());
		player.sendMessage(CC.translate("&aSuccessfully teleported to the sumo system's spectator spawn location."));
	}

}
