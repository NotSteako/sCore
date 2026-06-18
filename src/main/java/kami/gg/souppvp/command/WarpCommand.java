package kami.gg.souppvp.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.gui.WarpGUI;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WarpCommand {

    private final SoupPvP plugin;

    private static final String WORLD_NAME = "world";

    public WarpCommand(SoupPvP plugin) {
        this.plugin = plugin;
    }

    @Command(
            name = "",
            desc = "Teleport to a warp",
            usage = "<north|east|south|west>"
    )
    public void warp(@Sender Player player, String type) {
        if (!canWarp(player)) {
            return;
        }

        World world = Bukkit.getWorld(WORLD_NAME);

        if (world == null) {
            player.sendMessage(CC.translate("&cWorld '" + WORLD_NAME + "' could not be found."));
            return;
        }

        switch (type.toLowerCase()) {
            case "north":
                player.teleport(new Location(world, 0, 92, -95, 179F, 3F));
                player.sendMessage(CC.translate("&aTeleported to &bNorth&a!"));
                break;

            case "east":
                player.teleport(new Location(world, 95, 92, 0, -90F, -1F));
                player.sendMessage(CC.translate("&aTeleported to &bEast&a!"));
                break;

            case "south":
                player.teleport(new Location(world, 0, 92, 95, 0F, -1F));
                player.sendMessage(CC.translate("&aTeleported to &bSouth&a!"));
                break;

            case "west":
                player.teleport(new Location(world, -95, 92, 0, 91F, -5F));
                player.sendMessage(CC.translate("&aTeleported to &bWest&a!"));
                break;

            default:
                WarpGUI.open(player);
                break;
        }
    }

    private boolean canWarp(Player player) {
        Profile profile = SoupPvP.getInstance()
                .getProfilesHandler()
                .getProfileByUUID(player.getUniqueId());

        if (profile.isCombatTagged()) {
            player.sendMessage(CC.translate("&cYou're currently combat-tagged."));
            return false;
        }

        if (!SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player)) {
            player.sendMessage(CC.translate("&aYou have to be in &bSpawn&a."));
            return false;
        }

        return true;
    }
}