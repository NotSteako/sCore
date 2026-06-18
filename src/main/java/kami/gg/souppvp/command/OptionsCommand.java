package kami.gg.souppvp.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.options.OptionsMenu;
import kami.gg.souppvp.profile.Profile;
import org.bukkit.entity.Player;

public class OptionsCommand {

    @Command(name = "", desc = "open the settings menu")
    public void execute(@Sender Player player){
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        new OptionsMenu().openMenu(player);
    }
}
