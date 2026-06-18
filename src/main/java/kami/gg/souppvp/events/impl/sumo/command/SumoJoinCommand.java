package kami.gg.souppvp.events.impl.sumo.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.sumo.Sumo;
import kami.gg.souppvp.events.impl.sumo.SumoState;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class SumoJoinCommand {

	@Command(name = "join", desc = "join sumo event")
	public void execute(@Sender Player player) {
		Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
		Sumo activeSumo = SoupPvP.getInstance().getSumoHandler().getActiveSumo();

		if (!profile.getProfileState().equals(ProfileState.SPAWN)) {
			player.sendMessage(CC.translate("&cYou cannot join the sumo event right now. You need to be at spawn."));
			return;
		}

		if (activeSumo == null) {
			player.sendMessage(CC.translate("&cThere isn't an active sumo event."));
			return;
		}

		if (activeSumo.getState() != SumoState.WAITING) {
			player.sendMessage(CC.translate("&cThat sumo event is currently on-going and cannot be joined."));
			return;
		}

		if (profile.getSumoEvent() != null){
			player.sendMessage(CC.translate("&cYou are already in a sumo event."));
			return;
		}

		SoupPvP.getInstance().getSumoHandler().getActiveSumo().handleJoin(player);
	}

}
