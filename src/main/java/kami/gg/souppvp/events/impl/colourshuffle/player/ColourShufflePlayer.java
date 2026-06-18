package kami.gg.souppvp.events.impl.colourshuffle.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter @Setter
public class ColourShufflePlayer {

    private final UUID uuid;
    private final String username;
    private ColourShufflePlayerState state = ColourShufflePlayerState.WAITING;
    private int roundsSurvived = 0;

    public ColourShufflePlayer(Player player) {
        this.uuid = player.getUniqueId();
        this.username = player.getName();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }
}
