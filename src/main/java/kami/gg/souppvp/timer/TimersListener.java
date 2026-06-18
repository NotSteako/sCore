package kami.gg.souppvp.timer;

import kami.gg.souppvp.SoupPvP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author hieu
 * @date 24/06/2023
 */

public class TimersListener implements Listener {

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event){
        if (SoupPvP.getInstance().getTimersHandler().containsInHashMapPlayer(event.getPlayer().getUniqueId())){
            SoupPvP.getInstance().getTimersHandler().removeAllPlayerTimers(event.getPlayer().getUniqueId());
        }
    }

}
