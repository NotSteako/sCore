package kami.gg.souppvp.killstreak.inherit;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.killstreak.Killstreak;
import kami.gg.souppvp.killstreak.KillstreakReward;
import kami.gg.souppvp.perk.Perk;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class TwentyKillstreak extends Killstreak implements Listener {

    @Override
    public String getName() {
        return "Grandma Soups";
    }

    @Override
    public int getRequired() {
        return 20;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.MUSHROOM_STEW)
                .enchantment(Enchantment.UNBREAKING, 1)
                .name(CC.translate("&a" + getName()))
                .lore(Arrays.asList(CC.MENU_BAR, CC.translate("&7Gives you 2 Grandma Soups, that will"), CC.translate("&7instantly give maximum health on consumption."), CC.MENU_BAR, "", CC.translate("&fKillstreak Required: &d" + getRequired()), "")).build();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeathEvent(PlayerDeathEvent event){
        if (event.getEntity().getKiller() == null) return;
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getEntity().getKiller().getUniqueId());
        Perk hardlinePerk = SoupPvP.getInstance().getPerksHandler().getPerkByName("Hardline");
        if (SoupPvP.getInstance().getPerksHandler().getPerkByName(profile.getActivePerks().get(1)) == hardlinePerk){
            if (profile.getCurrentKillstreak() == getRequired()-1){
                event.getEntity().getKiller().sendMessage(CC.translate("&aYou've received the &d" + getName() + " &aperk for reaching a &d" + getRequired() + " &akillstreak!"));
                if (event.getEntity().getKiller().getInventory().firstEmpty() == -1){
                    for (int i=0; i<event.getEntity().getKiller().getInventory().getSize(); i++){
                        ItemStack itemstack = event.getEntity().getKiller().getInventory().getItem(i);
                        if (itemstack != null) {
                            if (itemstack.getType() == Material.BOWL || itemstack.getType() == Material.MUSHROOM_STEW) {
                                event.getEntity().getKiller().getInventory().setItem(i, KillstreakReward.GRANDMA_SOUP);
                                return;
                            }
                        }
                    }
                } else {
                    event.getEntity().getKiller().getInventory().addItem(KillstreakReward.GRANDMA_SOUP);
                }
                if (event.getEntity().getKiller().getInventory().firstEmpty() == -1){
                    for (int i=0; i<event.getEntity().getKiller().getInventory().getSize(); i++){
                        ItemStack itemstack = event.getEntity().getKiller().getInventory().getItem(i);
                        if (itemstack != null) {
                            if (itemstack.getType() == Material.BOWL || itemstack.getType() == Material.MUSHROOM_STEW) {
                                event.getEntity().getKiller().getInventory().setItem(i, KillstreakReward.GRANDMA_SOUP);
                                return;
                            }
                        }
                    }
                } else {
                    event.getEntity().getKiller().getInventory().addItem(KillstreakReward.GRANDMA_SOUP);
                }
            }
        } else {
            if (profile.getCurrentKillstreak() == getRequired()){
                event.getEntity().getKiller().sendMessage(CC.translate("&aYou've received the &d" + getName() + " &aperk for reaching a &d" + getRequired() + " &akillstreak!"));
                if (event.getEntity().getKiller().getInventory().firstEmpty() == -1){
                    for (int i=0; i<event.getEntity().getKiller().getInventory().getSize(); i++){
                        ItemStack itemstack = event.getEntity().getKiller().getInventory().getItem(i);
                        if (itemstack != null) {
                            if (itemstack.getType() == Material.BOWL || itemstack.getType() == Material.MUSHROOM_STEW) {
                                event.getEntity().getKiller().getInventory().setItem(i, KillstreakReward.GRANDMA_SOUP);
                                return;
                            }
                        }
                    }
                } else {
                    event.getEntity().getKiller().getInventory().addItem(KillstreakReward.GRANDMA_SOUP);
                }
                if (event.getEntity().getKiller().getInventory().firstEmpty() == -1){
                    for (int i=0; i<event.getEntity().getKiller().getInventory().getSize(); i++){
                        ItemStack itemstack = event.getEntity().getKiller().getInventory().getItem(i);
                        if (itemstack != null) {
                            if (itemstack.getType() == Material.BOWL || itemstack.getType() == Material.MUSHROOM_STEW) {
                                event.getEntity().getKiller().getInventory().setItem(i, KillstreakReward.GRANDMA_SOUP);
                                return;
                            }
                        }
                    }
                } else {
                    event.getEntity().getKiller().getInventory().addItem(KillstreakReward.GRANDMA_SOUP);
                }
            }
        }
    }

}
