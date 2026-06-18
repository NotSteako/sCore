package kami.gg.souppvp.tasks;

import kami.gg.souppvp.SoupPvP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClearTimerCacheTask {

    public ClearTimerCacheTask(){
        Bukkit.getScheduler().scheduleSyncRepeatingTask(SoupPvP.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()){
                UUID uuid = player.getUniqueId();
                if (!SoupPvP.getInstance().getCombatTagsHandler().getCombatTags().isEmpty()){
                    if (SoupPvP.getInstance().getCombatTagsHandler().getCombatTags().containsKey(uuid)){
                        if (SoupPvP.getInstance().getCombatTagsHandler().getCombatTags().get(uuid) < System.currentTimeMillis()){
                            SoupPvP.getInstance().getCombatTagsHandler().getCombatTags().remove(uuid);
                        }
                    }
                }
                if (!SoupPvP.getInstance().getSpawnTeleportationHandler().getSpawnTeleporataion().isEmpty()) {
                    if (SoupPvP.getInstance().getSpawnTeleportationHandler().getSpawnTeleporataion().containsKey(uuid)) {
                        if (SoupPvP.getInstance().getSpawnTeleportationHandler().getSpawnTeleporataion().get(uuid) < System.currentTimeMillis()){
                            SoupPvP.getInstance().getSpawnTeleportationHandler().getSpawnTeleporataion().remove(uuid);
                        }
                    }
                }
                SoupPvP.getInstance().getTimersHandler().removeAllPlayerTimers(uuid);
            }

        }, 0L, 20L * 60);
    }

}
