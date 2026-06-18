package kami.gg.souppvp.events.impl.colourshuffle.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class ColourShuffleSetFloorBCommand {

    @Command(name = "setfloorb", desc = "set corner B of the colour shuffle floor")
    @Require("souppvp.admin")
    public void execute(@Sender Player player) {
        SoupPvP.getInstance().getColourShuffleHandler().setFloorB(player.getLocation().getBlock().getLocation());
        SoupPvP.getInstance().getColourShuffleHandler().save();
        player.sendMessage(CC.translate("&aColour Shuffle floor corner B set to your block."));
    }
}
