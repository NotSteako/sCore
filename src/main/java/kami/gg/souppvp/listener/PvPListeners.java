package kami.gg.souppvp.listener;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.PlayerUtil;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.refinedev.phoenix.Phoenix;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class PvPListeners implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
            if (profile.isTeleportingToSpawn()) {
                profile.removeSpawnTeleportation();
                player.sendMessage(CC.translate("&cYour spawn teleportation has been cancelled as you have been combat-tagged."));
            }

            if (profile.getProfileState() == ProfileState.IN_1V1_LOBBY) {
                event.setCancelled(true);
                return;
            }

            if (profile.getProfileState() == ProfileState.SPAWN) {
                event.setCancelled(true);
                return;
            }

            if (SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player)) {
                event.setCancelled(true);
                return;
            }

            profile.addCombatTag();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamageByPlayer(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player damagedPlayer)) {
            return;
        }

        Player damager;

        if (event.getDamager() instanceof Player p) {
            damager = p;
        } else if (event.getDamager() instanceof Arrow arrow
                && arrow.getShooter() instanceof Player p) {
            damager = p;
        } else {
            return;
        }

        Profile damagedProfile = SoupPvP.getInstance()
                .getProfilesHandler()
                .getProfileByUUID(damagedPlayer.getUniqueId());

        Profile damagerProfile = SoupPvP.getInstance()
                .getProfilesHandler()
                .getProfileByUUID(damager.getUniqueId());

        if (damagedProfile == null || damagerProfile == null) {
            event.setCancelled(true);
            return;
        }

        ProfileState damagedState = damagedProfile.getProfileState();
        ProfileState damagerState = damagerProfile.getProfileState();

        // Spawn region protection
        if (SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(damager)
                || SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(damagedPlayer)) {
            event.setCancelled(true);
            return;
        }

        // Spawn / 1v1 lobby protection
        if (damagerState == ProfileState.SPAWN
                || damagedState == ProfileState.SPAWN
                || damagerState == ProfileState.IN_1V1_LOBBY
                || damagedState == ProfileState.IN_1V1_LOBBY) {

            event.setCancelled(true);
            return;
        }

        if (!SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(damager)
                || !SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(damagedPlayer)) {

            damagerProfile.addCombatTag();
            damagedProfile.addCombatTag();
        }

    }



    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        Location spawn = new Location(player.getWorld(), 0 ,81,0);
        event.setDeathMessage(null);
        event.getDrops().clear(); // <-- add this
        event.setDroppedExp(0);

        Phoenix api = Phoenix.getInstance();
        xyz.refinedev.phoenix.profile.IProfile profile2 = api.getProfileHandler().getProfile(profile.getUuid());




        if (event.getEntity().getKiller() != null && event.getEntity().getKiller() != event.getEntity()) {
            Profile killerProfile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getKiller().getUniqueId());
            if (killerProfile.isJuggernaut()) return;

            killerProfile.setKills(killerProfile.getKills() + 1);
            killerProfile.incrementKitKills(killerProfile.getCurrentKit().getName());

            killerProfile.setCurrentKillstreak(killerProfile.getCurrentKillstreak() + 1);

            if (killerProfile.getCurrentKillstreak() > killerProfile.getHighestKillstreak()){
                event.getEntity().getKiller().sendMessage(CC.translate("&aHighest Killstreak! &fYou are on a new highest killstreak of &a" + killerProfile.getCurrentKillstreak() + "&f."));
                killerProfile.setHighestKillstreak(killerProfile.getCurrentKillstreak());
            }
            killerProfile.setCredits(killerProfile.getCredits() + 17);
            killerProfile.setExperiences(killerProfile.getExperiences() + 3);

            if (killerProfile.getEnableKillDeathMessages()) {
                if (killerProfile.getCurrentKit().getName().equals("Pro")) {
                    event.getEntity().getKiller().getPlayer().sendMessage(CC.translate(
                            "&9You have killed "
                                    + profile2.getHighestRank().getColor()
                                    + event.getEntity().getName()
                                    + " &9for &a34 credits&9."
                    ));
                } else {
                    event.getEntity().getKiller().getPlayer().sendMessage(CC.translate(
                            "&9You have killed "
                                    + profile2.getHighestRank().getColor()
                                    + event.getEntity().getName()
                                    + " &9for &a17 credits&9."

                    ));
                }
            }

            PlayerUtil.repairPlayer(event.getEntity().getKiller().getPlayer());


            if (profile.getEnableKillDeathMessages()){

                Phoenix api2 = Phoenix.getInstance();

                xyz.refinedev.phoenix.profile.IProfile killerPhoenixProfile =
                        api2.getProfileHandler().getProfile(
                                event.getEntity().getKiller().getUniqueId()
                        );

                event.getEntity().sendMessage(CC.translate(
                        "&cYou have been killed by "
                                + killerPhoenixProfile.getHighestRank().getColor()
                                + event.getEntity().getKiller().getName()
                                + "&c."
                ));
//                event.getEntity().sendMessage(CC.translate("&cYou have been killed by &r" + killerPhoenixProfile.getHighestRank().getColor() + event.getEntity().getKiller().getName() + "&c."));
            }
            if (profile.getCurrentKillstreak() >= 10){
                for (Profile profile1 : SoupPvP.getInstance().getProfilesHandler().getProfiles()){
                    if (profile1.getEnableKillDeathMessages()){
                        Bukkit.getPlayer(profile1.getUuid()).sendMessage(CC.translate("&e" + profile.getUsername() + " &adied with a &e" + profile.getCurrentKillstreak() + " &akillstreak!"));
                    }
                }
            }
        } else {
            for (Profile profile1 : SoupPvP.getInstance().getProfilesHandler().getProfiles()) {
                if (!profile1.getEnableKillDeathMessages()) {
                    continue;
                }

                Player target = Bukkit.getPlayer(profile1.getUuid());
                if (target == null) {
                    continue;
                }

                String deathMessage =
                        profile2.getHighestRank().getColor()
                                + event.getEntity().getName()
                                + " &adied.";

                target.sendMessage(CC.translate(deathMessage));

                if (profile1.equals(profile)) {
                    target.sendMessage(CC.translate("&cYou died."));
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(
                SoupPvP.getInstance(),
                () -> player.spigot().respawn(),
                1L
        );

        PlayerUtil.teleportToLobby(player);
        profile.setCurrentKillstreak(0);
        profile.setDeaths(profile.getDeaths() + 1);
        LunarClientListener.updateNametag(event.getEntity());
    }


    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        PlayerUtil.resetPlayer(player);
        PlayerUtil.teleportToLobby(player);

        Bukkit.getScheduler().runTaskLater(
                SoupPvP.getInstance(),
                () -> PlayerUtil.resetPlayer(player),
                1L
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWarpSign(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        Player player = event.getPlayer();

        Material type = event.getClickedBlock().getType();

        if (!(type.name().contains("SIGN"))) {
            return;
        }

        if (!(event.getClickedBlock().getState() instanceof Sign sign)) {
            return;
        }

        String line1 = sign.getLine(0);
        String line2 = sign.getLine(1);

        if (!line1.contains("Warp")) {
            return;
        }

        event.setCancelled(true);

        // Check player is in spawn
        if (!isInSpawn(player.getLocation())) {
            player.sendMessage(CC.translate("&cYou must be in Spawn to use this warp."));
            return;
        }

        Location destination;

        switch (line2.trim().toLowerCase()) {
            case "south":
                destination = new Location(player.getWorld(), 6, 51, 36, -0, 1); // replace with real coords
                break;
            case "north":
                destination = new Location(player.getWorld(), 70, 52, 195, 126, 0 );
                break;
            case "east":
                destination = new Location(player.getWorld(), 81, 50, 99, 50, -0);
                break;
            case "west":
                destination = new Location(player.getWorld(), -66, 51, 170, -115, -2);
                break;
            default:
                player.sendMessage(CC.translate("&cInvalid warp sign."));
                return;
        }

        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());

        player.teleport(destination);
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName(profile.getCurrentKit().getName());
        kit.equipKit(player);
    }

    private boolean isInSpawn(Location loc) {
        // Define your spawn region bounds here
        Location spawnMin = new Location(loc.getWorld(), -50, 0, -50);
        Location spawnMax = new Location(loc.getWorld(), 50, 256, 50);

        return loc.getX() >= spawnMin.getX() && loc.getX() <= spawnMax.getX()
                && loc.getY() >= spawnMin.getY() && loc.getY() <= spawnMax.getY()
                && loc.getZ() >= spawnMin.getZ() && loc.getZ() <= spawnMax.getZ();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSoupRefillSign(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        Player player = event.getPlayer();

        Material type = event.getClickedBlock().getType();

        if (!(type.name().contains("SIGN"))) {
            return;
        }

        if (!(event.getClickedBlock().getState() instanceof Sign sign)) {
            return;
        }

        String line1 = sign.getLine(0);
        String line2 = sign.getLine(1);

        if (!line1.contains("Free") || !line2.contains("Soup")) {
            return;
        }

        event.setCancelled(true);

        Profile profile = SoupPvP.getInstance()
                .getProfilesHandler()
                .getProfileByUUID(player.getUniqueId());

        if (profile.isJuggernaut()) {
            player.sendMessage(CC.translate("&cYou may not refill soups whilst in Juggernaut."));
            return;
        }

        Inventory inventory = Bukkit.createInventory(null, 54, "Refill Station");

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, new ItemStack(Material.MUSHROOM_STEW));
        }

        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWaterSpread(BlockFromToEvent event) {
        if (event.getBlock().getType() == Material.WATER) {
            event.setCancelled(true);
        }
    }

    private final Random random = new Random();

    public Location getRandomTeleportLocation(Player player) {
        List<Location> locations = Arrays.asList(
                new Location(player.getWorld(), 23, 149, 181),
                new Location(player.getWorld(), 38, 149, 201),
                new Location(player.getWorld(), -8, 149, 216),
                new Location(player.getWorld(), -19, 149, 174)
        );

        return locations.get(random.nextInt(locations.size()));
    }

    @EventHandler
    public void CloudSignEvent(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (event.getClickedBlock().getType().equals(Material.OAK_SIGN) || event.getClickedBlock().getType().equals(Material.OAK_SIGN) || event.getClickedBlock().getType().equals(Material.OAK_WALL_SIGN)) {
                Sign cloudSign = (Sign) event.getClickedBlock().getState();
                if (cloudSign.getLine(0).contains("Teleport") && cloudSign.getLine(1).contains("Clouds")) {
                    if (profile.isJuggernaut()){
                        player.sendMessage(CC.translate("&cYou may not teleport to the clouds whilst in Juggernaut."));
                        return;
                    }
                    // Teleport the player to specific coordinates
                    Location teleportLocation = getRandomTeleportLocation(player);
                    player.teleport(teleportLocation);
                    player.sendMessage(CC.translate("&aYou have been teleported to the clouds!"));
                }
            }
        }
    }

}
