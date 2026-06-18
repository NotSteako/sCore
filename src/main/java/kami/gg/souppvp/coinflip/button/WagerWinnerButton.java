package kami.gg.souppvp.coinflip.button;

import kami.gg.souppvp.coinflip.CoinFlip;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.menu.Button;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class WagerWinnerButton extends Button {

    private CoinFlip coinFlip;

    public WagerWinnerButton(CoinFlip coinFlip){
        this.coinFlip = coinFlip;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(CC.translate("&e&l" + Bukkit.getPlayer(coinFlip.getWinner()).getName() + "'s Win:"));
        lore.add(CC.translate("&c• &a+" + (coinFlip.getAmount() * 2) + " credits"));
        lore.add("");
        lore.add(CC.translate("&aA total of &e" + (coinFlip.getAmount()*2) + " &ahas"));
        lore.add(CC.translate("&abeen added to their account!"));
        return new ItemBuilder(Material.EMERALD).name(CC.translate("&a&lWINNER")).lore(lore).build();
    }
}
