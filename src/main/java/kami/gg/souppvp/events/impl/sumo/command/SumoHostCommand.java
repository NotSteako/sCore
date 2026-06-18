package kami.gg.souppvp.events.impl.sumo.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.sumo.Sumo;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SumoHostCommand {

	@Command(name = "host", desc = "host sumo event")
	@Require("souppvp.sumohost")
	public void execute(@Sender Player player) {
		if (SoupPvP.getInstance().getSumoHandler().getActiveSumo() != null) {
			player.sendMessage(ChatColor.RED + "There is already an active event.");
			return;
		}

		SoupPvP.getInstance().getSumoHandler().setActiveSumo(new Sumo(player));
		SoupPvP.getInstance().getSumoHandler().getActiveSumo().handleJoin(player);
	}

}
