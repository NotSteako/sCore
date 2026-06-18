package kami.gg.souppvp.ffa;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.KitsHandler;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class FFACommand {

    private static Location pos1;
    private static Location pos2;

    private static Location regionMin;
    private static Location regionMax;

    private static final Location FFA_SPAWN =
            new Location(Bukkit.getWorld("world"), 1200, 103, 1200);

    public static void setPos1(Location location) {
        pos1 = location;
    }

    public static void setPos2(Location location) {
        pos2 = location;
    }

    public static Location getPos1() {
        return pos1;
    }

    public static Location getPos2() {
        return pos2;
    }

    public static boolean isInSafeZone(Location location) {

        if (location == null || location.getWorld() == null) {
            return false;
        }

        if (regionMin == null || regionMax == null) {
            return false;
        }

        if (regionMin.getWorld() == null || regionMax.getWorld() == null) {
            return false;
        }

        // 🔥 WORLD SAFETY CHECK (this is what you were missing)
        if (!location.getWorld().equals(regionMin.getWorld())) {
            return false;
        }

        // optional but recommended: ensure region is same world too
        if (!regionMin.getWorld().equals(regionMax.getWorld())) {
            return false;
        }

        return location.getBlockX() >= regionMin.getBlockX()
                && location.getBlockX() <= regionMax.getBlockX()
                && location.getBlockY() >= regionMin.getBlockY()
                && location.getBlockY() <= regionMax.getBlockY()
                && location.getBlockZ() >= regionMin.getBlockZ()
                && location.getBlockZ() <= regionMax.getBlockZ();
    }

    @Command(name = "", desc = "Teleport to FFA")
    public void execute(@Sender CommandSender sender) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return;
        }

        Player player = (Player) sender;
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());

        if (profile.getProfileState() != ProfileState.SPAWN) {
            player.sendMessage("§cYou can only use this command while in spawn.");
            return;
        }

        player.teleport(FFA_SPAWN);

        // Give Default kit
        Kit defaultKit = SoupPvP.getInstance()
                .getKitsHandler()
                .getKitByName("Default");

        if (defaultKit != null) {
            defaultKit.equipKit(player);
        }

        player.sendMessage("§aTeleported to FFA.");
    }

    public static void setRegion(Location pos1, Location pos2) {
        regionMin = new Location(
                pos1.getWorld(),
                Math.min(pos1.getBlockX(), pos2.getBlockX()),
                Math.min(pos1.getBlockY(), pos2.getBlockY()),
                Math.min(pos1.getBlockZ(), pos2.getBlockZ())
        );

        regionMax = new Location(
                pos1.getWorld(),
                Math.max(pos1.getBlockX(), pos2.getBlockX()),
                Math.max(pos1.getBlockY(), pos2.getBlockY()),
                Math.max(pos1.getBlockZ(), pos2.getBlockZ())
        );
    }

}