package kami.gg.souppvp.kit.inherit;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.KitCategory;
import kami.gg.souppvp.kit.KitRarity;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.timer.Timer;
import kami.gg.souppvp.util.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EskimoKit extends Kit {

    @Override
    public String getName() {
        return "Eskimo";
    }

    @Override
    public KitRarity getRarityType() {
        return KitRarity.ULTIMATE;
    }

    @Override
    public Integer getPrice() {
        return getRarityType().getPrice();
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.PACKED_ICE).build();
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Whenever possible, spawn in an ice dome like an igloo to");
        description.add("&7trap enemies and gain a higher chance of killing them.");
        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.IRON_SWORD).enchantment(Enchantment.SHARPNESS, 1).enchantment(Enchantment.UNBREAKING, 3).build());
        itemStacks.add(new ItemBuilder(Material.PACKED_ICE).name(CC.translate("&5Ice Dome")).build());
        return itemStacks;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.LEATHER_BOOTS).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.LEATHER_LEGGINGS).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.LEATHER_HELMET).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build()
        };
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        List<PotionEffect> potionEffects = new ArrayList<>();
        potionEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
        potionEffects.add(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0));
        return potionEffects;
    }

    @Override
    public void onSelect(Player player) {

    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event){
        Player player = event.getPlayer();
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Eskimo");
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;
        if (profile.getCurrentKit().equals(kit)){
            if (event.getPlayer().getItemInHand().isSimilar(this.getCombatEquipments().get(1)) && (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))){
                event.setCancelled(true);
                player.updateInventory();
                if(SoupPvP.getInstance().getTimersHandler().hasTimer(player.getUniqueId(), "Ice Dome", true)) {
                    player.sendMessage(ChatColor.RED + "You can't use this for another " + ChatColor.YELLOW + DurationFormatter.getRemaining(SoupPvP.getInstance().getTimersHandler().getRemaining(player.getUniqueId(), "Ice Dome", true), true) + ChatColor.RED + ".");
                    return;
                }
                SoupPvP.getInstance().getTimersHandler().addPlayerTimer(player.getUniqueId(), new Timer("Ice Dome", TimeUnit.SECONDS.toMillis(30)), true);
                XPBarTimer.runXpBar(player, 30);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 5 * 20, 1));
                BlockUtil.generateTemporarySphere(player.getLocation().add(0, -1, 0), 5, true, Material.ICE, 5);
            }
        }
    }

}
