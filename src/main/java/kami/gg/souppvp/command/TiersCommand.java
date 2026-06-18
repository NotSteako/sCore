package kami.gg.souppvp.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.tier.menu.TiersMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TiersCommand {

    @Command(name = "", desc = "open tiers menu")
    public void execute(@Sender CommandSender sender){
        Player player = (Player) sender;
        new TiersMenu().openMenu(player);
    }

}
