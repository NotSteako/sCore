package kami.gg.souppvp.events.impl.colourshuffle.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class ColourShuffleSetSpawnCommand {

    @Command(name = "setspawn", desc = "set the spectator spawn for colour shuffle")
    @Require("souppvp.admin")
    public void execute(@Sender Player player) {
        SoupPvP.getInstance().getColourShuffleHandler().setSpectatorSpawn(player.getLocation());
        SoupPvP.getInstance().getColourShuffleHandler().save();
        player.sendMessage(CC.translate("&aColour Shuffle spectator spawn set to your location."));
    }
}
