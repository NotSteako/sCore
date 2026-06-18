package kami.gg.souppvp.events.impl.sumo.task;

import kami.gg.souppvp.events.impl.sumo.Sumo;
import kami.gg.souppvp.events.impl.sumo.SumoState;
import kami.gg.souppvp.events.impl.sumo.SumoTask;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.Cooldown;
import kami.gg.souppvp.util.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SumoStartTask extends SumoTask {

	public SumoStartTask(Sumo sumo) {
		super(sumo, SumoState.WAITING);
	}

	@Override
	public void onRun() {
		if (getTicks() >= 120) {
			this.getSumo().end();
			return;
		}

		if (this.getSumo().getPlayers().size() <= 1 && this.getSumo().getCooldown() != null) {
			this.getSumo().setCooldown(null);
			this.getSumo().broadcastMessage("&cThere are not enough players for the Sumo Event to start.");
		}

		if (this.getSumo().getPlayers().size() == this.getSumo().getMaxPlayers() || (getTicks() >= 30 && this.getSumo().getPlayers().size() >= 2)) {
			if (this.getSumo().getCooldown() == null) {
				this.getSumo().setCooldown(new Cooldown(11_000));
				FancyMessage message = new FancyMessage(CC.translate("&7The &bSumo &7Event will start in &b00:10&7! "));
				message.then("[Click Here]").color(ChatColor.GREEN).command("/sumo join").tooltip(ChatColor.GREEN + "Click to join!").then(" (" + this.getSumo().getRemainingPlayers().size() + "/" + this.getSumo().getMaxPlayers() + ")").color(ChatColor.WHITE);
				for (Player player : this.getSumo().getPlayers()) {
					message.send(player);
				}
			} else {
				if (this.getSumo().getCooldown().hasExpired()) {
					this.getSumo().setState(SumoState.ROUND_STARTING);
					this.getSumo().onRound();
					this.getSumo().setTotalPlayers(this.getSumo().getPlayers().size());
					this.getSumo().setEventTask(new SumoRoundStartTask(this.getSumo()));
				}
			}
		}

		if (getTicks() % 10 == 0) {
			this.getSumo().announce();
		}
	}

}
