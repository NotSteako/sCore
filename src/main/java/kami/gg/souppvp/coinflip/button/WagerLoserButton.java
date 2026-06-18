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
public class WagerLoserButton extends Button {

    private CoinFlip coinFlip;

    public WagerLoserButton(CoinFlip coinFlip){
        this.coinFlip = coinFlip;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(CC.translate("&e&l" + Bukkit.getPlayer(coinFlip.getLoser()).getName() + "'s Lost:"));
        lore.add(CC.translate("&c• &a-" + coinFlip.getAmount() + " credits"));
        lore.add("");
        lore.add(CC.translate("&aA total of &e" + coinFlip.getAmount() + " &ahas"));
        lore.add(CC.translate("&abeen deducted from their account!"));
        return new ItemBuilder(Material.REDSTONE).name(CC.translate("&c&lLOSER")).lore(lore).build();
    }
}
