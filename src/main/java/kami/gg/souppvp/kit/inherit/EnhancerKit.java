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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EnhancerKit extends Kit {

    @Override
    public String getName() {
        return "Enhancer";
    }

    @Override
    public KitRarity getRarityType() {
        return KitRarity.RARE;
    }

    @Override
    public Integer getPrice() {
        return getRarityType().getPrice();
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.BREWING_STAND).build();
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Gain access to a portable effects enhancer to gain advantages");
        description.add("&7above other players through effects like strength, regeneration, etc.");
        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.IRON_SWORD).enchantment(Enchantment.SHARPNESS, 1).enchantment(Enchantment.UNBREAKING, 3).build());
        itemStacks.add(new ItemBuilder(Material.BREWING_STAND).name(CC.translate("&dStim Beacon")).build());
        return itemStacks;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.GOLDEN_BOOTS).enchantment(Enchantment.PROTECTION, 1).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.LEATHER_LEGGINGS).color(Color.BLACK).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 3).build(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.BLACK).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 3).build(),
                null
        };
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        List<PotionEffect> potionEffects = new ArrayList<>();
        potionEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
        return potionEffects;
    }

    @Override
    public void onSelect(Player player) {

    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event){
        Player player = event.getPlayer();
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Enhancer");
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;
        if (profile.getCurrentKit().equals(kit)){
            if (event.getPlayer().getItemInHand().isSimilar(this.getCombatEquipments().get(1)) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
                event.setCancelled(true);
                player.updateInventory();
                if (SoupPvP.getInstance().getTimersHandler().hasTimer(player.getUniqueId(), "Stim Beacon", true)) {
                    player.sendMessage(ChatColor.RED + "You can't use this for another " + ChatColor.YELLOW + DurationFormatter.getRemaining(SoupPvP.getInstance().getTimersHandler().getRemaining(player.getUniqueId(), "Stim Beacon", true), true) + ChatColor.RED + ".");
                    return;
                }
                SoupPvP.getInstance().getTimersHandler().addPlayerTimer(player.getUniqueId(), new Timer("Stim Beacon", TimeUnit.SECONDS.toMillis(60)), true);
                XPBarTimer.runXpBar(player, 60);
                PlayerUtil.playSound(player, Sound.UI_BUTTON_CLICK);
                BlockUtil.generateTemporaryStimBeacon(event.getClickedBlock().getLocation().add(0, 1, 0));
                new BukkitRunnable() {
                    public void run() {
                        for (Entity entity : player.getWorld().getEntities()){
                            if (entity instanceof Player){
                                if (SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(entity)) continue;
                                if (event.getClickedBlock().getLocation().distance(entity.getLocation()) < 5){
                                    Player entityPlayer = (Player) entity;
                                    Profile entityProfile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(entityPlayer.getUniqueId());
                                    if (entityProfile.getCurrentKit().getName().equals(EnhancerKit.this.getName())){
                                        entityPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 2));
                                        entityPlayer.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 10 * 20, 0));
                                        entityPlayer.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 10 * 20, 0));
                                        entityPlayer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 0));
                                    }
                                }
                            }
                        }
                        this.cancel();
                    }
                }.runTaskTimer(SoupPvP.getInstance(), 2L, 5*20L);
            }
        }
    }

}
