package kami.gg.souppvp.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.perk.menu.PerksMenu;
import org.bukkit.entity.Player;

public class PerksCommand {

    @Command(name = "", desc = "open the perks menu")
    public void execute(@Sender Player player) {
        new PerksMenu().openMenu(player);
    }

}
