package kami.gg.souppvp.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.editor.KitEditorMenu;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitEditorCommand {

    @Command(name = "", desc = "Edit your current kit's hotbar layout")
    public void execute(@Sender CommandSender sender) {
        Player player = (Player) sender;
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());

        if (profile == null) {
            player.sendMessage(CC.translate("&cYour profile isn't loaded yet."));
            return;
        }
        Kit kit = profile.getCurrentKit();
        if (kit == null) {
            player.sendMessage(CC.translate("&cYou don't have a kit selected. Pick one first!"));
            return;
        }
        KitEditorMenu.open(player, kit);
    }
}