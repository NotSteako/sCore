package kami.gg.souppvp.events.impl.fourcorners.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.fourcorners.FourCornersCorner;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class FourCornersSetSpawnCommand {

    @Command(name = "setspawn", desc = "set 4Corners event locations", usage = "<position>")
    @Require("souppvp.fourcornerssetspawn")
    public void execute(@Sender Player player, String position) {
        switch (position.toLowerCase()) {
            case "spec": case "spectator":
                SoupPvP.getInstance().getFourCornersHandler().setSpectatorSpawn(player.getLocation());
                player.sendMessage(CC.translate("&aSuccessfully updated the 4Corners spectator spawn."));
                SoupPvP.getInstance().getFourCornersHandler().save(); break;
            case "red":
                SoupPvP.getInstance().getFourCornersHandler().setCornerLocation(FourCornersCorner.RED, player.getLocation());
                player.sendMessage(CC.translate("&aSuccessfully updated the &cRed &acorner."));
                SoupPvP.getInstance().getFourCornersHandler().save(); break;
            case "blue":
                SoupPvP.getInstance().getFourCornersHandler().setCornerLocation(FourCornersCorner.BLUE, player.getLocation());
                player.sendMessage(CC.translate("&aSuccessfully updated the &9Blue &acorner."));
                SoupPvP.getInstance().getFourCornersHandler().save(); break;
            case "yellow":
                SoupPvP.getInstance().getFourCornersHandler().setCornerLocation(FourCornersCorner.YELLOW, player.getLocation());
                player.sendMessage(CC.translate("&aSuccessfully updated the &eYellow &acorner."));
                SoupPvP.getInstance().getFourCornersHandler().save(); break;
            case "green":
                SoupPvP.getInstance().getFourCornersHandler().setCornerLocation(FourCornersCorner.GREEN, player.getLocation());
                player.sendMessage(CC.translate("&aSuccessfully updated the &aGreen &acorner."));
                SoupPvP.getInstance().getFourCornersHandler().save(); break;
            default:
                player.sendMessage(CC.translate("&cAvailable Positions: red, blue, yellow, green, spec"));
        }
    }
}