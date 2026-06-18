package kami.gg.souppvp.events.impl.redrover.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.redrover.RedRover;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RedRoverHostCommand {

    @Command(name = "host", desc = "host red rover event")
    @Require("redrover.host")
    public void execute(@Sender Player player) {
        if (SoupPvP.getInstance().getRedRoverHandler().getActiveRedRover() != null) {
            player.sendMessage(ChatColor.RED + "There is already an active event.");
            return;
        }
        SoupPvP.getInstance().getRedRoverHandler().setActiveRedRover(new RedRover(player));
        SoupPvP.getInstance().getRedRoverHandler().getActiveRedRover().handleJoin(player);
    }

}