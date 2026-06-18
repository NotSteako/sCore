package kami.gg.souppvp.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FreeKitsCommand {

    @Command(name = "kits toggle", desc = "toggle free kits mode")
    @Require("souppvp.freekits")
    public void execute(@Sender CommandSender sender){
        Player player = (Player) sender;
        SoupPvP.setIsFreeKitsMode(!SoupPvP.getIsFreeKitsMode());
        SoupPvP.getInstance().getConfig().set("FREE-KITS", SoupPvP.getIsFreeKitsMode());
        SoupPvP.getInstance().saveConfig();
        SoupPvP.getInstance().reloadConfig();
        String status = SoupPvP.getIsFreeKitsMode() ? "&aenabled" : "&cdisabled";
        player.sendMessage(CC.translate("&aSuccessfully " + status + " &afree kits mode!"));
    }

}
