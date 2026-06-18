package kami.gg.souppvp.coinflip.menu.wager;

import kami.gg.souppvp.coinflip.CoinFlip;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Topu
 * @date 6/6/2023
 */
public class WinnerMenu extends Menu {

    private CoinFlip coinFlip;

    public WinnerMenu(CoinFlip coinFlip){
        this.coinFlip = coinFlip;
        setPlaceholder(true);
    }

    @Override
    public String getTitle(Player player) {
        return CC.translate("&a&lCoinflip &7- &a&lYOU WON!!");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        HashMap<Integer, Button> buttonHashMap = new HashMap<>();
        buttonHashMap.put(13, new Button() {

            @Override
            public ItemStack getButtonItem(Player player) {
                ItemStack stack = new ItemBuilder(Material.PLAYER_HEAD)
                        .durability((short) 3)
                        .name(CC.translate("&a&l" + Bukkit.getPlayer(coinFlip.getWinner()).getName()))
                        .lore(Collections.singletonList("")).build();
                return stack;
            }

        });
        for (int i=0; i<27; i++) {
            if (i == 3 || i == 4 || i == 5 || i == 12 || i == 14 || i == 21 || i == 22 || i == 23){
                buttonHashMap.put(i, Button.placeholder(Material.RED_STAINED_GLASS_PANE, (byte) 5, " "));
            }
        }
        return buttonHashMap;
    }
}