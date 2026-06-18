package kami.gg.souppvp.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.killstreak.menu.KillstreakMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KillstreakCommand {

    @Command(name = "", desc = "open the killstreaks menu")
    public void execute(@Sender CommandSender sender){
        Player player = (Player) sender;
        new KillstreakMenu().openMenu(player);
    }

}
