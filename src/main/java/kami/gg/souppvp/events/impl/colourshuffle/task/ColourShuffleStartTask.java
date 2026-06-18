package kami.gg.souppvp.events.impl.colourshuffle.task;

import kami.gg.souppvp.events.impl.colourshuffle.ColourShuffle;
import kami.gg.souppvp.events.impl.colourshuffle.ColourShuffleState;
import kami.gg.souppvp.events.impl.colourshuffle.ColourShuffleTask;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.Cooldown;
import kami.gg.souppvp.util.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ColourShuffleStartTask extends ColourShuffleTask {

    public ColourShuffleStartTask(ColourShuffle event) {
        super(event, ColourShuffleState.WAITING);
    }

    @Override
    public void onRun() {
        // No one to play with — cancel the event after 2 minutes of waiting.
        if (getTicks() >= 120) {
            getEvent().end();
            return;
        }

        // Not enough players — clear the countdown.
        if (getEvent().getPlayers().size() <= 1 && getEvent().getCooldown() != null) {
            getEvent().setCooldown(null);
            getEvent().broadcastMessage("&cThere are not enough players for the Colour Shuffle Event to start.");
        }

        // Start the countdown once we have at least 2 players + 30s of waiting OR we hit max players.
        if (getEvent().getPlayers().size() == getEvent().getMaxPlayers()
                || (getTicks() >= 30 && getEvent().getPlayers().size() >= 2)) {

            if (getEvent().getCooldown() == null) {
                getEvent().setCooldown(new Cooldown(11_000L));
                FancyMessage message = new FancyMessage(CC.translate("&7The &dColour Shuffle &7Event will start in &d00:10&7! "));
                message.then("[Click Here]").color(ChatColor.GREEN).command("/colourshuffle join")
                        .tooltip(ChatColor.GREEN + "Click to join!")
                        .then(" (" + getEvent().getRemainingPlayers().size() + "/" + getEvent().getMaxPlayers() + ")")
                        .color(ChatColor.WHITE);
                for (Player player : getEvent().getPlayers()) message.send(player);
            } else if (getEvent().getCooldown().hasExpired()) {
                getEvent().setState(ColourShuffleState.ROUND_STARTING);
                getEvent().setTotalPlayers(getEvent().getPlayers().size());
                getEvent().onRound();
            }
        }

        if (getTicks() % 10 == 0) {
            getEvent().announce();
        }
    }
}
