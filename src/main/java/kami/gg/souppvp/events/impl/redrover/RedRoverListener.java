package kami.gg.souppvp.events.impl.redrover;

import kami.gg.souppvp.SoupPvP;
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

public class RedRoverListener implements Listener {

    private RedRover activeOf(Player player) {
        RedRover rr = SoupPvP.getInstance().getRedRoverHandler().getActiveRedRover();
        if (rr == null) return null;
        if (!rr.getEventPlayers().containsKey(player.getUniqueId()) && !rr.getSpectators().contains(player.getUniqueId())) return null;
        return rr;
    }

    @EventHandler
    public void onPlayerMoveEventWater(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode().equals(GameMode.CREATIVE)) return;
        RedRover rr = activeOf(player);
        if (rr == null) return;
        if (!rr.getEventPlayers().containsKey(player.getUniqueId())) return;
        Material material = player.getLocation().getBlock().getType();
        if (material == Material.WATER || material == Material.WATER) {
            if (rr.isFighting(player.getUniqueId())) {
                SpectatorUtil.resetPlayer(player);
                player.teleport(SoupPvP.getInstance().getRedRoverHandler().getSpectatorSpawn());
                rr.handleDeath(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        RedRover rr = activeOf(event.getPlayer());
        if (rr == null) return;
        if (!rr.isFighting(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        RedRover rr = activeOf(player);
        if (rr == null) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.VOID || event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
            event.setCancelled(true);
            event.getEntity().setFireTicks(0);
            if (!rr.isFighting() || !rr.isFighting(player.getUniqueId())) {
                player.teleport(SoupPvP.getInstance().getRedRoverHandler().getSpectatorSpawn());
                return;
            }
            SpectatorUtil.resetPlayer(player);
            player.teleport(SoupPvP.getInstance().getRedRoverHandler().getSpectatorSpawn());
            rr.handleDeath(player);
            return;
        }

        if (!rr.isFighting() || !rr.isFighting(player.getUniqueId())) { event.setCancelled(true); return; }
        event.setDamage(0);
        player.setHealth(20.0);
        player.updateInventory();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Player attacker = null;
        if (event.getDamager() instanceof Player) attacker = (Player) event.getDamager();
        else if (event.getDamager() instanceof Projectile) {
            if (((Projectile) event.getDamager()).getShooter() instanceof Player)
                attacker = (Player) ((Projectile) event.getDamager()).getShooter();
        }

        if (attacker != null && event.getEntity() instanceof Player) {
            Player damaged = (Player) event.getEntity();
            RedRover rr = SoupPvP.getInstance().getRedRoverHandler().getActiveRedRover();
            if (rr == null) return;
            boolean dInEvent = rr.getEventPlayers().containsKey(damaged.getUniqueId());
            boolean aInEvent = rr.getEventPlayers().containsKey(attacker.getUniqueId());
            if (dInEvent && aInEvent) {
                if (!rr.isFighting() || !rr.isFighting(damaged.getUniqueId()) || !rr.isFighting(attacker.getUniqueId())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        RedRover rr = SoupPvP.getInstance().getRedRoverHandler().getActiveRedRover();
        if (rr == null) return;
        if (rr.getEventPlayers().containsKey(event.getPlayer().getUniqueId())) rr.handleLeave(event.getPlayer());
    }

}