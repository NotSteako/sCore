package kami.gg.souppvp.command.leave;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.PlayerUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OPLeaveCommand {

    @Command(name = "", desc = "teleport to spawn")
    @Require("souppvp.opleave")
    public void execute(@Sender CommandSender sender){
        Player player = (Player) sender;
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (profile.getSumoEvent() == null){
            PlayerUtil.teleportToLobby(player);
            PlayerUtil.resetPlayer(player);
        } else {
            player.sendMessage(CC.translate("&cYou're currently in a sumo event."));
        }
    }

}
