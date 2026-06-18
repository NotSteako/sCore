package kami.gg.souppvp.command.admin;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.Cuboid;
import kami.gg.souppvp.util.LocationUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CuboidSetCommand {

    @Command(name = "set", desc = "Setting spawn cuboid", usage = "<a/b>")
    @Require("souppvp.cuboid")
    public void execute(@Sender CommandSender sender, String position){
        Player player = (Player) sender;
        if (position.equalsIgnoreCase("a")){
            SoupPvP.getInstance().getSpawnHandler().setA(player.getLocation());
            SoupPvP.getInstance().getSpawnHandler().setCuboid(new Cuboid(SoupPvP.getInstance().getSpawnHandler().getA(), SoupPvP.getInstance().getSpawnHandler().getB()));
            SoupPvP.getInstance().getConfig().set("SPAWN.LOCATION-A", LocationUtil.convertLocationToString(SoupPvP.getInstance().getSpawnHandler().getA()));
            SoupPvP.getInstance().saveConfig();
            SoupPvP.getInstance().reloadConfig();
            player.sendMessage(CC.translate("&aSuccessfully updated the spawn's location a."));
        } else if (position.equalsIgnoreCase("b")) {
            SoupPvP.getInstance().getSpawnHandler().setB(player.getLocation());
            SoupPvP.getInstance().getSpawnHandler().setCuboid(new Cuboid(SoupPvP.getInstance().getSpawnHandler().getA(), SoupPvP.getInstance().getSpawnHandler().getB()));
            SoupPvP.getInstance().getConfig().set("SPAWN.LOCATION-B", LocationUtil.convertLocationToString(SoupPvP.getInstance().getSpawnHandler().getB()));
            SoupPvP.getInstance().saveConfig();
            SoupPvP.getInstance().reloadConfig();
            player.sendMessage(CC.translate("&aSuccessfully updated the spawn's location b."));
        } else {
            player.sendMessage(CC.translate("&cAvailable Positions: a, b"));
        }
    }

}
