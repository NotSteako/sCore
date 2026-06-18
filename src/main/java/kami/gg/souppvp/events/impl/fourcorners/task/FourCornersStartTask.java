package kami.gg.souppvp.events.impl.fourcorners.task;

import kami.gg.souppvp.events.impl.fourcorners.FourCorners;
import kami.gg.souppvp.events.impl.fourcorners.FourCornersState;
import kami.gg.souppvp.events.impl.fourcorners.FourCornersTask;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.Cooldown;
import kami.gg.souppvp.util.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class FourCornersStartTask extends FourCornersTask {

    public FourCornersStartTask(FourCorners event) { super(event, FourCornersState.WAITING); }

    @Override
    public void onRun() {
        if (getTicks() >= 120) { this.getEvent().end(); return; }

        if (this.getEvent().getPlayers().size() <= 1 && this.getEvent().getCooldown() != null) {
            this.getEvent().setCooldown(null);
            this.getEvent().broadcastMessage("&cThere are not enough players for the 4Corners Event to start.");
        }

        if (this.getEvent().getPlayers().size() == this.getEvent().getMaxPlayers() ||
                (getTicks() >= 30 && this.getEvent().getPlayers().size() >= 2)) {
            if (this.getEvent().getCooldown() == null) {
                this.getEvent().setCooldown(new Cooldown(11_000));
                FancyMessage message = new FancyMessage(CC.translate("&7The &b4Corners &7Event will start in &b00:10&7! "));
                message.then("[Click Here]").color(ChatColor.GREEN).command("/fourcorners join")
                        .tooltip(ChatColor.GREEN + "Click to join!")
                        .then(" (" + this.getEvent().getRemainingPlayers().size() + "/" + this.getEvent().getMaxPlayers() + ")")
                        .color(ChatColor.WHITE);
                for (Player p : this.getEvent().getPlayers()) message.send(p);
            } else if (this.getEvent().getCooldown().hasExpired()) {
                this.getEvent().setState(FourCornersState.ROUND_STARTING);
                this.getEvent().setTotalPlayers(this.getEvent().getPlayers().size());
                this.getEvent().onRound();
            }
        }
        if (getTicks() % 10 == 0) this.getEvent().announce();
    }
}
