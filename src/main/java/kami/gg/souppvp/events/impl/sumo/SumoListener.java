package kami.gg.souppvp.events.impl.sumo;

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

public class SumoListener implements Listener {

	@EventHandler
	public void onPlayerMoveEventWater(PlayerMoveEvent event){
		Player player = event.getPlayer();
		Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
		if (!player.getGameMode().equals(GameMode.CREATIVE)){
			if (profile.getSumoEvent() != null){
				Material material = player.getLocation().getBlock().getType();
				if (material == Material.WATER || material == Material.WATER) {
					SpectatorUtil.resetPlayer(player);
					player.teleport(SoupPvP.getInstance().getSumoHandler().getSpectatorSpawn());
					profile.getSumoEvent().handleDeath(player);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
		Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getPlayer().getUniqueId());
		if (profile.getSumoEvent() != null) {
			if (!profile.getSumoEvent().isFighting(event.getPlayer().getUniqueId())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
			if (profile.getSumoEvent() != null) {
				if (event.getCause() == EntityDamageEvent.DamageCause.VOID || event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
					event.setCancelled(true);
					event.getEntity().setFireTicks(0);
					if (!profile.getSumoEvent().isFighting() || !profile.getSumoEvent().isFighting(player.getUniqueId())) {
						player.teleport(SoupPvP.getInstance().getSumoHandler().getSpectatorSpawn());
						return;
					}
					SpectatorUtil.resetPlayer(player);
					player.teleport(SoupPvP.getInstance().getSumoHandler().getSpectatorSpawn());
					profile.getSumoEvent().handleDeath(player);
					return;
				}
				if (profile.getSumoEvent() != null) {
					if (!profile.getSumoEvent().isFighting() || !profile.getSumoEvent().isFighting(player.getUniqueId())) {
						event.setCancelled(true);
						return;
					}
					event.setDamage(0);
					player.setHealth(20.0);
					player.updateInventory();
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Player attacker = null;
		if (event.getDamager() instanceof Player) {
			attacker = (Player) event.getDamager();
		} else if (event.getDamager() instanceof Projectile) {
			if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
				attacker = (Player) ((Projectile) event.getDamager()).getShooter();
			}
		} 

		if (attacker != null && event.getEntity() instanceof Player) {
			Player damaged = (Player) event.getEntity();
			Profile damagedProfile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(damaged.getUniqueId());
			Profile attackerProfile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(attacker.getUniqueId());
			if (damagedProfile.getSumoEvent() != null && attackerProfile.getSumoEvent() != null) {
				Sumo sumo = damagedProfile.getSumoEvent();
				if (!sumo.isFighting() || !sumo.isFighting(damaged.getUniqueId()) ||
						!sumo.isFighting(attacker.getUniqueId())) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getPlayer().getUniqueId());
		if (profile.getSumoEvent() != null) {
			profile.getSumoEvent().handleLeave(event.getPlayer());
		}
	}

}
