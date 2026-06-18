package kami.gg.souppvp.command.guild;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.OptArg;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.Guild;
import kami.gg.souppvp.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.UUID;



public class GuildInfoCommand {

    @Command(name = "info", desc = "Inspect a guild's tag, leader and member count", usage = "[guildName]")


    public void execute(@Sender CommandSender sender, @OptArg("") String guildName) {

        Guild guild;
        if (guildName == null || guildName.isEmpty()) {
            if (!(sender instanceof Player p)) { sender.sendMessage(CC.translate("&cConsole must pass a guild name.")); return; }
            guild = SoupPvP.getInstance().getGuildsHandler().getByPlayer(p.getUniqueId());
            if (guild == null) {
                sender.sendMessage(CC.translate("&cYou aren't in a guild — try &e/guild info <name>&c."));
                return;
            }
        } else {
            guild = SoupPvP.getInstance().getGuildsHandler().getByName(guildName);
            if (guild == null) { sender.sendMessage(CC.translate("&cNo guild called &e" + guildName + " &cexists.")); return; }
        }
        OfflinePlayer leader = Bukkit.getOfflinePlayer(guild.getLeader());
        sender.sendMessage(CC.translate("&8&m----------------------------------------"));
        sender.sendMessage(CC.translate("&dGuild: " + guild.getColoredTag()));
        sender.sendMessage(CC.translate("&7Leader: &f" + (leader.getName() == null ? "Unknown" : leader.getName())));
        sender.sendMessage(CC.translate("&7Members: &f" + guild.getMembers().size()));
        int online = 0;
        for (UUID uuid : guild.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) online++;
        }
        sender.sendMessage(CC.translate("&7Online: &f" + online));
        sender.sendMessage(CC.translate("&7Tag colour: " + guild.getTag() + "(preview)&7"));
        sender.sendMessage(CC.translate("&8&m----------------------------------------"));
    }
}