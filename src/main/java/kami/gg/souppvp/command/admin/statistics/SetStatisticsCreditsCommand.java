package kami.gg.souppvp.command.admin.statistics;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import org.bukkit.command.CommandSender;

/**
 * @author hieu
 * @date 26/09/2023
 */

public class SetStatisticsCreditsCommand {

    @Command(name = "credits", desc = "Set player's credits", usage = "<value>")
    @Require("souppvp.statistics")
    public void execute(@Sender CommandSender sender, Profile profile, int value){
        if (profile == null){
            sender.sendMessage(CC.translate("&cCouldn't resolve that player's name."));
            return;
        }
        profile.setCredits(value);
        sender.sendMessage(CC.translate("&aSuccessfully updated!"));
        profile.saveProfile();
    }

}
