package kami.gg.souppvp.util;

import kami.gg.souppvp.SoupPvP;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class XPBarTimer {

    private static final Map<Player, BukkitTask> runnables = new HashMap<>();

    public static void runXpBar(Player player, int cooldown) {

        // Cancel any existing bar for this player so we always start fresh
        BukkitTask old = runnables.remove(player);
        if (old != null) {
            old.cancel();
        }

        player.setLevel(cooldown);
        player.setExp(1F);

        BukkitTask task = new BukkitRunnable() {

            final long time = System.currentTimeMillis() + cooldown * 1000L;

            @Override
            public void run() {

                if (!runnables.containsKey(player)) {
                    cancel();

                    player.setLevel(0);
                    player.setExp(0F);

                    return;
                }

                if (System.currentTimeMillis() >= this.time) {

                    cancel();

                    player.setLevel(0);
                    player.setExp(0F);

                    runnables.remove(player);

                    return;
                }

                long remaining = this.time - System.currentTimeMillis();

                player.setLevel((int) Math.ceil(remaining / 1000.0));
                player.setExp(remaining / (cooldown * 1000F));
            }

        }.runTaskTimer(SoupPvP.getInstance(), 1L, 1L);

        runnables.put(player, task);
    }

    public static void remove(Player player) {

        BukkitTask task = runnables.remove(player);

        if (task != null) {
            task.cancel();
        }

        player.setLevel(0);
        player.setExp(0F);
    }
}