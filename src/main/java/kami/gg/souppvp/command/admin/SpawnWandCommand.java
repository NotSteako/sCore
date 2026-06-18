package kami.gg.souppvp.command.admin;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.handlers.SpawnPolygon;
import kami.gg.souppvp.util.CC;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SpawnWandCommand implements Listener {

    private static final Material WAND_MATERIAL = Material.BLAZE_ROD;
    private static final String   WAND_NAME     = ChatColor.GOLD + "Spawn Wand";

    // Per-session point lists: UUID → list of [x, z] pairs
    private final Map<UUID, List<double[]>> sessions      = new HashMap<>();
    private final Map<UUID, World>          sessionWorlds = new HashMap<>();

    // Players currently outside spawn who are seeing the warning border
    private final Set<UUID> outsideBorder = new HashSet<>();

    // ── Commands ───────────────────────────────────────────────────

    @Command(name = "", desc = "Give spawn wand")
    public void give(@Sender CommandSender sender) {
        Player player = (Player) sender;
        if (!player.hasPermission("souppvp.admin")) {
            player.sendMessage(CC.translate("&cNo permission."));
            return;
        }
        giveWand(player);
    }

    @Command(name = "done", desc = "Save spawn polygon")
    public void done(@Sender CommandSender sender) {
        finishPolygon((Player) sender);
    }

    @Command(name = "clear", desc = "Clear spawn points")
    public void clear(@Sender CommandSender sender) {
        Player player = (Player) sender;
        sessions.remove(player.getUniqueId());
        sessionWorlds.remove(player.getUniqueId());
        player.sendMessage(CC.translate("&ePoints cleared."));
    }

    @Command(name = "undo", desc = "Undo last point")
    public void undo(@Sender CommandSender sender) {
        Player player = (Player) sender;
        List<double[]> pts = sessions.get(player.getUniqueId());
        if (pts == null || pts.isEmpty()) {
            player.sendMessage(CC.translate("&cNo points to undo."));
            return;
        }
        double[] removed = pts.remove(pts.size() - 1);
        player.sendMessage(CC.translate("&eRemoved last point &7("
                + fmt(removed[0]) + ", " + fmt(removed[1]) + "&7)."));
    }

    // ── Wand interaction ───────────────────────────────────────────
    // Using RIGHT_CLICK_BLOCK so the block is never broken client-side.
    // LEFT_CLICK_BLOCK sends a dig packet that causes a visual break
    // even when cancelled; right-click has no such side effect.

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        // Accept both right-click air and right-click block so swinging
        // in the air doesn't feel broken, but only record on a real block.
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

        Player player = event.getPlayer();
        if (!isHoldingWand(player)) return;

        event.setCancelled(true); // prevent block placement / use

        if (!player.hasPermission("souppvp.admin")) return;
        if (action == Action.RIGHT_CLICK_AIR || event.getClickedBlock() == null) {
            player.sendMessage(CC.translate("&eRight-click a &fblock&e to add a point."));
            return;
        }

        World world = event.getClickedBlock().getWorld();
        UUID  uuid  = player.getUniqueId();

        if (sessionWorlds.containsKey(uuid) && !sessionWorlds.get(uuid).equals(world)) {
            player.sendMessage(CC.translate("&cAll points must be in the same world. Use /spawnwand clear."));
            return;
        }

        sessionWorlds.put(uuid, world);
        sessions.computeIfAbsent(uuid, k -> new ArrayList<>());

        // Centre on the block so diagonal corners feel natural
        double x = event.getClickedBlock().getX() + 0.5;
        double z = event.getClickedBlock().getZ() + 0.5;
        sessions.get(uuid).add(new double[]{x, z});

        int count = sessions.get(uuid).size();

        // Highlight the selected block with particles so the admin can see it
        Location highlight = event.getClickedBlock().getLocation().add(0.5, 1.1, 0.5);
        player.spawnParticle(Particle.FLAME, highlight, 8, 0.2, 0.1, 0.2, 0.02);

        player.sendMessage(CC.translate("&aPoint &f" + count + " &aadded at &f"
                + fmt(x) + "&7, &f" + fmt(z)
                + " &7— do &f/spawnwand done &7when finished."));
    }

    // ── Player-move: show/hide per-player world border ─────────────
    // Paper 1.21 supports player.setWorldBorder(WorldBorder) which is
    // client-side only — the player sees a vanilla warning border while
    // outside spawn without affecting anyone else.

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Skip if they haven't crossed a block boundary (saves CPU)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        SpawnPolygon polygon = SoupPvP.getInstance().getPacketHandler().getPolygon();
        if (polygon == null) return;

        boolean insideNow = polygon.contains(event.getTo().getX(), event.getTo().getZ());
        boolean wasOutside = outsideBorder.contains(player.getUniqueId());

        if (!insideNow && !wasOutside) {
            // Just left spawn — show the border
            outsideBorder.add(player.getUniqueId());
            showSpawnBorder(player, polygon);
        } else if (insideNow && wasOutside) {
            // Just re-entered spawn — reset to the world's default border
            outsideBorder.remove(player.getUniqueId());
            player.setWorldBorder(null); // null = revert to server border
        }
    }

    // ── Helpers ────────────────────────────────────────────────────

    /**
     * Creates a per-player WorldBorder centred on the polygon and sized
     * to just enclose it. The warning distance (red fog) is always active
     * because we set warningDistance to the full radius, meaning the player
     * is always inside the warning zone when outside spawn.
     */
    private void showSpawnBorder(Player player, SpawnPolygon polygon) {
        // Find the bounding box of the polygon
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;

        for (double[] pt : polygon.getPoints()) {
            minX = Math.min(minX, pt[0]);
            maxX = Math.max(maxX, pt[0]);
            minZ = Math.min(minZ, pt[1]);
            maxZ = Math.max(maxZ, pt[1]);
        }

        double centerX = (minX + maxX) / 2.0;
        double centerZ = (minZ + maxZ) / 2.0;

        // The border size is the full diameter; add a small buffer so the
        // vanilla border wall sits roughly on the polygon edge
        double diameter = Math.max(maxX - minX, maxZ - minZ) + 2.0;

        WorldBorder border = Bukkit.createWorldBorder();
        border.setCenter(centerX, centerZ);
        border.setSize(diameter);
        border.setWarningDistance((int) (diameter / 2) + 8); // always in warning zone
        border.setWarningTime(0);
        border.setDamageBuffer(0);
        border.setDamageAmount(0); // visual only — your ProfileState handles actual restriction

        player.setWorldBorder(border);
    }

    private void giveWand(Player player) {
        ItemStack wand = new ItemStack(WAND_MATERIAL);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(WAND_NAME);
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Right-click blocks to add border points",
                ChatColor.YELLOW + "/spawnwand done " + ChatColor.GRAY + "to save",
                ChatColor.YELLOW + "/spawnwand undo " + ChatColor.GRAY + "to remove last",
                ChatColor.YELLOW + "/spawnwand clear " + ChatColor.GRAY + "to start over"
        ));
        wand.setItemMeta(meta);
        player.getInventory().addItem(wand);
        player.sendMessage(CC.translate("&aSpawn wand given. &7Right-click blocks to place points."));
    }

    private void finishPolygon(Player player) {
        UUID uuid = player.getUniqueId();
        List<double[]> pts = sessions.get(uuid);

        if (pts == null || pts.size() < 3) {
            player.sendMessage(CC.translate("&cNeed at least 3 points to form a polygon."));
            return;
        }

        World world = sessionWorlds.get(uuid);
        SpawnPolygon polygon = new SpawnPolygon(world, pts);

        var config = SoupPvP.getInstance().getConfig();
        config.set("spawn-polygon", null);
        polygon.save(config.createSection("spawn-polygon"));
        SoupPvP.getInstance().saveConfig();

        SoupPvP.getInstance().getPacketHandler().setPolygon(polygon);

        sessions.remove(uuid);
        sessionWorlds.remove(uuid);

        player.sendMessage(CC.translate("&aSpawn border saved with &f" + pts.size() + " &apoints!"));
    }

    private boolean isHoldingWand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != WAND_MATERIAL) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && WAND_NAME.equals(meta.getDisplayName());
    }

    private String fmt(double v) {
        return String.format("%.1f", v);
    }
}