package kami.gg.souppvp.guild;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Safe legacy + hex colour translator for guild UI strings.
 *
 * Independent of SoupPvP's CC class — your deployed CC.java calls
 * matcher.group(1) on a no-capture hex pattern, so any string containing
 * "#rrggbb" passed to CC.translate throws "No group 1". This helper does
 * the same job (& codes + hex codes) but always uses group(0).
 */
public final class GuildText {

    private static final Pattern HEX = Pattern.compile("#[A-Fa-f0-9]{6}");

    private GuildText() {}

    public static String translate(String input) {
        if (input == null) return "";
        Matcher m = HEX.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String hex = m.group();
            try {
                m.appendReplacement(sb, Matcher.quoteReplacement(ChatColor.of(hex).toString()));
            } catch (Throwable t) {
                m.appendReplacement(sb, Matcher.quoteReplacement(hex));
            }
        }
        m.appendTail(sb);
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', sb.toString());
    }
}