package kami.gg.souppvp.events.impl.sumo.task;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.sumo.Sumo;
import kami.gg.souppvp.events.impl.sumo.SumoState;
import kami.gg.souppvp.events.impl.sumo.SumoTask;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.PlayerUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SumoRoundStartTask extends SumoTask {

	public SumoRoundStartTask(Sumo sumo) {
		super(sumo, SumoState.ROUND_STARTING);
	}

	@Override
	public void onRun() {
		if (getTicks() >= 3) {
			this.getSumo().broadcastMessage(CC.translate("&bMatch Started!"));
			this.getSumo().setEventTask(null);
			this.getSumo().setState(SumoState.ROUND_FIGHTING);

			Player playerA = this.getSumo().getRoundPlayerA().getPlayer();
			Player playerB = this.getSumo().getRoundPlayerB().getPlayer();

			Location spawnA = SoupPvP.getInstance().getSumoHandler().getSpawnA();
			Location spawnB = SoupPvP.getInstance().getSumoHandler().getSpawnB();

			playerA.teleport(spawnA);
			playerB.teleport(spawnB);

			playerA.playSound(playerA.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
			PlayerUtil.allowMovement(playerA);

			playerB.playSound(playerB.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
			PlayerUtil.allowMovement(playerB);

			((Sumo) this.getSumo()).setRoundStart(System.currentTimeMillis());
		} else {
			int seconds = getSeconds();
			Player playerA = this.getSumo().getRoundPlayerA().getPlayer();
			Player playerB = this.getSumo().getRoundPlayerB().getPlayer();

			Location spawnA = SoupPvP.getInstance().getSumoHandler().getSpawnA();
			Location spawnB = SoupPvP.getInstance().getSumoHandler().getSpawnB();

			playerA.teleport(spawnA);
			playerB.teleport(spawnB);

			playerA.playSound(playerA.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
			PlayerUtil.denyMovement(playerA);

			playerB.playSound(playerB.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
			PlayerUtil.denyMovement(playerB);

			this.getSumo().broadcastMessage("&7The round will be starting in &b" + seconds + "&7...");
		}
	}

}
