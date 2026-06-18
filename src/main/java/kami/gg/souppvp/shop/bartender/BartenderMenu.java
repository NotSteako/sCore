package kami.gg.souppvp.shop.bartender;

import kami.gg.souppvp.shop.bartender.button.PotionOfFireResistanceButton;
import kami.gg.souppvp.shop.bartender.button.SplashPotionOfHarmingButton;
import kami.gg.souppvp.shop.bartender.button.SplashPotionOfPoisonButton;
import kami.gg.souppvp.shop.bartender.button.SplashPotionOfSlownessButton;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.Menu;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BartenderMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return CC.translate("Select a potion to buy");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttonMap = new HashMap<>();
        buttonMap.put(10, new PotionOfFireResistanceButton(250));
        buttonMap.put(12, new SplashPotionOfHarmingButton(500));
        buttonMap.put(14, new SplashPotionOfPoisonButton(750));
        buttonMap.put(16, new SplashPotionOfSlownessButton(1000));
        setPlaceholder(true);
        return buttonMap;
    }

    @Override
    public int size(Map<Integer, Button> buttons) {
        return 27;
    }
}
