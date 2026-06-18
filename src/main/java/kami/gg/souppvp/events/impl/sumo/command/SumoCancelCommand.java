package kami.gg.souppvp.events.impl.sumo.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import org.bukkit.command.CommandSender;

public class SumoCancelCommand {

	@Command(name = "cancel", desc = "cancel sumo event")
	@Require("souppvp.sumocancel")
	public void execute(@Sender CommandSender sender) {
		if (SoupPvP.getInstance().getSumoHandler().getActiveSumo() == null) {
			sender.sendMessage(CC.translate("&cThere isn't an active sumo event."));
			return;
		}
		SoupPvP.getInstance().getSumoHandler().getActiveSumo().end();
	}

}
