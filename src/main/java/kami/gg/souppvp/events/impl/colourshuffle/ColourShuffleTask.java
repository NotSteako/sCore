package kami.gg.souppvp.events.impl.colourshuffle;

import kami.gg.souppvp.SoupPvP;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public abstract class ColourShuffleTask extends BukkitRunnable {

    private int ticks;
    private final ColourShuffle event;
    private final ColourShuffleState requiredState;

    public ColourShuffleTask(ColourShuffle event, ColourShuffleState requiredState) {
        this.event = event;
        this.requiredState = requiredState;
    }

    @Override
    public void run() {
        if (SoupPvP.getInstance().getColourShuffleHandler().getActiveEvent() == null
                || !SoupPvP.getInstance().getColourShuffleHandler().getActiveEvent().equals(event)
                || event.getState() != requiredState) {
            cancel();
            return;
        }
        onRun();
        ticks++;
    }

    public abstract void onRun();
}
