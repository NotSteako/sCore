package kami.gg.souppvp.coinflip.menu.wager;

import kami.gg.souppvp.coinflip.CoinFlip;
import kami.gg.souppvp.coinflip.button.WagerLoserButton;
import kami.gg.souppvp.coinflip.button.WagerWinnerButton;
import kami.gg.souppvp.coinflip.menu.CoinFlipMenu;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.Menu;
import kami.gg.souppvp.util.menu.button.BackButton;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class WagerResultMenu extends Menu {

    private CoinFlip coinFlip;

    public WagerResultMenu(CoinFlip coinFlip){
        this.coinFlip = coinFlip;
    }

    @Override
    public String getTitle(Player player) {
        return CC.translate("&a&lCoinflip &7- " + (coinFlip.getWinner().equals(player.getUniqueId()) ? "&a&lYOU WON!!" : "&c&lYOU LOST!!"));
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttonMap = new HashMap<>();
        buttonMap.put(11, new WagerWinnerButton(coinFlip));
        buttonMap.put(13, new BackButton(new CoinFlipMenu()));
        buttonMap.put(15, new WagerLoserButton(coinFlip));

        for (int i=0; i<27; i++) {
            buttonMap.putIfAbsent(i, Button.placeholder(Material.RED_STAINED_GLASS_PANE, (byte) 14, " "));
        }
        return buttonMap;
    }

    @Override
    public int size(Map<Integer, Button> buttons) {
        return 27;
    }
}
