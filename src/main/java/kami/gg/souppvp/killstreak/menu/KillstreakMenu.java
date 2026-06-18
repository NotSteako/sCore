package kami.gg.souppvp.killstreak.menu;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.killstreak.Killstreak;
import kami.gg.souppvp.killstreak.menu.button.KillstreakButton;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.Menu;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class KillstreakMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return "Killstreaks";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        HashMap<Integer, Button> buttonHashMap = new HashMap<>();
        int i=10;
        for (Killstreak killstreak : SoupPvP.getInstance().getKillstreaksHandler().getKillstreaks()){
            if (i == 17) {
                i = 19;
            }
            buttonHashMap.put(i, new KillstreakButton(killstreak));
            i++;
        }
        setPlaceholder(true);
        return buttonHashMap;
    }

    @Override
    public int size(Map<Integer, Button> buttons) {
        return 36;
    }
}
