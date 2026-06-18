package kami.gg.souppvp.events.impl.redrover.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RedRoverPlayer {

    @Getter private final UUID uuid;
    @Getter private final String username;
    @Getter @Setter private RedRoverPlayerState state = RedRoverPlayerState.WAITING;
    @Getter @Setter private RedRoverTeam team = RedRoverTeam.NONE;
    @Getter @Setter private boolean captain = false;
    @Getter @Setter private int roundWins = 0;

    public RedRoverPlayer(Player player) {
        this.uuid = player.getUniqueId();
        this.username = player.getName();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public void incrementRoundWins() {
        this.roundWins++;
    }

}