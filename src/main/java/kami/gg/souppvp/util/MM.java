package kami.gg.souppvp.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class MM {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    public static Component parse(String input) {
        return MINI.deserialize(input);
    }

    public static String legacy(String input) {
        return MINI.serialize(MINI.deserialize(input));
    }
}