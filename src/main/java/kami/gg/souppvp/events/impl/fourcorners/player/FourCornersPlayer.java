package kami.gg.souppvp.events.impl.fourcorners.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class FourCornersPlayer {

    @Getter private final UUID uuid;
    @Getter private final String username;
    @Getter @Setter private FourCornersPlayerState state = FourCornersPlayerState.WAITING;
    @Getter @Setter private int roundWins = 0;

    public FourCornersPlayer(Player player) {
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