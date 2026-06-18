package kami.gg.souppvp.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.gui.WarpGUI;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.tier.menu.TiersMenu;
import kami.gg.souppvp.util.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpMenuCommand {

    @Command(name = "", desc = "warp menu opener")
    public void execute(@Sender CommandSender sender){

        Player player = (Player) sender;

        if (!canWarp(player)) {
            return;
        }

        WarpGUI.open(player);
    }

    private boolean canWarp(Player player) {
        Profile profile = SoupPvP.getInstance()
                .getProfilesHandler()
                .getProfileByUUID(player.getUniqueId());

        if (profile.isCombatTagged()) {
            player.sendMessage(CC.translate("&cYou're currently combat-tagged."));
            return false;
        }

        if (!SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player)) {
            player.sendMessage(CC.translate("&aYou have to be in &bSpawn&a."));
            return false;
        }

        return true;
    }

}
