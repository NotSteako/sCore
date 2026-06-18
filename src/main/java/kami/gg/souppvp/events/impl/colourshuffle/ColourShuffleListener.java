package kami.gg.souppvp.events.impl.colourshuffle;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.SpectatorUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ColourShuffleListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (profile == null || profile.getColourShuffleEvent() == null) return;
        ColourShuffle ev = profile.getColourShuffleEvent();

        // Void damage = elimination (falling through the dropped floor).
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID
                || event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
            player.setFireTicks(0);
            if (ev.isRunning()) {
                SpectatorUtil.resetPlayer(player);
                ev.handleElimination(player);
            } else {
                player.teleport(SoupPvP.getInstance().getColourShuffleHandler().getSpectatorSpawn());
            }
            return;
        }

        // No PvP damage during Colour Shuffle.
        event.setCancelled(true);
        event.setDamage(0);
        if (player.getGameMode() != GameMode.SPECTATOR) player.setHealth(20.0);
        player.updateInventory();
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Profile profile = SoupPvP.getInstance()
                .getProfilesHandler()
                .getProfileByUUID(player.getUniqueId());

        if (profile == null || profile.getColourShuffleEvent() == null) {
            return;
        }

        ColourShuffle colourShuffle = profile.getColourShuffleEvent();

        if (!colourShuffle.isRunning()) {
            return;
        }

        if (player.isInWater()) {
            SpectatorUtil.resetPlayer(player);
            colourShuffle.handleElimination(player);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPvP(EntityDamageByEntityEvent event) {
        Player attacker = null;
        if (event.getDamager() instanceof Player) attacker = (Player) event.getDamager();
        else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
            attacker = (Player) ((Projectile) event.getDamager()).getShooter();
        }
        if (attacker == null || !(event.getEntity() instanceof Player)) return;
        Profile a = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(attacker.getUniqueId());
        Profile b = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getEntity().getUniqueId());
        if ((a != null && a.getColourShuffleEvent() != null) || (b != null && b.getColourShuffleEvent() != null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getPlayer().getUniqueId());
        if (profile != null && profile.getColourShuffleEvent() != null) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getPlayer().getUniqueId());
        if (profile != null && profile.getColourShuffleEvent() != null) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getPlayer().getUniqueId());
        if (profile != null && profile.getColourShuffleEvent() != null) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent event) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getPlayer().getUniqueId());
        if (profile != null && profile.getColourShuffleEvent() != null) {
            profile.getColourShuffleEvent().handleLeave(event.getPlayer());
        }
    }
}
