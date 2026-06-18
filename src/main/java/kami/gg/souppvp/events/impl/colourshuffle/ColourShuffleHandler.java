package kami.gg.souppvp.events.impl.colourshuffle;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.LocationUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages the active Colour Shuffle event plus the wool-floor region itself.
 * The floor is defined by two opposite corners loaded from
 * {@code EVENTS.COLOUR_SHUFFLE.FLOOR-A} and {@code FLOOR-B} in {@code config.yml}.
 */
public class ColourShuffleHandler {

    @Getter @Setter private ColourShuffle activeEvent;
    @Getter @Setter private Location spectatorSpawn;
    @Getter @Setter private Location floorA;
    @Getter @Setter private Location floorB;

    /** Snapshot of the original blocks under the floor area so we can restore on /cancel. */
    private final Map<String, Material> originalSnapshot = new HashMap<>();

    public ColourShuffleHandler() {
        load();
    }

    public void load() {
        String s1 = SoupPvP.getInstance().getConfig().getString("EVENTS.COLOUR_SHUFFLE.SPECTATOR-SPAWN");
        String s2 = SoupPvP.getInstance().getConfig().getString("EVENTS.COLOUR_SHUFFLE.FLOOR-A");
        String s3 = SoupPvP.getInstance().getConfig().getString("EVENTS.COLOUR_SHUFFLE.FLOOR-B");
        if (s1 != null) spectatorSpawn = LocationUtil.deserialize(s1);
        if (s2 != null) floorA = LocationUtil.deserialize(s2);
        if (s3 != null) floorB = LocationUtil.deserialize(s3);
    }

    public void save() {
        if (spectatorSpawn != null) SoupPvP.getInstance().getConfig().set("EVENTS.COLOUR_SHUFFLE.SPECTATOR-SPAWN", LocationUtil.serialize(spectatorSpawn));
        if (floorA != null)         SoupPvP.getInstance().getConfig().set("EVENTS.COLOUR_SHUFFLE.FLOOR-A", LocationUtil.serialize(floorA));
        if (floorB != null)         SoupPvP.getInstance().getConfig().set("EVENTS.COLOUR_SHUFFLE.FLOOR-B", LocationUtil.serialize(floorB));
        SoupPvP.getInstance().saveConfig();
        SoupPvP.getInstance().reloadConfig();
    }

    public boolean isConfigured() {
        return spectatorSpawn != null && floorA != null && floorB != null
                && floorA.getWorld() != null && floorA.getWorld().equals(floorB.getWorld());
    }

    /** Teleport target above the centre of the floor (1 block above the y-level). */
    public Location getFloorTeleportLocation() {
        if (!isConfigured()) return spectatorSpawn;
        double cx = (floorA.getX() + floorB.getX()) / 2.0 + 0.5;
        double cz = (floorA.getZ() + floorB.getZ()) / 2.0 + 0.5;
        double cy = Math.max(floorA.getY(), floorB.getY()) + 2;
        Location loc = new Location(floorA.getWorld(), cx, cy, cz);
        loc.setYaw(spectatorSpawn != null ? spectatorSpawn.getYaw() : 0);
        loc.setPitch(spectatorSpawn != null ? spectatorSpawn.getPitch() : 0);
        return loc;
    }

    private int minX() { return Math.min(floorA.getBlockX(), floorB.getBlockX()); }
    private int maxX() { return Math.max(floorA.getBlockX(), floorB.getBlockX()); }
    private int minZ() { return Math.min(floorA.getBlockZ(), floorB.getBlockZ()); }
    private int maxZ() { return Math.max(floorA.getBlockZ(), floorB.getBlockZ()); }
    private int floorY() { return Math.max(floorA.getBlockY(), floorB.getBlockY()); }

    /** Fill the floor region with random wool colours, snapshotting the original first. */
    public void shuffleFloor() {
        if (!isConfigured()) return;
        World world = floorA.getWorld();
        int y = floorY();
        if (originalSnapshot.isEmpty()) {
            // First call this event — snapshot the original floor so we can restore later.
            for (int x = minX(); x <= maxX(); x++) {
                for (int z = minZ(); z <= maxZ(); z++) {
                    originalSnapshot.put(x + ":" + y + ":" + z, world.getBlockAt(x, y, z).getType());
                }
            }
        }
        Material[] palette = ColourShuffle.WOOL_PALETTE;
        for (int x = minX(); x <= maxX(); x++) {
            for (int z = minZ(); z <= maxZ(); z++) {
                Material wool = palette[ThreadLocalRandom.current().nextInt(palette.length)];
                world.getBlockAt(x, y, z).setType(wool, false);
            }
        }
    }

    /** Remove every wool block that is not the safe colour (revealing the void/air below). */
    public void dropNonSafeBlocks(Material safeColour) {
        if (!isConfigured()) return;
        World world = floorA.getWorld();
        int y = floorY();
        for (int x = minX(); x <= maxX(); x++) {
            for (int z = minZ(); z <= maxZ(); z++) {
                Block b = world.getBlockAt(x, y, z);
                if (b.getType() != safeColour) {
                    b.setType(Material.AIR, false);
                }
            }
        }
    }

    /** Restore the original blocks under the floor area; clears snapshot. */
    public void clearFloor() {
        if (originalSnapshot.isEmpty() || !isConfigured()) return;
        World world = floorA.getWorld();
        for (Map.Entry<String, Material> entry : originalSnapshot.entrySet()) {
            String[] parts = entry.getKey().split(":");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            world.getBlockAt(x, y, z).setType(entry.getValue(), false);
        }
        originalSnapshot.clear();
    }
}
