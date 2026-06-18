package kami.gg.souppvp.command.guild;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.Guild;
import kami.gg.souppvp.listener.LunarClientListener;
import kami.gg.souppvp.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuildDisbandCommand {

    @Command(name = "disband", desc = "Disband your guild (leader only)")
    public void execute(@Sender Player player) {
        Guild guild = SoupPvP.getInstance().getGuildsHandler().getByPlayer(player.getUniqueId());
        if (guild == null) { player.sendMessage(CC.translate("&cYou are not in a guild.")); return; }
        if (!guild.isLeader(player.getUniqueId())) {
            player.sendMessage(CC.translate("&cOnly the guild leader can disband the guild."));
            return;
        }
        List<UUID> snapshot = new ArrayList<>(guild.getMembers());
        String coloredTag = guild.getColoredTag();
        SoupPvP.getInstance().getGuildsHandler().disband(guild);
        for (UUID uuid : snapshot) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(CC.translate("&eYour guild " + coloredTag + " &ehas been disbanded."));
                LunarClientListener.updateNametag(p);
            }
        }
    }
}