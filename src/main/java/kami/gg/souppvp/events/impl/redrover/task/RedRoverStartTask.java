package kami.gg.souppvp.events.impl.redrover.task;

import kami.gg.souppvp.events.impl.redrover.RedRover;
import kami.gg.souppvp.events.impl.redrover.RedRoverState;
import kami.gg.souppvp.events.impl.redrover.RedRoverTask;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.Cooldown;
import kami.gg.souppvp.util.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RedRoverStartTask extends RedRoverTask {

    public RedRoverStartTask(RedRover redRover) { super(redRover, RedRoverState.WAITING); }

    @Override
    public void onRun() {
        if (getTicks() >= 120) { this.getRedRover().end(); return; }

        if (this.getRedRover().getPlayers().size() <= 1 && this.getRedRover().getCooldown() != null) {
            this.getRedRover().setCooldown(null);
            this.getRedRover().broadcastMessage("&cThere are not enough players for the Red Rover Event to start.");
        }

        if (this.getRedRover().getPlayers().size() == this.getRedRover().getMaxPlayers()
                || (getTicks() >= 30 && this.getRedRover().getPlayers().size() >= 2)) {
            if (this.getRedRover().getCooldown() == null) {
                this.getRedRover().setCooldown(new Cooldown(11_000));
                FancyMessage message = new FancyMessage(CC.translate("&7The &cRed Rover &7Event will start in &c00:10&7! "));
                message.then("[Click Here]").color(ChatColor.GREEN).command("/redrover join")
                        .tooltip(ChatColor.GREEN + "Click to join!")
                        .then(" (" + this.getRedRover().getRemainingPlayers().size() + "/" + this.getRedRover().getMaxPlayers() + ")")
                        .color(ChatColor.WHITE);
                for (Player player : this.getRedRover().getPlayers()) message.send(player);
            } else if (this.getRedRover().getCooldown().hasExpired()) {
                this.getRedRover().setEventTask(null);
                this.getRedRover().startDraft();
            }
        }

        if (getTicks() % 10 == 0) this.getRedRover().announce();
    }

}