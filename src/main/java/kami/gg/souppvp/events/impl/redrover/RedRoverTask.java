package kami.gg.souppvp.events.impl.redrover;

import kami.gg.souppvp.SoupPvP;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public abstract class RedRoverTask extends BukkitRunnable {

    private int ticks;
    private RedRover redRover;
    private RedRoverState eventState;

    public RedRoverTask(RedRover redRover, RedRoverState eventState) {
        this.redRover = redRover;
        this.eventState = eventState;
    }

    @Override
    public void run() {
        if (SoupPvP.getInstance().getRedRoverHandler().getActiveRedRover() == null ||
                !SoupPvP.getInstance().getRedRoverHandler().getActiveRedRover().equals(redRover) ||
                redRover.getState() != eventState) {
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