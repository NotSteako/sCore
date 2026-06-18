package kami.gg.souppvp.events.impl.redrover.task;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.redrover.RedRover;
import kami.gg.souppvp.events.impl.redrover.RedRoverState;
import kami.gg.souppvp.events.impl.redrover.RedRoverTask;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.PlayerUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class RedRoverRoundStartTask extends RedRoverTask {

    public RedRoverRoundStartTask(RedRover redRover) { super(redRover, RedRoverState.ROUND_STARTING); }

    @Override
    public void onRun() {
        if (getTicks() >= 3) {
            this.getRedRover().broadcastMessage(CC.translate("&cMatch Started!"));
            this.getRedRover().setEventTask(null);
            this.getRedRover().setState(RedRoverState.ROUND_FIGHTING);

            Player playerA = this.getRedRover().getRoundPlayerA().getPlayer();
            Player playerB = this.getRedRover().getRoundPlayerB().getPlayer();

            Location spawnA = SoupPvP.getInstance().getRedRoverHandler().getSpawnA();
            Location spawnB = SoupPvP.getInstance().getRedRoverHandler().getSpawnB();

            playerA.teleport(spawnA);
            playerB.teleport(spawnB);

            playerA.playSound(playerA.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
            PlayerUtil.allowMovement(playerA);

            playerB.playSound(playerB.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
            PlayerUtil.allowMovement(playerB);

            this.getRedRover().setRoundStart(System.currentTimeMillis());
        } else {
            int seconds = getSeconds();
            Player playerA = this.getRedRover().getRoundPlayerA().getPlayer();
            Player playerB = this.getRedRover().getRoundPlayerB().getPlayer();

            Location spawnA = SoupPvP.getInstance().getRedRoverHandler().getSpawnA();
            Location spawnB = SoupPvP.getInstance().getRedRoverHandler().getSpawnB();

            playerA.teleport(spawnA);
            playerB.teleport(spawnB);

            playerA.playSound(playerA.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
            PlayerUtil.denyMovement(playerA);

            playerB.playSound(playerB.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
            PlayerUtil.denyMovement(playerB);

            this.getRedRover().broadcastMessage("&7The round will be starting in &c" + seconds + "&7...");
        }
    }

}