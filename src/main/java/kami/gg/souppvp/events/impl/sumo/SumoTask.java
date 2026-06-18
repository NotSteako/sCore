package kami.gg.souppvp.events.impl.sumo;

import kami.gg.souppvp.SoupPvP;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public abstract class SumoTask extends BukkitRunnable {

	private int ticks;
	private Sumo sumo;
	private SumoState eventState;

	public SumoTask(Sumo sumo, SumoState eventState) {
		this.sumo = sumo;
		this.eventState = eventState;
	}

	@Override
	public void run() {
		if (SoupPvP.getInstance().getSumoHandler().getActiveSumo() == null ||
		    !SoupPvP.getInstance().getSumoHandler().getActiveSumo().equals(sumo) || sumo.getState() != eventState) {
			cancel();
			return;
		}

		onRun();

		ticks++;
	}

	public int getSeconds() {
		return 3 - ticks;
	}

	public abstract void onRun();

}
