package kami.gg.souppvp.command.bounty;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.bounty.BountyMenu;
import org.bukkit.entity.Player;

public class BountyListCommand {

    @Command(name = "list", desc = "View all online bounties")
    public void execute(@Sender Player player){
        new BountyMenu().openMenu(player);
    }

}
