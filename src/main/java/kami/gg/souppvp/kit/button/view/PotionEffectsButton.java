package kami.gg.souppvp.kit.button.view;

import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.menu.Button;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class PotionEffectsButton extends Button {

    private Kit kit;

    public PotionEffectsButton(Kit kit){
        this.kit = kit;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        for (PotionEffect potionEffect : kit.getPotionEffects()){
            lore.add(CC.translate("&7• &f" + WordUtils.capitalize(potionEffect.getType().getName().replaceAll("_", " ").toLowerCase() + ": &b" + (potionEffect.getAmplifier() + 1))));
        }
        return new ItemBuilder(Material.POTION).name(CC.translate("&bPotion Effects:")).lore(lore).durability(8227).build();
    }
}

