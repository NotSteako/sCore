package kami.gg.souppvp.ffa.util;

import org.bukkit.Location;

public class FFACuboid {

    private final Location min;
    private final Location max;

    public FFACuboid(Location pos1, Location pos2) {
        this.min = new Location(
                pos1.getWorld(),
                Math.min(pos1.getBlockX(), pos2.getBlockX()),
                Math.min(pos1.getBlockY(), pos2.getBlockY()),
                Math.min(pos1.getBlockZ(), pos2.getBlockZ())
        );

        this.max = new Location(
                pos1.getWorld(),
                Math.max(pos1.getBlockX(), pos2.getBlockX()),
                Math.max(pos1.getBlockY(), pos2.getBlockY()),
                Math.max(pos1.getBlockZ(), pos2.getBlockZ())
        );
    }

    public boolean contains(Location location) {
        return location.getWorld().equals(min.getWorld())
                && location.getBlockX() >= min.getBlockX()
                && location.getBlockX() <= max.getBlockX()
                && location.getBlockY() >= min.getBlockY()
                && location.getBlockY() <= max.getBlockY()
                && location.getBlockZ() >= min.getBlockZ()
                && location.getBlockZ() <= max.getBlockZ();
    }
}