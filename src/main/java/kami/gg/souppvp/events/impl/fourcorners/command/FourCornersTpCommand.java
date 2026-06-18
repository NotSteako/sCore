package kami.gg.souppvp.events.impl.fourcorners.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class FourCornersTpCommand {

    @Command(name = "tp", desc = "tp to 4Corners event")
    @Require("souppvp.fourcornerstp")
    public void execute(@Sender Player player) {
        if (SoupPvP.getInstance().getFourCornersHandler().getSpectatorSpawn() == null) {
            player.sendMessage(CC.translate("&cSpectator spawn is not set.")); return;
        }
        player.teleport(SoupPvP.getInstance().getFourCornersHandler().getSpectatorSpawn());
        player.sendMessage(CC.translate("&aSuccessfully teleported to the 4Corners spectator spawn."));
    }
}