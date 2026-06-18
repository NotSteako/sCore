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

public class GuildAcceptCommand {

    @Command(name = "accept", desc = "Accept a pending guild invite", usage = "<guildName>")
    public void execute(@Sender Player player, String guildName) {
        Guild guild = SoupPvP.getInstance().getGuildsHandler().getByName(guildName);
        if (guild == null) { player.sendMessage(CC.translate("&cNo guild called &e" + guildName + " &cexists.")); return; }
        if (!guild.getPendingInvites().contains(player.getUniqueId())) {
            player.sendMessage(CC.translate("&cYou don't have a pending invite from that guild.")); return;
        }
        if (SoupPvP.getInstance().getGuildsHandler().getByPlayer(player.getUniqueId()) != null) {
            player.sendMessage(CC.translate("&cYou are already in a guild. Leave it first.")); return;
        }
        guild.getPendingInvites().remove(player.getUniqueId());
        guild.getMembers().add(player.getUniqueId());
        SoupPvP.getInstance().getGuildsHandler().save(guild);
        SoupPvP.getInstance().getGuildsHandler().setProfileGuild(player.getUniqueId(), guild.getName());

        player.sendMessage(CC.translate("&aYou joined " + guild.getColoredTag() + "&a!"));
        LunarClientListener.updateNametag(player);
        for (UUID uuid : guild.getMembers()) {
            if (uuid.equals(player.getUniqueId())) continue;
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(CC.translate("&a" + player.getName() + " &7joined " + guild.getColoredTag() + "&7."));
            }
        }
    }
}