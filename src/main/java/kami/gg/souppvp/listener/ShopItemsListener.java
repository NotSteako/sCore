package kami.gg.souppvp.listener;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.TasksUtility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class ShopItemsListener implements Listener {

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event){
        Player player = event.getPlayer();
        if (SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player)){
            if (event.getItem().isSimilar(new ItemStack(Material.MILK_BUCKET))){
                event.setCancelled(true);
            }
        } else {
            TasksUtility.runTaskLater(() -> {
                Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
                Kit kit = profile.getCurrentKit();
                for (PotionEffect potionEffect : kit.getPotionEffects()){
                    player.addPotionEffect(potionEffect);
                }
            }, 2L);
        }
    }

}
