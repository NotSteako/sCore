package kami.gg.souppvp.tasks;

import kami.gg.souppvp.SoupPvP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class ClearDropsTask {

    public ClearDropsTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(SoupPvP.getInstance(), () -> {
            for (Entity event : Bukkit.getServer().getWorld("world").getEntities()) {
                if (event.getType().equals(EntityType.ITEM)) {
                    event.remove();
                }
            }
        }, 0L, (60 * 20));
    }

}
