package kami.gg.souppvp.listener;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.editor.KitEditorHolder;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpawnListeners implements Listener {

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
            if (SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player) || profile.getProfileState() == ProfileState.SPAWN) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLeave(PlayerMoveEvent event) {
        // Optimization: only process if they moved to a different block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());

        // Must be in SPAWN state to care
        if (profile.getProfileState() != ProfileState.SPAWN) return;

        boolean wasInSpawn = SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(event.getFrom());
        boolean isInSpawn  = SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(event.getTo());

        // Trigger only on the exact moment they cross the boundary
        if (wasInSpawn && !isInSpawn) {
            player.removePotionEffect(PotionEffectType.SPEED);
            profile.setProfileState(ProfileState.COMBAT); // or whatever your active state is
            giveKit(player);
        }
    }


    private void giveKit(Player player) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName(profile.getCurrentKit().getName());
        kit.equipKit(player);
    }

    @EventHandler
    public void onPlayerMoveItem(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        // Allow interaction inside the Kit Editor (it has its own protection logic)
        if (event.getView().getTopInventory().getHolder() instanceof KitEditorHolder) {
            return;
        }

        if (event.getWhoClicked() instanceof Player) {
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getWhoClicked().getUniqueId());
            if (SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(event.getWhoClicked()) || profile.getProfileState() == ProfileState.SPAWN){
                event.setCancelled(true);
            }
        }
    }

}

