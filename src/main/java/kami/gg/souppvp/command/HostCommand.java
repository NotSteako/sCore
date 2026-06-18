package kami.gg.souppvp.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.events.menu.HostEventsMenu;
import org.bukkit.entity.Player;

public class HostCommand {

    @Command(name = "", desc = "open the host menu")
    public void execute(@Sender Player player){
        new HostEventsMenu().openMenu(player);
    }

}
