package kami.gg.souppvp.ffa.commands;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FFAWandCommand {

    @Command(name = "wand", desc = "Get the FFA region wand")
    @Require("souppvp.admin")
    public void execute(@Sender Player player) {

        player.getInventory().addItem(new ItemStack(Material.GOLDEN_AXE));

        player.sendMessage("§aYou have been given the FFA wand.");
        player.sendMessage("§7Left click a block to set Position 1.");
        player.sendMessage("§7Right click a block to set Position 2.");
    }
}