package kami.gg.souppvp.command.guild;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.GuildText;
import kami.gg.souppvp.guild.gui.GuildFightGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /guild fight — opens the Guild Fight selector GUI.
 */
public class GuildFightCommand {

    @Command(name = "fight", desc = "Open the guild fight selector")
    public void execute(@Sender CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(GuildText.translate("&cOnly players can use this command."));
            return;
        }
        new GuildFightGUI(SoupPvP.getInstance().getGuildsHandler(), player).open(player);
    }
}