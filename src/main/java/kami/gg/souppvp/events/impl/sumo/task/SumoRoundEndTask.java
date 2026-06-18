package kami.gg.souppvp.events.impl.sumo.task;

import kami.gg.souppvp.events.impl.sumo.Sumo;
import kami.gg.souppvp.events.impl.sumo.SumoState;
import kami.gg.souppvp.events.impl.sumo.SumoTask;

public class SumoRoundEndTask extends SumoTask {

	public SumoRoundEndTask(Sumo sumo) {
		super(sumo, SumoState.ROUND_ENDING);
	}

	@Override
	public void onRun() {
		if (this.getSumo().canEnd()) {
			this.getSumo().end();
		} else {
			if (getTicks() >= 3) {
				this.getSumo().onRound();
			}
		}
	}

}
