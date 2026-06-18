package kami.gg.souppvp.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.juggernaut.Juggernaut;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author hieu
 * @date 10/06/2023
 */
public class JuggernautCommand {

    @Command(name = "", desc = "start juggernaut event", usage = "<player>")
    @Require("souppvp.juggernaut")
    public void execute(@Sender CommandSender sender, Player player){
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (profile == null){
            sender.sendMessage(CC.translate("&cCouldn't resolve that player's name."));
        } else {
            if (profile.getProfileState() != ProfileState.SPAWN){
                sender.sendMessage(CC.translate("&cThat player that to be in spawn first."));
                return;
            }
            Juggernaut.setJuggernaut(Bukkit.getPlayer(profile.getUuid()));
        }
    }

}
