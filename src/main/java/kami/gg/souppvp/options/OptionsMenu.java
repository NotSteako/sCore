package kami.gg.souppvp.options;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.options.button.*;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.Menu;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class OptionsMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return CC.translate("Configure your settings");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        Map<Integer, Button> buttonMap = new HashMap<>();
        buttonMap.put(10, new KillDeathMessagesButton());
        buttonMap.put(11, new KillstreakMessagesButton());
        buttonMap.put(12, new ScoreboardButton());
        buttonMap.put(13, new ResetStatisticsButton(10000));
        setPlaceholder(true);
        return buttonMap;
    }

    @Override
    public int size(Map<Integer, Button> buttons) {
        return 27;
    }

}
