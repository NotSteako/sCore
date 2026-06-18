package kami.gg.souppvp.events.menu;

import kami.gg.souppvp.events.menu.button.ColourShuffleEventButton;
import kami.gg.souppvp.events.menu.button.FourCornersEventButton;
import kami.gg.souppvp.events.menu.button.SumoEventButton;
import kami.gg.souppvp.events.menu.button.RedRoverEventButton;

import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class HostEventsMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return CC.translate("Select an Event");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttonMap = new HashMap<>();
        buttonMap.put(10, new SumoEventButton());
        buttonMap.put(12, new ColourShuffleEventButton());
        buttonMap.put(14, new RedRoverEventButton());
        buttonMap.put(16, new FourCornersEventButton());
        buttonMap.put(13, Button.placeholder(Material.RED_STAINED_GLASS_PANE, (byte) 14, " "));
        buttonMap.put(28, Button.placeholder(Material.RED_STAINED_GLASS_PANE, (byte) 14, " "));
        buttonMap.put(31, Button.placeholder(Material.RED_STAINED_GLASS_PANE, (byte) 14, " "));
        buttonMap.put(34, Button.placeholder(Material.RED_STAINED_GLASS_PANE, (byte) 14, " "));
        setPlaceholder(true);
        return buttonMap;

    }




    @Override
    public int size(Map<Integer, Button> buttons) {
        return 45;
    }
}
