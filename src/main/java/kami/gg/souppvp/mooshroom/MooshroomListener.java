package kami.gg.souppvp.mooshroom;

import kami.gg.souppvp.mooshroom.handler.MooshroomHandler;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.PlayerUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;

public class MooshroomListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof MushroomCow)) return;
        MushroomCow cow = (MushroomCow) event.getRightClicked();
        if (!cow.hasMetadata(MooshroomHandler.METADATA_KEY)) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        if (player.getInventory().firstEmpty() == -1) {

            PlayerUtil.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING);
            player.sendMessage(CC.translate("&cFull Inventory"));
            return;
        }

        player.getInventory().addItem(new ItemStack(Material.MUSHROOM_STEW, 1));
        player.updateInventory();
    }

    @EventHandler(ignoreCancelled = true)
    public void onShear(PlayerShearEntityEvent event) {
        if (!(event.getEntity() instanceof MushroomCow)) return;
        MushroomCow cow = (MushroomCow) event.getEntity();
        if (cow.hasMetadata(MooshroomHandler.METADATA_KEY)) {
            event.setCancelled(true);
        }
    }

    /** Hard guarantee that nothing damages a managed Mooshroom. */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof MushroomCow)) return;
        if (event.getEntity().hasMetadata(MooshroomHandler.METADATA_KEY)) {
            event.setCancelled(true);
        }
    }
}