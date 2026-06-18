package kami.gg.souppvp.events.impl.fourcorners.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.fourcorners.FourCorners;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class FourCornersHostCommand {

    @Command(name = "host", desc = "host 4Corners event")
    @Require("souppvp.fourcornershost")
    public void execute(@Sender Player player) {
        if (SoupPvP.getInstance().getFourCornersHandler().getActiveEvent() != null) {
            player.sendMessage(ChatColor.RED + "There is already an active event.");
            return;
        }
        SoupPvP.getInstance().getFourCornersHandler().setActiveEvent(new FourCorners(player));
        SoupPvP.getInstance().getFourCornersHandler().getActiveEvent().handleJoin(player);
    }
}