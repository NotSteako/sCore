package kami.gg.souppvp.events.impl.fourcorners;

import kami.gg.souppvp.SoupPvP;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public abstract class FourCornersTask extends BukkitRunnable {

    private int ticks;
    private FourCorners event;
    private FourCornersState eventState;

    public FourCornersTask(FourCorners event, FourCornersState eventState) {
        this.event = event;
        this.eventState = eventState;
    }

    @Override
    public void run() {
        if (SoupPvP.getInstance().getFourCornersHandler().getActiveEvent() == null ||
                !SoupPvP.getInstance().getFourCornersHandler().getActiveEvent().equals(event) ||
                event.getState() != eventState) {
            cancel();
            return;
        }
        onRun();
        ticks++;
    }

    public int getSeconds() { return 3 - ticks; }

    public abstract void onRun();
}