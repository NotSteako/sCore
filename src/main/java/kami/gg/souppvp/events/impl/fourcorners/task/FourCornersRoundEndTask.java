package kami.gg.souppvp.events.impl.fourcorners.task;

import kami.gg.souppvp.events.impl.fourcorners.FourCorners;
import kami.gg.souppvp.events.impl.fourcorners.FourCornersState;
import kami.gg.souppvp.events.impl.fourcorners.FourCornersTask;

public class FourCornersRoundEndTask extends FourCornersTask {
    public FourCornersRoundEndTask(FourCorners event) { super(event, FourCornersState.ROUND_ENDING); }

    @Override
    public void onRun() {
        if (this.getEvent().canEnd()) this.getEvent().end();
        else if (getTicks() >= 3) this.getEvent().onRound();
    }
}