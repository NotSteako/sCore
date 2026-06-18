package kami.gg.souppvp.events.impl.redrover.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class RedRoverTpCommand {

    @Command(name = "tp", desc = "tp to red rover event")
    @Require("redrover.host")
    public void execute(@Sender Player player) {
        player.teleport(SoupPvP.getInstance().getRedRoverHandler().getSpectatorSpawn());
        player.sendMessage(CC.translate("&aSuccessfully teleported to the red rover system's spectator spawn location."));
    }

}