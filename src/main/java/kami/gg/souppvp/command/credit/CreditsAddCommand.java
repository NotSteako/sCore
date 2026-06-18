package kami.gg.souppvp.command.credit;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import org.bukkit.command.CommandSender;

public class CreditsAddCommand {

    @Command(name = "add", desc = "add credits to player", usage = "<profile> <amount>")
    @Require("souppvp.credits")
    public void execute(@Sender CommandSender sender, Profile targetProfile, int amount){
        if (targetProfile == null){
            sender.sendMessage(CC.translate("&cCouldn't resolve that player's name."));
            return;
        }
        targetProfile.setCredits(targetProfile.getCredits() + amount);
        targetProfile.saveProfile();
        sender.sendMessage(CC.translate("&aSuccessfully add &e" + amount + "&a credits to &e" + targetProfile.getUsername() + "'s &abalance."));
    }

}
