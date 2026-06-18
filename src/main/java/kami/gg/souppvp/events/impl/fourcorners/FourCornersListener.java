package kami.gg.souppvp.events.impl.fourcorners;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.SpectatorUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class FourCornersListener implements Listener {

    @EventHandler
    public void onPlayerMoveEventWater(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (player.getGameMode().equals(GameMode.CREATIVE)) return;
        FourCorners ev = profile.getFourCornersEvent();
        if (ev == null) return;
        Material material = player.getLocation().getBlock().getType();
        if (material == Material.WATER || material == Material.WATER) {
            // Lightning effect where the player hit the water
            if (player.getWorld() != null) {
                player.getWorld().strikeLightningEffect(player.getLocation());
            }
            SpectatorUtil.resetPlayer(player);
            player.teleport(SoupPvP.getInstance().getFourCornersHandler().getSpectatorSpawn());
            if (ev.isAlive(player.getUniqueId())) {
                ev.handleDeath(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getPlayer().getUniqueId());
        if (profile.getFourCornersEvent() != null) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        FourCorners ev = profile.getFourCornersEvent();
        if (ev == null) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID || event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
            event.setCancelled(true);
            event.getEntity().setFireTicks(0);
            SpectatorUtil.resetPlayer(player);
            player.teleport(SoupPvP.getInstance().getFourCornersHandler().getSpectatorSpawn());
            if (ev.isAlive(player.getUniqueId())) ev.handleDeath(player);
            return;
        }
        event.setCancelled(true); // no PvP / no damage during 4Corners
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Player attacker = null;
        if (event.getDamager() instanceof Player) attacker = (Player) event.getDamager();
        else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player)
            attacker = (Player) ((Projectile) event.getDamager()).getShooter();
        if (attacker != null && event.getEntity() instanceof Player) {
            Player damaged = (Player) event.getEntity();
            Profile dp = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(damaged.getUniqueId());
            Profile ap = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(attacker.getUniqueId());
            if (dp.getFourCornersEvent() != null || ap.getFourCornersEvent() != null) event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getPlayer().getUniqueId());
        if (profile.getFourCornersEvent() != null) profile.getFourCornersEvent().handleLeave(event.getPlayer());
    }
}