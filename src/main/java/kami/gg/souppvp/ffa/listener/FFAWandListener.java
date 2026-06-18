package kami.gg.souppvp.ffa.listener;

import kami.gg.souppvp.ffa.FFACommand;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class FFAWandListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        if (event.getItem() == null) {
            return;
        }

        if (event.getItem().getType() != Material.GOLDEN_AXE) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            FFACommand.setPos1(event.getClickedBlock().getLocation());
            event.getPlayer().sendMessage("§aPosition 1 set.");
            event.setCancelled(true);
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            FFACommand.setPos2(event.getClickedBlock().getLocation());
            event.getPlayer().sendMessage("§aPosition 2 set.");
            event.setCancelled(true);
        }
    }
}