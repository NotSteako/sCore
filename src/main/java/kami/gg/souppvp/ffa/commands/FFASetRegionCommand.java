package kami.gg.souppvp.ffa.commands;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.ffa.FFACommand;
import org.bukkit.entity.Player;

public class FFASetRegionCommand {

    @Command(name = "setregion", desc = "Set the FFA safezone")
    @Require("souppvp.admin")
    public void execute(@Sender Player player) {

        if (FFACommand.getPos1() == null || FFACommand.getPos2() == null) {
            player.sendMessage("§cYou must select both positions first.");
            return;
        }

        FFACommand.setRegion(
                FFACommand.getPos1(),
                FFACommand.getPos2()
        );

        player.sendMessage("§aFFA safezone region has been saved.");
    }
}