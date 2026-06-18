package kami.gg.souppvp.command.guild;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.Guild;
import kami.gg.souppvp.guild.GuildText;
import kami.gg.souppvp.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuildCommand {

    @Command(name = "", desc = "Guild — usage: /guild help")
    public void execute(@Sender CommandSender sender) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(CC.translate("&cOnly players can use guild commands."));
            return;
        }
        Guild guild = SoupPvP.getInstance().getGuildsHandler().getByPlayer(p.getUniqueId());
        sender.sendMessage(CC.translate("&8&m----------------------------------------"));
        sender.sendMessage(CC.translate("&d&lGUILD &7commands"));
        sender.sendMessage("");
        sender.sendMessage(CC.translate("  &d/guild create <name> &7- Found a new guild"));
        sender.sendMessage(CC.translate("  &d/guild tag &7- Open the tag colour picker"));
        sender.sendMessage(CC.translate("  &d/guild invite <player> &7- Invite a player"));
        sender.sendMessage(CC.translate("  &d/guild accept <name> &7- Accept an invite"));
        sender.sendMessage(CC.translate("  &d/guild deny <name> &7- Deny an invite"));
        sender.sendMessage(CC.translate("  &d/guild kick <player> &7- Kick a member (leader)"));
        sender.sendMessage(CC.translate("  &d/guild leave &7- Leave your guild"));
        sender.sendMessage(CC.translate("  &d/guild disband &7- Disband your guild (leader)"));
        sender.sendMessage(CC.translate("  &d/guild list &7- Show your guild's members"));
        sender.sendMessage(CC.translate("  &d/guild info [name] &7- Inspect a guild"));
        sender.sendMessage(GuildText.translate("  &d/guild stats [name] &7- Guild stats"));
        if (guild != null) {
            sender.sendMessage("");
            sender.sendMessage(CC.translate("&7You are in " + guild.getColoredTag()
                    + " &7(" + guild.getMembers().size() + " members)"));
            OfflinePlayer leader = Bukkit.getOfflinePlayer(guild.getLeader());
            sender.sendMessage(CC.translate("&7Leader: &f" + (leader.getName() == null ? "Unknown" : leader.getName())));
        } else {
            sender.sendMessage("");
            sender.sendMessage(CC.translate("&7You are not currently in a guild."));
        }
        sender.sendMessage(CC.translate("&8&m----------------------------------------"));
    }
}