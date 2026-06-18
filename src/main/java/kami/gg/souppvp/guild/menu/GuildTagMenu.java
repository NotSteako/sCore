package kami.gg.souppvp.guild.menu;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.Guild;
import kami.gg.souppvp.guild.GuildText;
import kami.gg.souppvp.guild.menu.button.GuildColorButton;
import kami.gg.souppvp.guild.menu.button.GuildCustomTagButton;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class GuildTagMenu extends Menu {

    public static final LinkedHashMap<String, Material> COLORS = new LinkedHashMap<>();
    static {
        COLORS.put("&0", Material.BLACK_WOOL);
        COLORS.put("&1", Material.BLUE_WOOL);
        COLORS.put("&2", Material.GREEN_WOOL);
        COLORS.put("&3", Material.CYAN_WOOL);
        COLORS.put("&4", Material.RED_WOOL);
        COLORS.put("&5", Material.PURPLE_WOOL);
        COLORS.put("&6", Material.ORANGE_WOOL);
        COLORS.put("&7", Material.LIGHT_GRAY_WOOL);
        COLORS.put("&8", Material.GRAY_WOOL);
        COLORS.put("&9", Material.LIGHT_BLUE_WOOL);
        COLORS.put("&a", Material.LIME_WOOL);
        COLORS.put("&b", Material.LIGHT_BLUE_WOOL);
        COLORS.put("&c", Material.PINK_WOOL);
        COLORS.put("&d", Material.MAGENTA_WOOL);
        COLORS.put("&e", Material.YELLOW_WOOL);
        COLORS.put("&f", Material.WHITE_WOOL);
    }

    @Override public String getTitle(Player player) { return GuildText.translate("&dGuild Tag Colour"); }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        Guild guild = SoupPvP.getInstance().getGuildsHandler().getByPlayer(player.getUniqueId());

        int[] slotsRow1 = {10, 11, 12, 13, 14, 15, 16};
        int[] slotsRow2 = {19, 20, 21, 22, 23, 24, 25};

        String[] codes = COLORS.keySet().toArray(new String[0]);
        Material[] mats = COLORS.values().toArray(new Material[0]);

        for (int i = 0; i < 7; i++)
            buttons.put(slotsRow1[i], new GuildColorButton(codes[i], mats[i], guild));
        for (int i = 0; i < 7; i++)
            buttons.put(slotsRow2[i], new GuildColorButton(codes[7 + i], mats[7 + i], guild));
        buttons.put(28, new GuildColorButton(codes[14], mats[14], guild)); // &e
        buttons.put(29, new GuildColorButton(codes[15], mats[15], guild)); // &f
        buttons.put(31, new GuildCustomTagButton(guild));

        setPlaceholder(true);
        return buttons;
    }

    @Override public int size(Map<Integer, Button> buttons) { return 45; }
}