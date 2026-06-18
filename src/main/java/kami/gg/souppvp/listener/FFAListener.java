package kami.gg.souppvp.listener;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class FFAListener implements Listener {

    private static final double SAFE_RADIUS = 2.0;
    private static final double SAFE_RADIUS_SQUARED = SAFE_RADIUS * SAFE_RADIUS;

    private static final Location FFA_SPAWN =
            new Location(Bukkit.getWorld("world"), 1200, 103, 1200);

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        Profile profile = SoupPvP.getInstance()
                .getProfilesHandler()
                .getProfileByUUID(attacker.getUniqueId());

        if (profile != null) {
            profile.setProfileState(ProfileState.COMBAT);
        }

        boolean victimSafe = isInSafeZone(victim);
        boolean attackerSafe = isInSafeZone(attacker);

        if (victimSafe || attackerSafe) {
            event.setCancelled(true);
        }
    }

    private boolean isInSafeZone(Player player) {
        World spawnWorld = FFA_SPAWN.getWorld();

        if (spawnWorld == null) {
            return false;
        }

        if (!player.getWorld().equals(spawnWorld)) {
            return false;
        }

        return player.getLocation().distanceSquared(FFA_SPAWN)
                <= SAFE_RADIUS_SQUARED;
    }
}