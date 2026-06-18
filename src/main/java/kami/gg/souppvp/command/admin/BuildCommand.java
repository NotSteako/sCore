package kami.gg.souppvp.command.admin;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.coinflip.menu.CoinFlipMenu;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * @author hieu
 * @date 28/09/2023
 */

public class BuildCommand {

    @Command(name = "", desc = "enter build mode")
    public void execute(@Sender CommandSender sender){
        Player player = (Player) sender;
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (player.hasMetadata("build")){
            player.removeMetadata("build", SoupPvP.getInstance());
            player.sendMessage(CC.translate("&cYou are no longer in builder mode."));
        } else {
            player.setMetadata("build", new FixedMetadataValue(SoupPvP.getInstance(), "build"));
            player.sendMessage(CC.translate("&aYou are now in builder mode."));
            player.setGameMode(GameMode.CREATIVE);
        }
    }

}
