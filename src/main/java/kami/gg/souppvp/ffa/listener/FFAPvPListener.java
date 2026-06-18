package kami.gg.souppvp.ffa.listener;

import kami.gg.souppvp.ffa.FFACommand;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class FFAPvPListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (!(event.getDamager() instanceof Player)) {
            return;
        }


        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        if (FFACommand.isInSafeZone(victim.getLocation())
                || FFACommand.isInSafeZone(attacker.getLocation())) {
            event.setCancelled(true);
            attacker.sendMessage("§cYou cannot fight in the FFA safezone.");
        }
    }
}