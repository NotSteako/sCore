package kami.gg.souppvp.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.menu.KitsSelectMenu;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.tier.menu.TiersMenu;
import kami.gg.souppvp.util.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitCommand {

    @Command(name = "", desc = "open kit menu")
    public void execute(@Sender CommandSender sender){
        Player player = (Player) sender;

        if (!canKit(player)) {
            return;
        }

        new KitsSelectMenu().openMenu(player);
    }

    private boolean canKit(Player player) {
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
