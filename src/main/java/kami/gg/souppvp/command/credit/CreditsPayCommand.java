package kami.gg.souppvp.command.credit;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CreditsPayCommand {

    @Command(name = "", desc = "pay a player", usage = "<player> <amount>")
    public void execute(@Sender Player sender, Profile targetProfile, int amount) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(sender.getUniqueId());
        if (targetProfile == null){
            sender.sendMessage(CC.translate("Couldn't resolve that player's name."));
            return;
        }
        if (profile == targetProfile){
            sender.sendMessage(CC.translate("&cYou can't send credits to yourself."));
        } else {
            if (profile.getCredits() < amount){
                sender.sendMessage(CC.translate("&cInsufficient credits!"));
            } else {
                if (amount > 0){
                    targetProfile.setCredits(targetProfile.getCredits() + amount);
                    sender.sendMessage(CC.translate("&aSuccessfully sent &e" + targetProfile.getUsername() + " &b" + amount + " &acredits."));
                    if (Bukkit.getPlayer(targetProfile.getUuid()) != null){
                        Bukkit.getPlayer(targetProfile.getUuid()).sendMessage(CC.translate("&aYou've received &b" + amount + " &acredits from &e" + sender.getName() + "&a."));
                    }
                    profile.setCredits(profile.getCredits() - amount);
                } else {
                    sender.sendMessage(CC.translate("&cThe amount has to be greater than zero!"));
                }
            }
        }
    }

}
