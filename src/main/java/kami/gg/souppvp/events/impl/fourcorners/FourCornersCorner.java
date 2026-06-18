package kami.gg.souppvp.events.impl.fourcorners;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@AllArgsConstructor
@Getter
public enum FourCornersCorner {
    RED("Red", ChatColor.RED),
    BLUE("Blue", ChatColor.BLUE),
    YELLOW("Yellow", ChatColor.YELLOW),
    GREEN("Green", ChatColor.GREEN);

    private final String displayName;
    private final ChatColor color;
}