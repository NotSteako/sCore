package kami.gg.souppvp.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.shop.ShopMenu;
import kami.gg.souppvp.util.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand {

    @Command(name = "", desc = "open the shop menu")
    public void execute(@Sender CommandSender sender){
        Player player = (Player) sender;
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(((Player) sender).getUniqueId());
        if (profile.isJuggernaut()){
            sender.sendMessage(CC.translate("&cYou may not visit the shop whilst in Juggernaut."));
            return;
        }
        new ShopMenu().openMenu(player);
    }

}
