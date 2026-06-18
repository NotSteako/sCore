package kami.gg.souppvp.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.coinflip.menu.CoinFlipMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CoinflipCommand {

    @Command(name = "", desc = "coinflip")
    public void execute(@Sender CommandSender sender){
        new CoinFlipMenu().openMenu((Player) sender);
    }

}
