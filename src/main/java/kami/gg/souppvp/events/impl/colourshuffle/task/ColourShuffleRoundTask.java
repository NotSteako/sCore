package kami.gg.souppvp.events.impl.colourshuffle.task;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.colourshuffle.ColourShuffle;
import kami.gg.souppvp.events.impl.colourshuffle.ColourShuffleState;
import kami.gg.souppvp.events.impl.colourshuffle.ColourShuffleTask;
import kami.gg.souppvp.events.impl.colourshuffle.player.ColourShufflePlayerState;
import kami.gg.souppvp.util.CC;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Manages a single round of Colour Shuffle.
 *  - On tick 0: round announced (already done by {@link ColourShuffle#onRound()}).
 *  - On tick (timeBudget - 1): drop every wool that isn't the safe colour.
 *  - On tick timeBudget: detect players still on a non-safe block, restore floor, start next round.
 *
 * Time budget shrinks each round (starts at 6 seconds, floor of 2 seconds).
 */
public class ColourShuffleRoundTask extends ColourShuffleTask {

    public ColourShuffleRoundTask(ColourShuffle event) {
        super(event, ColourShuffleState.ROUND_STARTING);
    }

    private int budget() {
        return Math.max(2, 6 - (getEvent().getCurrentRound() - 1));
    }

    @Override
    public void onRun() {
        ColourShuffle ev = getEvent();
        int budget = budget();

        if (ev.getState() == ColourShuffleState.ROUND_STARTING && getTicks() <= budget) {
            // Show countdown to players.
            int secondsLeft = budget - getTicks();
            for (Player p : ev.getRemainingPlayers()) {
                p.sendActionBar(CC.translate("&7Round &d" + ev.getCurrentRound()
                        + " &7• Safe: &r" + ColourShuffle.colourName(ev.getSafeColour())
                        + " &7• &d" + secondsLeft + "s"));
                try { p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1.0F); } catch (Throwable ignored) {}
            }
            if (getTicks() == budget) {
                // Drop the floor!
                ev.setState(ColourShuffleState.ROUND_RUNNING);
                ev.setRoundStart(System.currentTimeMillis());
                SoupPvP.getInstance().getColourShuffleHandler().dropNonSafeBlocks(ev.getSafeColour());
                ev.broadcastMessage("&cThe floor has dropped!");
                cancel();
                // Schedule the round-end check 1.5s after the drop so players have time to fall into the void.
                org.bukkit.Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
                    eliminateFallers(ev);
                    if (ev.canEnd()) {
                        ev.end();
                    } else {
                        // Survivor(s) — start next round.
                        for (Player p : ev.getRemainingPlayers()) {
                            ColourShufflePlayer.markSurvived(ev, p);
                        }
                        ev.onRound();
                    }
                }, 30L);
            }
        }
    }

    private static void eliminateFallers(ColourShuffle ev) {
        for (Player p : ev.getRemainingPlayers()) {
            Material below = p.getLocation().clone().subtract(0, 1, 0).getBlock().getType();
            if (below == Material.AIR || below.name().endsWith("_WOOL") && below != ev.getSafeColour()) {
                ev.handleElimination(p);
            } else if (p.getLocation().getY() < SoupPvP.getInstance().getColourShuffleHandler().getFloorA().getY() - 10) {
                ev.handleElimination(p);
            }
        }
    }

    /** Tiny helper to avoid pulling player class into this file just for one increment. */
    static class ColourShufflePlayer {
        static void markSurvived(ColourShuffle ev, Player p) {
            kami.gg.souppvp.events.impl.colourshuffle.player.ColourShufflePlayer csp = ev.getEventPlayer(p);
            if (csp != null && csp.getState() == ColourShufflePlayerState.WAITING) {
                csp.setRoundsSurvived(csp.getRoundsSurvived() + 1);
            }
        }
    }
}
