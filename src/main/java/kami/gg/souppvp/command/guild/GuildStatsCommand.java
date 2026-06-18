package kami.gg.souppvp.command.guild;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.OptArg;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.Guild;
import kami.gg.souppvp.guild.GuildStats;
import kami.gg.souppvp.guild.GuildText;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /guild stats [guildName] — sums kills/deaths/credits/etc. across every
 * member of a guild (online + offline) via a single Mongo aggregation.
 */
public class GuildStatsCommand {

    @Command(name = "stats", desc = "Show aggregated stats for a guild", usage = "[guildName]")
    public void execute(@Sender CommandSender sender, @OptArg("") String guildName) {
        Guild guild;
        if (guildName == null || guildName.isEmpty()) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(GuildText.translate("&cConsole must pass a guild name."));
                return;
            }
            guild = SoupPvP.getInstance().getGuildsHandler().getByPlayer(p.getUniqueId());
            if (guild == null) {
                sender.sendMessage(GuildText.translate("&cYou aren't in a guild — try &e/guild stats <name>&c."));
                return;
            }
        } else {
            guild = SoupPvP.getInstance().getGuildsHandler().getByName(guildName);
            if (guild == null) {
                sender.sendMessage(GuildText.translate("&cNo guild called &e" + guildName + " &cexists."));
                return;
            }
        }

        GuildStats s = SoupPvP.getInstance().getGuildsHandler().aggregateStats(guild);

        sender.sendMessage(GuildText.translate("&8&m----------------------------------------"));
        sender.sendMessage(GuildText.translate("&dStats for " + guild.getColoredTag()));
        sender.sendMessage(GuildText.translate("&7Members: &f" + s.memberCount));
        sender.sendMessage(GuildText.translate("&7Total Kills: &a" + s.totalKills));
        sender.sendMessage(GuildText.translate("&7Total Deaths: &c" + s.totalDeaths));
        sender.sendMessage(GuildText.translate("&7Guild K/D: &e" + String.format("%.2f", s.averageKDR())));
        sender.sendMessage(GuildText.translate("&7Total Credits: &b" + s.totalCredits));
        sender.sendMessage(GuildText.translate("&7Highest Killstreak: &6" + s.highestKillstreak));
        sender.sendMessage(GuildText.translate("&7Total XP: &d" + s.totalExperiences));
        sender.sendMessage(GuildText.translate("&7Total Events Won: &5" + s.totalEventsWon));
        sender.sendMessage(GuildText.translate("&8&m----------------------------------------"));
    }
}