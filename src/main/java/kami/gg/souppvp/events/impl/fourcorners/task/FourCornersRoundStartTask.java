package kami.gg.souppvp.events.impl.fourcorners.task;

import kami.gg.souppvp.events.impl.fourcorners.FourCorners;
import kami.gg.souppvp.events.impl.fourcorners.FourCornersState;
import kami.gg.souppvp.events.impl.fourcorners.FourCornersTask;
import kami.gg.souppvp.util.CC;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class FourCornersRoundStartTask extends FourCornersTask {

    private static final int PICK_SECONDS = 10; // 1 tick = 1 sec (20L period in parent)

    public FourCornersRoundStartTask(FourCorners event) { super(event, FourCornersState.ROUND_STARTING); }

    @Override
    public void onRun() {
        int ticks = getTicks();
        int remaining = PICK_SECONDS - ticks;

        if (remaining <= 0) {
            this.getEvent().broadcastMessage(CC.translate("&cBridges dropping!"));
            for (Player p : this.getEvent().getRemainingPlayers()) p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
            this.getEvent().setRoundStart(System.currentTimeMillis());
            this.getEvent().setState(FourCornersState.ROUND_FIGHTING);
            this.getEvent().setEventTask(null);
            this.getEvent().onBridgesDrop();
            return;
        }
        if (remaining == 10 || remaining == 5 || remaining == 3 || remaining == 2 || remaining == 1) {
            this.getEvent().broadcastMessage("&7[Round &b" + this.getEvent().getRoundNumber() + "&7] Bridges dropping in &b" + remaining + " &7seconds.");
            for (Player p : this.getEvent().getRemainingPlayers()) p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
        }
    }
}