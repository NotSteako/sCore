package kami.gg.souppvp.command.guild;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.Guild;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class GuildInviteCommand {

    @Command(name = "invite", desc = "Invite a player to your guild", usage = "<player>")
    public void execute(@Sender Player player, Player target) {
        Guild guild = SoupPvP.getInstance().getGuildsHandler().getByPlayer(player.getUniqueId());
        if (guild == null) { player.sendMessage(CC.translate("&cYou are not in a guild.")); return; }
        if (!guild.isLeader(player.getUniqueId())) {
            player.sendMessage(CC.translate("&cOnly the guild leader can invite new members.")); return;
        }
        if (target == null || !target.isOnline()) { player.sendMessage(CC.translate("&cThat player isn't online.")); return; }
        if (target.getUniqueId().equals(player.getUniqueId())) { player.sendMessage(CC.translate("&cYou can't invite yourself.")); return; }
        if (guild.isMember(target.getUniqueId())) { player.sendMessage(CC.translate("&cThey're already in your guild.")); return; }
        if (SoupPvP.getInstance().getGuildsHandler().getByPlayer(target.getUniqueId()) != null) {
            player.sendMessage(CC.translate("&cThat player is already in another guild.")); return;
        }
        if (guild.getPendingInvites().contains(target.getUniqueId())) {
            player.sendMessage(CC.translate("&cThey already have a pending invite from you.")); return;
        }
        guild.getPendingInvites().add(target.getUniqueId());
        SoupPvP.getInstance().getGuildsHandler().save(guild);
        player.sendMessage(CC.translate("&aYou invited &f" + target.getName() + " &ato " + guild.getColoredTag() + "&a."));
        target.sendMessage(CC.translate("&eYou've been invited to join " + guild.getColoredTag() + "&e!"));
        target.sendMessage(CC.translate("&7Type &a/guild accept " + guild.getName()
                + " &7to join, or &c/guild deny " + guild.getName() + " &7to decline."));
    }
}
