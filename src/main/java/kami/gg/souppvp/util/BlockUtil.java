package kami.gg.souppvp.util;

import kami.gg.souppvp.SoupPvP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;

public class BlockUtil {

    public static void generateTemporaryStimBeacon(Location location) {
        if (location.getBlock().getType().equals(Material.AIR)){
            location.getBlock().setType(Material.BREWING_STAND);
            Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> location.getBlock().setType(Material.AIR), 100L);
        }
    }

    public static void generateTemporary3x3Block(Block block, Material material) {
        Location center = block.getLocation().add(0, 1, 0);

        Location plus1X = block.getLocation().add(1, 1, 0);
        Location plus1Xplus1Z = block.getLocation().add(1, 1, 1);
        Location plus1Z = block.getLocation().add(0, 1, 1);
        Location neg1Xplus1Z = block.getLocation().add(-1, 1, 1);
        Location neg1X = block.getLocation().add(-1, 1, 0);
        Location neg1XnegZ = block.getLocation().add(-1, 1, -1);
        Location neg1Z = block.getLocation().add(0, 1, -1);
        Location plus1negZ = block.getLocation().add(1, 1, -1);

        ArrayList<Location> placingLocation = new ArrayList<>();
        placingLocation.add(center);
        placingLocation.add(plus1X);
        placingLocation.add(plus1Xplus1Z);
        placingLocation.add(plus1Z);
        placingLocation.add(neg1Xplus1Z);
        placingLocation.add(neg1X);
        placingLocation.add(neg1XnegZ);
        placingLocation.add(neg1Z);
        placingLocation.add(plus1negZ);

        for (Location placingLocationCheck : placingLocation){
            if (placingLocationCheck.getBlock().getType().equals(Material.AIR)){
                placingLocationCheck.getBlock().setType(material);
                placingLocationCheck.getBlock().setMetadata("AvatarWaterPool", new FixedMetadataValue(SoupPvP.getInstance(), "avatarKit"));
                Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> placingLocationCheck.getBlock().setType(Material.AIR), 80L);
                Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), placingLocation::clear, 100L);
            }
        }
    }

    public static Block getTargetBlock(Player player, int range) {
        BlockIterator iter = new BlockIterator(player, range);
        Block lastBlock = iter.next();
        while (iter.hasNext()) {
            lastBlock = iter.next();
            if (lastBlock.getType() == Material.AIR) continue;
            break;
        }
        return lastBlock;
    }

    public static void generateTemporarySphere(Location centerBlock, int radius, boolean hollow, Material type, int duration) {
        int bx = centerBlock.getBlockX();
        int by = centerBlock.getBlockY();
        int bz = centerBlock.getBlockZ();

        for (int x = bx - radius; x <= bx + radius; x++) {
            for (int y = by - radius; y <= by + radius; y++) {
                for (int z = bz - radius; z <= bz + radius; z++) {

                    double distance = ((bx - x) * (bx - x) + ((bz - z) * (bz - z)) + ((by - y) * (by - y)));

                    if (distance < radius * radius && !(hollow && distance < ((radius - 1) * (radius - 1)))) {

                        Location l = new Location(centerBlock.getWorld(), x, y, z);

                        if (y>=centerBlock.getBlockY() && l.getBlock().getType() == Material.AIR) {
                            l.getBlock().setType(type);

                            Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
                                if (l.getBlock().getType() == type)
                                    l.getBlock().setType(Material.AIR);
                            }, duration * 20L);
                        }
                    }

                }
            }
        }
    }

}
