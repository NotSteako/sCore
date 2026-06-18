package kami.gg.souppvp.events.impl.redrover.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@AllArgsConstructor
@Getter
public enum RedRoverTeam {

    NONE("None", ChatColor.GRAY),
    RED("Red", ChatColor.RED),
    BLUE("Blue", ChatColor.AQUA);

    private final String readable;
    private final ChatColor color;

}