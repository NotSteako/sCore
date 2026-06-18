package kami.gg.souppvp.command.guild;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.Guild;
import kami.gg.souppvp.listener.LunarClientListener;
import kami.gg.souppvp.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GuildLeaveCommand {

    @Command(name = "leave", desc = "Leave your current guild")
    public void execute(@Sender Player player) {
        Guild guild = SoupPvP.getInstance().getGuildsHandler().getByPlayer(player.getUniqueId());
        if (guild == null) { player.sendMessage(CC.translate("&cYou are not in a guild.")); return; }
        if (guild.isLeader(player.getUniqueId())) {
            player.sendMessage(CC.translate("&cYou're the leader — use &e/guild disband &cor transfer leadership first."));
            return;
        }
        guild.getMembers().remove(player.getUniqueId());
        SoupPvP.getInstance().getGuildsHandler().save(guild);
        SoupPvP.getInstance().getGuildsHandler().setProfileGuild(player.getUniqueId(), null);

        player.sendMessage(CC.translate("&eYou left " + guild.getColoredTag() + "&e."));
        LunarClientListener.updateNametag(player);
        for (UUID uuid : guild.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(CC.translate("&7" + player.getName() + " has left " + guild.getColoredTag() + "&7."));
            }
        }
    }
}