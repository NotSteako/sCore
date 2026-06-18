package kami.gg.souppvp.command.guild;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.Guild;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class GuildDenyCommand {

    @Command(name = "deny", desc = "Deny a pending guild invite", usage = "<guildName>")
    public void execute(@Sender Player player, String guildName) {
        Guild guild = SoupPvP.getInstance().getGuildsHandler().getByName(guildName);
        if (guild == null) { player.sendMessage(CC.translate("&cNo guild called &e" + guildName + " &cexists.")); return; }
        if (!guild.getPendingInvites().contains(player.getUniqueId())) {
            player.sendMessage(CC.translate("&cYou don't have a pending invite from that guild.")); return;
        }
        guild.getPendingInvites().remove(player.getUniqueId());
        SoupPvP.getInstance().getGuildsHandler().save(guild);
        player.sendMessage(CC.translate("&7You denied the invite from " + guild.getColoredTag() + "&7."));
    }
}