package kami.gg.souppvp.listener;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.PlayerUtil;
import kami.gg.souppvp.util.TaskUtil;
import kami.gg.souppvp.util.TasksUtility;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerListeners implements Listener {

    private final Set<UUID> forceInvis = new HashSet<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(final PlayerLoginEvent event) {
        final Player player = event.getPlayer();

        player.setCollidable(false);
        if (player.hasPermission("modsuite.staff")) {
            this.forceInvis.add(player.getUniqueId());
        }


    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerUtil.resetPlayer(event.getPlayer());

        TaskUtil.runLater(() -> {
            LunarClientListener.updateNametag(event.getPlayer());
        }, 20L);
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (event.getTo() == null) {
            return;
        }

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Profile profile = SoupPvP.getInstance()
                .getProfilesHandler()
                .getProfileByUUID(player.getUniqueId());

        if (profile == null) {
            return;
        }

        // Only trigger when leaving spawn while in SPAWN state
        if (profile.getProfileState() != ProfileState.SPAWN) {
            return;
        }

        if (SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player)) {
            return;
        }

        player.sendMessage(CC.translate("&7You no longer have spawn protection!"));

        profile.setProfileState(ProfileState.COMBAT);
        LunarClientListener.updateNametag(player);

        TaskUtil.runLater(() -> {
            if (player.hasMetadata("noFall")) {
                player.removeMetadata("noFall", SoupPvP.getInstance());
            }
        }, 80L);

        if (profile.isJuggernaut()) {
            return;
        }

        if (profile.getCurrentKit() == null) {
            player.sendMessage(CC.translate("&cNo kit selected."));
            return;
        }

        Kit kit = SoupPvP.getInstance()
                .getKitsHandler()
                .getKitByName(profile.getCurrentKit().getName());

        if (kit != null) {
            kit.equipKit(player);
        } else {
            player.sendMessage(CC.translate("&cFailed to load your selected kit."));
        }

        LunarClientListener.updateNametag(player);
    }
    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event){
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getEntity().hasMetadata("noFall") && event.getCause().equals(EntityDamageEvent.DamageCause.FALL)){
            event.setCancelled(true);
            event.getEntity().removeMetadata("noFall", SoupPvP.getInstance());
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerOnSpongeEvent(PlayerMoveEvent event){
        if (!event.getPlayer().getGameMode().equals(GameMode.CREATIVE)){
            Player player = event.getPlayer();
            if (event.getTo().getBlockX() == event.getFrom().getBlockX() && event.getTo().getBlockY() == event.getFrom().getBlockY() && event.getTo().getBlockZ() == event.getFrom().getBlockZ()) return;
            if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SPONGE){
                if (player.hasMetadata("jammed")) return;
                TasksUtility.runTaskLater(() -> {
                    Vector vector = player.getVelocity().setY(+2.5);
                    player.setVelocity(vector);
                    PlayerUtil.playSound(player, Sound.ENTITY_CHICKEN_EGG);
                }, 2L);
            }
        }
    }

    @EventHandler
    public void onDenyMovement(PlayerMoveEvent event){
        if (!event.getPlayer().getGameMode().equals(GameMode.CREATIVE)){
            if (event.getTo().getBlockX() == event.getFrom().getBlockX() && event.getTo().getBlockY() == event.getFrom().getBlockY() && event.getTo().getBlockZ() == event.getFrom().getBlockZ()) return;
            if (event.getPlayer().hasMetadata("denyMovement")){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        // Remove advancement announcements
        event.message(null); // Paper
    }

}
