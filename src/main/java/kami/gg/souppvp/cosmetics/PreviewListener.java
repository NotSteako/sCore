package kami.gg.souppvp.cosmetics;

import kami.gg.souppvp.SoupPvP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class PreviewListener implements Listener {

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (event.isSneaking() && PreviewSession.isInPreview(player)) {
            PreviewSession.endPreview(player);
        }
    }

    // Completely freeze the player — cancel all movement including head rotation
//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onMove(PlayerMoveEvent event) {
//        Player player = event.getPlayer();
//
//        PreviewSession session = PreviewSession.getSession(player);
//        if (session == null) {
//            return;
//        }
//
//        Location from = event.getFrom();
//        Location to = event.getTo();
//
//        if (to == null) {
//            return;
//        }
//
//        // Prevent both movement and camera rotation
//        if (from.getX() != to.getX()
//                || from.getY() != to.getY()
//                || from.getZ() != to.getZ()
//                || from.getYaw() != to.getYaw()
//                || from.getPitch() != to.getPitch()) {
//
//            event.setTo(session.getLockedLocation().clone());
//        }
//    }

    // If a new player joins while someone is in preview, hide the previewer from them too
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (PreviewSession.isInPreview(other)) {
                joined.hideEntity(SoupPvP.getInstance(), other);
            }
        }
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (event.getExited() instanceof Player player && PreviewSession.isInPreview(player)) {
            event.setCancelled(true);
            PreviewSession.endPreview(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PreviewSession.endPreview(event.getPlayer());
    }
}