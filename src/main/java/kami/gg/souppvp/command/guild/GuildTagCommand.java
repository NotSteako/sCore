package kami.gg.souppvp.command.guild;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.Guild;
import kami.gg.souppvp.guild.menu.GuildTagMenu;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class GuildTagCommand {

    @Command(name = "tag", desc = "Open the guild tag colour picker")
    public void execute(@Sender Player player) {
        Guild guild = SoupPvP.getInstance().getGuildsHandler().getByPlayer(player.getUniqueId());
        if (guild == null) { player.sendMessage(CC.translate("&cYou must be in a guild to change its tag.")); return; }
        if (!guild.isLeader(player.getUniqueId())) {
            player.sendMessage(CC.translate("&cOnly the guild leader can change the tag colour.")); return;
        }
        new GuildTagMenu().openMenu(player);
    }
}