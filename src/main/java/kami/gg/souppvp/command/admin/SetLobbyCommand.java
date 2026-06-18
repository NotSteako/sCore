package kami.gg.souppvp.command.admin;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.LocationUtil;
import kami.gg.souppvp.util.PlayerUtil;
import org.bukkit.entity.Player;

public class SetLobbyCommand {

    @Command(name = "", desc = "set the lobby location to where you stand and teleport you to it")
    public void execute(@Sender Player player) {
        if (!player.hasPermission("souppvp.admin")) {
            player.sendMessage(CC.translate("&cYou do not have permission to use this command."));
            return;
        }

        String serialized = LocationUtil.serialize(player.getLocation());
        SoupPvP.getInstance().getConfig().set("LOBBY", serialized);
        SoupPvP.getInstance().saveConfig();

        PlayerUtil.teleportToLobby(player);
        player.sendMessage(CC.translate("&aLobby location set to your current position."));
    }

}