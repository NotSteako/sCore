package kami.gg.souppvp.listener;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.Cuboid;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnProtectionListener implements Listener {

    private final Map<UUID, Long> messageCooldown = new HashMap<>();

    private Cuboid getSpawn() {
        return SoupPvP.getInstance().getSpawnHandler().getCuboid();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPearlTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }

        if (!getSpawn().contains(event.getFrom()) && !getSpawn().contains(event.getTo())) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();

        // Refund the pearl that was consumed
        player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));

        long now = System.currentTimeMillis();
        long lastMessage = messageCooldown.getOrDefault(player.getUniqueId(), 0L);

        if (now - lastMessage >= 2000L) {
            player.sendMessage(CC.translate("&cCan't pearl back into &bSpawn&c."));
            messageCooldown.put(player.getUniqueId(), now);
        }
    }
}