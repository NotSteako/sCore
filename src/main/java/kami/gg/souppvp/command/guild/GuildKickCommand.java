package kami.gg.souppvp.command.guild;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.Guild;
import kami.gg.souppvp.listener.LunarClientListener;
import kami.gg.souppvp.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GuildKickCommand {

    @Command(name = "kick", desc = "Kick a member from your guild", usage = "<playerName>")
    public void execute(@Sender Player player, String targetName) {
        Guild guild = SoupPvP.getInstance().getGuildsHandler().getByPlayer(player.getUniqueId());
        if (guild == null) { player.sendMessage(CC.translate("&cYou are not in a guild.")); return; }
        if (!guild.isLeader(player.getUniqueId())) {
            player.sendMessage(CC.translate("&cOnly the guild leader can kick members.")); return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID targetUuid = target.getUniqueId();
        if (targetUuid == null || !guild.isMember(targetUuid)) {
            player.sendMessage(CC.translate("&cThat player isn't in your guild.")); return;
        }
        if (guild.isLeader(targetUuid)) {
            player.sendMessage(CC.translate("&cYou can't kick yourself. Use &e/guild disband&c instead.")); return;
        }
        guild.getMembers().remove(targetUuid);
        SoupPvP.getInstance().getGuildsHandler().save(guild);
        SoupPvP.getInstance().getGuildsHandler().setProfileGuild(targetUuid, null);

        player.sendMessage(CC.translate("&aYou kicked &f" + target.getName() + " &afrom the guild."));
        Player online = Bukkit.getPlayer(targetUuid);
        if (online != null && online.isOnline()) {
            online.sendMessage(CC.translate("&cYou were kicked from " + guild.getColoredTag() + "&c."));
            LunarClientListener.updateNametag(online);
        }
        for (UUID uuid : guild.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline() && !p.getUniqueId().equals(player.getUniqueId())) {
                p.sendMessage(CC.translate("&7" + target.getName() + " was kicked from " + guild.getColoredTag() + "&7."));
            }
        }
    }
}