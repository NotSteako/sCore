package kami.gg.souppvp.tier.menu;

import kami.gg.souppvp.tier.Tiers;
import kami.gg.souppvp.tier.button.TierButton;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.Menu;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TiersMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return CC.translate("Viewing all available tiers");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttonMap = new HashMap<>();
        for (Tiers tier : Tiers.values()){
            buttonMap.put(buttonMap.size(), new TierButton(tier));
        }
        setPlaceholder(true);
        return buttonMap;
    }

    @Override
    public int size(Map<Integer, Button> buttons) {
        return 45;
    }
}
