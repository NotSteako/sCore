package kami.gg.souppvp.listener;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.handlers.NoCollideHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class NoCollideListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        // Defer a few ticks so Assemble has already swapped the
        // player onto their per-player scoreboard.
        Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(),
                NoCollideHandler::refreshAll, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        NoCollideHandler.removeEverywhere(event.getPlayer());
    }
}