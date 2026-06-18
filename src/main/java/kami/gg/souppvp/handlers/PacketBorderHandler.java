package kami.gg.souppvp.handlers;

import kami.gg.souppvp.SoupPvP;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Renders a fake red-glass border along a polygon's edges for players
 * who are outside the spawn zone but within RENDER_RADIUS blocks of it.
 *
 * Perf notes vs. the old cuboid version:
 *  - No Location allocations inside the hot path (reuses a single Location object)
 *  - Edge points cached; only recomputed when polygon changes
 *  - Already-shown blocks skip resend until their 4 s timer expires
 *  - Runs every 4 ticks instead of every tick
 */
public class PacketBorderHandler extends BukkitRunnable {

    // How close a player must be to the border (horizontal) before blocks appear
    private static final int RENDER_RADIUS    = 8;
    private static final int RENDER_RADIUS_SQ = RENDER_RADIUS * RENDER_RADIUS;

    // Vertical range of fake glass rendered around the player's eye level
    private static final int Y_RANGE = 4;

    // How long a fake block stays visible before being reverted (ms)
    private static final long DISPLAY_MILLIS = 4_000L;

    private static final BlockData BORDER_DATA =
            Material.RED_STAINED_GLASS.createBlockData();

    // polygon can be swapped at runtime (hot-reload from /spawnwand done)
    private volatile SpawnPolygon polygon;

    // Cached flat list of edge XZ columns: int[] {x, z}
    private int[][] edgeColumns;

    // Per-player: Location → expiry timestamp (ms)
    private final Map<UUID, Map<Location, Long>> sentBlocks = new HashMap<>();

    // ── Lifecycle ────────────────────────────────────────────────────────────

    public void setPolygon(SpawnPolygon polygon) {
        this.polygon = polygon;
        rebuildEdgeCache();
    }

    private void rebuildEdgeCache() {
        if (polygon == null || !polygon.isValid()) {
            edgeColumns = new int[0][];
            return;
        }
        // Walk every edge once and collect unique XZ columns
        java.util.LinkedHashSet<Long> seen = new java.util.LinkedHashSet<>();
        java.util.List<int[]> cols = new java.util.ArrayList<>();
        polygon.forEachEdgePoint((x, z) -> {
            long key = ((long) x << 32) | (z & 0xFFFFFFFFL);
            if (seen.add(key)) cols.add(new int[]{x, z});
        });
        edgeColumns = cols.toArray(new int[0][]);
    }

    public void start() {
        // Load polygon from config if present
        var config = SoupPvP.getInstance().getConfig();
        if (config.contains("spawn-polygon")) {
            try {
                SpawnPolygon loaded = SpawnPolygon.load(
                        config.getConfigurationSection("spawn-polygon"),
                        null /* unused param */);
                setPolygon(loaded);
            } catch (Exception e) {
                Bukkit.getLogger().warning("[SoupPvP] Failed to load spawn polygon: " + e.getMessage());
            }
        }
        runTaskTimer(SoupPvP.getInstance(), 0L, 4L); // every 4 ticks
    }

    // ── Main loop ────────────────────────────────────────────────────────────

    @Override
    public void run() {
        if (polygon == null || !polygon.isValid() || edgeColumns.length == 0) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            tick(player);
        }
    }

    private void tick(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (!player.getWorld().equals(polygon.getWorld())) {
            clearPlayer(player);
            return;
        }

        // If inside the polygon, remove any shown blocks and bail
        Location loc = player.getLocation();
        if (polygon.contains(loc.getX(), loc.getZ())) {
            clearPlayer(player);
            return;
        }

        Map<Location, Long> sent = sentBlocks.computeIfAbsent(
                player.getUniqueId(), k -> new HashMap<>());

        long now = System.currentTimeMillis();

        // Revert expired fake blocks
        Iterator<Map.Entry<Location, Long>> it = sent.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Location, Long> entry = it.next();
            if (now < entry.getValue()) continue;
            Location bl = entry.getKey();
            if (bl.getChunk().isLoaded()) {
                player.sendBlockChange(bl, bl.getBlock().getBlockData());
            }
            it.remove();
        }

        // Send border blocks near the player
        double px = loc.getX(), py = loc.getY(), pz = loc.getZ();
        // Reusable location — avoids allocation per block
        Location check = new Location(player.getWorld(), 0, 0, 0);

        for (int[] col : edgeColumns) {
            int cx = col[0], cz = col[1];
            double dx = cx + 0.5 - px;
            double dz = cz + 0.5 - pz;
            if (dx * dx + dz * dz > RENDER_RADIUS_SQ) continue;

            for (int dy = -Y_RANGE; dy <= Y_RANGE; dy++) {
                int by = (int) Math.floor(py) + dy;
                check.setX(cx);
                check.setY(by);
                check.setZ(cz);

                // Skip if already displayed and timer not expired
                Long expiry = sent.get(check);
                if (expiry != null && now < expiry) continue;

                if (!check.getChunk().isLoaded()) continue;
                if (check.getBlock().getType().isOccluding()) continue;

                // Clone needed for map key (check is reused)
                Location key = check.clone();
                player.sendBlockChange(key, BORDER_DATA);
                sent.put(key, now + DISPLAY_MILLIS);
            }
        }
    }

    // ── Cleanup ──────────────────────────────────────────────────────────────

    /** Reverts all fake blocks for a player and removes their entry. */
    public void clearPlayer(Player player) {
        Map<Location, Long> map = sentBlocks.remove(player.getUniqueId());
        if (map == null) return;
        for (Location loc : map.keySet()) {
            if (loc.getWorld().equals(player.getWorld()) && loc.getChunk().isLoaded()) {
                player.sendBlockChange(loc, loc.getBlock().getBlockData());
            }
        }
    }

    /** Call when a player quits so their entry is cleaned up. */
    public void onPlayerQuit(Player player) {
        sentBlocks.remove(player.getUniqueId()); // no revert needed — they're gone
    }

    public SpawnPolygon getPolygon() { return polygon; }
}