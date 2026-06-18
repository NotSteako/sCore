package kami.gg.souppvp.tasks;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.listener.LunarClientListener;
import kami.gg.souppvp.perk.Perk;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.TaskUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class CanaPerkAndFiremanKitTask {

    public CanaPerkAndFiremanKitTask(){
        Bukkit.getScheduler().scheduleSyncRepeatingTask(SoupPvP.getInstance(), () -> {
            Perk canaPerk = SoupPvP.getInstance().getPerksHandler().getPerkByName("Cana");
            Kit firemanKit = SoupPvP.getInstance().getKitsHandler().getKitByName("Fireman");
            for (Player player : Bukkit.getOnlinePlayers()){
                Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
                if (player.getLocation().getBlock().getType() == Material.WATER || player.getLocation().getBlock().getType() == Material.WATER){
                    if (SoupPvP.getInstance().getPerksHandler().getPerkByName(profile.getActivePerks().get(2)) == canaPerk || profile.getCurrentKit() == firemanKit){
                        player.damage(2);
                        TaskUtil.runLater(() -> {
                            LunarClientListener.updateNametag(player);
                        }, 1L);
                    }
                }
            }
        }, 0L, 15L);
    }

}
