package kami.gg.souppvp.events.impl.sumo.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.sumo.Sumo;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class SumoLeaveCommand {

	@Command(name = "leave", desc = "leave sumo event")
	public void execute(@Sender Player player) {
		Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
		Sumo activeSumo = SoupPvP.getInstance().getSumoHandler().getActiveSumo();

		if (activeSumo == null) {
			player.sendMessage(CC.translate("&cThere isn't an active sumo event."));
			return;
		}

		if (profile.getSumoEvent() == null || !activeSumo.getEventPlayers().containsKey(player.getUniqueId())) {
			player.sendMessage(CC.translate("&cYou are not apart of the active sumo event."));
			return;
		}

		SoupPvP.getInstance().getSumoHandler().getActiveSumo().handleLeave(player);
	}

}
