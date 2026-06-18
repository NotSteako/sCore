package kami.gg.souppvp.events.impl.colourshuffle.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class ColourShuffleSetFloorACommand {

    @Command(name = "setfloora", desc = "set corner A of the colour shuffle floor")
    @Require("souppvp.admin")
    public void execute(@Sender Player player) {
        SoupPvP.getInstance().getColourShuffleHandler().setFloorA(player.getLocation().getBlock().getLocation());
        SoupPvP.getInstance().getColourShuffleHandler().save();
        player.sendMessage(CC.translate("&aColour Shuffle floor corner A set to your block."));
    }
}
