
package kami.gg.souppvp.command.shop;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.PlayerUtil;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;

public class CreditsCommand {
    @Command(name = "help",desc = "Credit utilities", usage = "<player> <amount>")
    public void execute(@Sender CommandSender sender) {
        Player player = (Player) sender;
        player.sendMessage(CC.translate("&6Credits Command:"));
        player.sendMessage(CC.translate("&e/credit pay <player> <amount> &f- &7Pays a player a certain amount of credits"));
        player.sendMessage(CC.translate("&e/credit set <player> <amount> &f- &7Sets a player's amount of credits"));
        player.sendMessage(CC.translate("&e/credit add <player> <amount> &f- &7Adds to the player's amount of credits"));
    }

}