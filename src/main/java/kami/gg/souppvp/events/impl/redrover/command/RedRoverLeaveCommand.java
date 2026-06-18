package kami.gg.souppvp.events.impl.redrover.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.redrover.RedRover;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class RedRoverLeaveCommand {

    @Command(name = "leave", desc = "leave red rover event")
    public void execute(@Sender Player player) {
        RedRover active = SoupPvP.getInstance().getRedRoverHandler().getActiveRedRover();
        if (active == null) { player.sendMessage(CC.translate("&cThere isn't an active red rover event.")); return; }
        if (!active.getEventPlayers().containsKey(player.getUniqueId())) {
            player.sendMessage(CC.translate("&cYou are not apart of the active red rover event."));
            return;
        }
        active.handleLeave(player);
    }

}