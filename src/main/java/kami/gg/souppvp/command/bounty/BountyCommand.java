package kami.gg.souppvp.command.bounty;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.listener.LunarClientListener;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.refinedev.phoenix.Phoenix;

public class BountyCommand {

    @Command(name = "", desc = "Add to player's bounty", usage = "<player> <amount>")
    public void execute(@Sender CommandSender sender, Player target, int amount){
        Player player = (Player) sender;
        Profile setterProfile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        Profile targetProfile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(target.getUniqueId());

        Phoenix api = Phoenix.getInstance();
        xyz.refinedev.phoenix.profile.IProfile profile2 = api.getProfileHandler().getProfile(player.getUniqueId());

        if (targetProfile == null){
            sender.sendMessage(CC.translate("&cCouldn't resolve that player's name."));
            return;
        }

        if(player.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(CC.translate("&cYou cannot place a bounty on yourself."));
            return;
        }

        if (setterProfile.getCredits() < amount){
            sender.sendMessage(CC.translate("&cInsufficient credits!"));
        } else {
            if (amount <= 0){
                sender.sendMessage(CC.translate("&cThe amount has to be greater than zero!"));
            } else {
                Integer beforeBounty = targetProfile.getBounty();
                setterProfile.setCredits(setterProfile.getCredits() - amount);
                targetProfile.setBounty(amount + targetProfile.getBounty());
                Integer afterBounty = targetProfile.getBounty();
                Bukkit.broadcastMessage(CC.translate(profile2.getHighestRank().getColor() + sender.getName() + " &ehas upped the bounty on " + profile2.getHighestRank().getColor()  + targetProfile.getUsername() + " &eto &a" + targetProfile.getBounty() + " (+" + (afterBounty - beforeBounty) + ") &ecredits."));
                if (Bukkit.getPlayer(targetProfile.getUuid()) != null){
                    LunarClientListener.updateNametag(Bukkit.getPlayer(targetProfile.getUuid()));
                }
            }
        }
    }

}
