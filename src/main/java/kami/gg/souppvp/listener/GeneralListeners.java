package kami.gg.souppvp.listener;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.handlers.SpawnHandler;
import kami.gg.souppvp.perk.Perk;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.TaskUtil;
import kami.gg.souppvp.util.TasksUtility;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GeneralListeners implements Listener {

    private static final Set<UUID> spawnTeleportCooldown = new HashSet<>();

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (event.getTo() == null) return;

        // Ignore tiny movements (reduces spam)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        SpawnHandler spawn = SoupPvP.getInstance().getSpawnHandler();
        if (spawn == null || spawn.getCuboid() == null) return;

        Profile profile = SoupPvP.getInstance()
                .getProfilesHandler()
                .getProfileByUUID(player.getUniqueId());

        if (profile == null || profile.getProfileState() != ProfileState.COMBAT) return;

        // Must be inside spawn
        if (!spawn.getCuboid().contains(player)) return;

        // prevent teleport spam
        if (spawnTeleportCooldown.contains(player.getUniqueId())) return;
        spawnTeleportCooldown.add(player.getUniqueId());

        Location safe = findNearestLocationNearSpawnCuboid(player.getLocation().getBlock());

        if (safe == null || spawn.getCuboid().contains(safe)) {
            spawnTeleportCooldown.remove(player.getUniqueId());
            return;
        }

        player.teleport(safe.clone().add(0.5, 0, 0.5));

        Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () ->
                        spawnTeleportCooldown.remove(player.getUniqueId()),
                10L);
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event){
        event.setDeathMessage(null);
        event.getDrops().clear();
        event.setDroppedExp(0);
        for (Player player : Bukkit.getOnlinePlayers()){
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        }

        Profile playerProfile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getEntity().getUniqueId());
        Perk profilePerk = SoupPvP.getInstance().getPerksHandler().getPerkByName(playerProfile.getActivePerks().get(2));
        Perk conartistPerk = SoupPvP.getInstance().getPerksHandler().getPerkByName("Conartist");
        if (profilePerk == conartistPerk){
            if (new Random().nextInt(101) <= 50) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(SoupPvP.getInstance(), () -> event.getEntity().spigot().respawn(), 2L);
                return;
            }
        }
        ItemStack mushroom = new ItemStack(Material.MUSHROOM_STEW);
        for (int i=0; i<9; i++){
            event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), mushroom);
        }
        Location deathLocation = event.getEntity().getLocation();
        TaskUtil.runLater(() -> {
            for (Entity entity : Bukkit.getServer().getWorld("world").getEntities()) {
                if (entity.getLocation().distance(deathLocation) > 5){
                    return;
                }
                if (entity.getType().equals(EntityType.ITEM)) {
                    entity.remove();
                }
            }
        }, 60L);
        Bukkit.getScheduler().scheduleSyncDelayedTask(SoupPvP.getInstance(), () -> event.getEntity().spigot().respawn(), 2L);
    }


    private static final Set<Material> BLOCKED_CONTAINERS = EnumSet.of(
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.ENDER_CHEST,
            Material.BARREL,
            Material.SMITHING_TABLE,
            Material.ANVIL,
            Material.CHIPPED_ANVIL,
            Material.DAMAGED_ANVIL,
            Material.CRAFTING_TABLE,
            Material.FURNACE,
            Material.BLAST_FURNACE,
            Material.SMOKER,
            Material.BREWING_STAND,
            Material.HOPPER,
            Material.DROPPER,
            Material.DISPENSER,
            Material.ENCHANTING_TABLE,
            Material.GRINDSTONE,
            Material.LOOM,
            Material.CARTOGRAPHY_TABLE,
            Material.STONECUTTER
    );

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;

        if (BLOCKED_CONTAINERS.contains(event.getClickedBlock().getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SURVIVAL){
            ItemStack itemStack = event.getItemDrop().getItemStack();
            Material itemType = itemStack.getType();
            String itemTypeName = itemType.name().toLowerCase();

            if (itemTypeName.contains("sword") || itemTypeName.contains("fishing_rod")  || itemTypeName.contains("potion") || itemTypeName.contains("axe") || itemType == Material.BOW) {
                player.sendMessage(CC.translate("&cYou can't drop your attacking weapon."));
                event.setCancelled(true);
            }

            if (itemTypeName.contains("feather") || itemTypeName.contains("packed_ice") || itemTypeName.contains("blaze_rod") || itemTypeName.contains("iron_chain")) {
                player.sendMessage(CC.translate("&cYou can't drop your signature."));
                event.setCancelled(true);
            }
//            if (itemTypeName.contains("helmet") || itemTypeName.contains("player_head") || itemTypeName.contains("chestplate") || itemTypeName.contains("leggings") || itemTypeName.contains("boots")) {
//                player.sendMessage(CC.translate("&cYou can't drop your armor."));
//                event.setCancelled(true);
//            }
            if (itemType == Material.BOWL) {
                event.getItemDrop().remove();
            } else {
                TasksUtility.runTaskLater(() -> {
                    event.getItemDrop().remove();
                }, 5 * 20);
            }
        }
    }

    private static Location findNearestLocationNearSpawnCuboid(Block block) {
        SpawnHandler spawn = SoupPvP.getInstance().getSpawnHandler();
        if (spawn == null || spawn.getCuboid() == null) return null;

        List<Location> checks = Arrays.asList(
                block.getLocation().clone().add(1, 0, 0),
                block.getLocation().clone().add(-1, 0, 0),
                block.getLocation().clone().add(0, 0, 1),
                block.getLocation().clone().add(0, 0, -1),
                block.getLocation().clone().add(1, 0, 1),
                block.getLocation().clone().add(-1, 0, -1),
                block.getLocation().clone().add(1, 0, -1),
                block.getLocation().clone().add(-1, 0, 1)
        );

        for (Location loc : checks) {
            if (!spawn.getCuboid().contains(loc)) {
                return loc;
            }
        }

        // no safe location found
        return null;
    }
}