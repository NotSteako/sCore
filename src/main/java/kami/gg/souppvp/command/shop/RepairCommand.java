package kami.gg.souppvp.command.shop;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.PlayerUtil;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RepairCommand {

    @Command(name = "", desc = "repair armor")
    public void execute(@Sender CommandSender sender) {
        Player player = (Player) sender;
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (profile.isJuggernaut()){
            sender.sendMessage(CC.translate("&cYou may not repair whilst in Juggernaut."));
            return;
        }
        if (SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player)){
            PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK);
            player.sendMessage(CC.translate("&cYou can't do this in spawn."));
        } else {
            if (profile.getCredits() >= 150){
                PlayerUtil.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP);
                profile.setCredits(profile.getCredits() - 150);
                PlayerUtil.repairPlayer(player);
                player.sendMessage(CC.translate("&aSuccessfully bought the &dRepair Durability &afeature."));
            } else {
                player.sendMessage(CC.translate("&cInsufficient funds you need &b150 &cCredits. &7(" + profile.getCredits() + "&7)"));
                PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK);
            }
        }
    }

}
