package kami.gg.souppvp.command.guild;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.Guild;
import kami.gg.souppvp.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GuildListCommand {

    @Command(name = "list", desc = "List the members of your guild")
    public void execute(@Sender Player player) {
        Guild guild = SoupPvP.getInstance().getGuildsHandler().getByPlayer(player.getUniqueId());
        if (guild == null) { player.sendMessage(CC.translate("&cYou are not in a guild.")); return; }
        player.sendMessage(CC.translate("&8&m----------------------------------------"));
        player.sendMessage(CC.translate("&dMembers of " + guild.getColoredTag()));
        for (UUID uuid : guild.getMembers()) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            String label = (op.getName() == null) ? uuid.toString().substring(0, 8) : op.getName();
            Player online = Bukkit.getPlayer(uuid);
            String statusColor = (online != null && online.isOnline()) ? "&a" : "&7";
            String role = guild.isLeader(uuid) ? " &6(Leader)" : "";
            player.sendMessage(CC.translate(" " + statusColor + "• " + label + role));
        }
        player.sendMessage(CC.translate("&8&m----------------------------------------"));
    }
}