package kami.gg.souppvp.events.impl.redrover.task;

import kami.gg.souppvp.events.impl.redrover.RedRover;
import kami.gg.souppvp.events.impl.redrover.RedRoverState;
import kami.gg.souppvp.events.impl.redrover.RedRoverTask;

public class RedRoverRoundEndTask extends RedRoverTask {

    public RedRoverRoundEndTask(RedRover redRover) { super(redRover, RedRoverState.ROUND_ENDING); }

    @Override
    public void onRun() {
        if (this.getRedRover().canEnd()) this.getRedRover().end();
        else if (getTicks() >= 3) this.getRedRover().onRound();
    }

}