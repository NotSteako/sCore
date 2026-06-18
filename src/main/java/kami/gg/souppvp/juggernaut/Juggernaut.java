package kami.gg.souppvp.juggernaut;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.listener.LunarClientListener;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author hieu
 * @date 10/06/2023
 */
public class Juggernaut {

    public static void setJuggernaut(Player player){
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        profile.setJuggernaut(true);
        profile.setBounty(profile.getBounty() + 5000);
        player.getInventory().clear();
        player.getInventory().setArmorContents(getArmor);
        player.getInventory().setItem(0, new ItemBuilder(Material.DIAMOND_SWORD).enchantment(Enchantment.SHARPNESS, 5).enchantment(Enchantment.UNBREAKING, 10).enchantment(Enchantment.FIRE_ASPECT, 2).build());
        player.getInventory().setItem(1, new ItemBuilder(Material.ENDER_PEARL).amount(32).build());
        player.getInventory().setItem(2, new ItemBuilder(Material.STICK).enchantment(Enchantment.KNOCKBACK, 5).build());
        player.getInventory().setItem(8, new ItemBuilder(Material.GOLDEN_APPLE).amount(32).build());
        PlayerUtil.giveSoup(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        Bukkit.broadcastMessage(CC.translate("&a" + player.getName() + " &eis now a Juggernaut, kill them to receive their bounty of &a" + profile.getBounty() + " &ecredits!"));
        LunarClientListener.updateNametag(player);
    }

    private static ItemStack[] getArmor = new ItemStack[]{
            new ItemBuilder(Material.DIAMOND_BOOTS)
                    .enchantment(Enchantment.PROTECTION, 15)
                    .enchantment(Enchantment.UNBREAKING, 10)
                    .enchantment(Enchantment.FEATHER_FALLING, 20)
                    .build(),
            new ItemBuilder(Material.DIAMOND_LEGGINGS)
                    .enchantment(Enchantment.PROTECTION, 15)
                    .enchantment(Enchantment.UNBREAKING, 10)
                    .build(),
            new ItemBuilder(Material.DIAMOND_CHESTPLATE)
                    .enchantment(Enchantment.PROTECTION, 15)
                    .enchantment(Enchantment.UNBREAKING, 10)
                    .build(),
            new ItemBuilder(Material.DIAMOND_HELMET)
                    .enchantment(Enchantment.PROTECTION, 15)
                    .enchantment(Enchantment.UNBREAKING, 10)
                    .build()

    };

}
