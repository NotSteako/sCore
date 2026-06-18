package kami.gg.souppvp.events.impl.redrover.task;

import kami.gg.souppvp.events.impl.redrover.RedRover;
import kami.gg.souppvp.events.impl.redrover.RedRoverState;
import kami.gg.souppvp.events.impl.redrover.RedRoverTask;

public class RedRoverDraftTask extends RedRoverTask {

    private static final int PICK_TIMEOUT_SECONDS = 20;

    public RedRoverDraftTask(RedRover redRover) { super(redRover, RedRoverState.DRAFTING); }

    @Override
    public void onRun() {
        int remaining = PICK_TIMEOUT_SECONDS - getTicks();
        if (remaining == 10 || remaining == 5 || (remaining > 0 && remaining <= 3)) {
            this.getRedRover().broadcastMessage("&7Captain has &c" + remaining + "s &7to pick...");
        }
        if (getTicks() >= PICK_TIMEOUT_SECONDS) {
            this.getRedRover().broadcastMessage("&7Captain took too long. Auto-picking a player...");
            this.getRedRover().autoPickRandom();
        }
    }

}