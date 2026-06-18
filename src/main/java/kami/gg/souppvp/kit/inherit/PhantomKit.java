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
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PhantomKit extends Kit {

    @Override
    public String getName() {
        return "Phantom";
    }

    @Override
    public KitRarity getRarityType() {
        return KitRarity.LEGENDARY;
    }

    @Override
    public Integer getPrice() {
        return getRarityType().getPrice();
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.FEATHER).build();
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Fly high into the sky to escape from awkward");
        description.add("&7situations.");
        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.DIAMOND_SWORD).enchantment(Enchantment.SHARPNESS, 1).build());
        itemStacks.add(new ItemBuilder(Material.FEATHER).name(CC.translate("&7Phantom")).build());
        return itemStacks;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.LEATHER_BOOTS).color(Color.WHITE).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.LEATHER_LEGGINGS).color(Color.WHITE).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.WHITE).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.LEATHER_HELMET).color(Color.WHITE).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build()
        };
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        List<PotionEffect> potionEffects = new ArrayList<>();
        potionEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        return potionEffects;
    }

    @Override
    public void onSelect(Player player) {

    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event){
        Player player = event.getPlayer();
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Phantom");
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;
        if (profile.getCurrentKit() == kit && event.getPlayer().getItemInHand().isSimilar(this.getCombatEquipments().get(1)) && (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))){
            event.setCancelled(true);
            player.updateInventory();
            if (SoupPvP.getInstance().getTimersHandler().hasTimer(player.getUniqueId(), "Phantom", true)) {
                player.sendMessage(ChatColor.RED + "You can't use this for another " + ChatColor.YELLOW + DurationFormatter.getRemaining(SoupPvP.getInstance().getTimersHandler().getRemaining(player.getUniqueId(), "Phantom", true), true) + ChatColor.RED + ".");
                return;
            }
            SoupPvP.getInstance().getTimersHandler().addPlayerTimer(player.getUniqueId(), new Timer("Phantom", TimeUnit.SECONDS.toMillis(30)), true);
            XPBarTimer.runXpBar(player, 30);
            player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1F, 1F);
            for (Entity entity : player.getNearbyEntities(5, 5, 5)){
                if (entity instanceof Player){
                    PlayerUtil.playSound((Player) entity, Sound.ENTITY_ENDERMAN_STARE);
                }
            }
            player.setAllowFlight(true);
            player.setFlying(true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 5, 1));
            TasksUtility.runTaskLater(() -> {
                player.setAllowFlight(false);
                player.setFlying(false);
            }, 5 * 20);
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event){
        if (SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getEntity().getUniqueId()).getCurrentKit().equals(SoupPvP.getInstance().getKitsHandler().getKitByName("Phantom"))){
            event.getEntity().setFlying(false);
            event.getEntity().setAllowFlight(false);
        }
    }

}
