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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CyclopsKit extends Kit {

    @Override
    public String getName() {
        return "Cyclops";
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
        return new ItemBuilder(Material.REDSTONE).build();
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Laser beam enemies with penetrable blocks to set");
        description.add("&7the exciting fights straight and scare off enemies.");
        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.IRON_SWORD).enchantment(Enchantment.SHARPNESS, 1).enchantment(Enchantment.UNBREAKING, 3).build());
        itemStacks.add(new ItemBuilder(Material.REDSTONE).name(CC.translate("&cLaser Beam")).build());
        return itemStacks;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.IRON_BOOTS).build(),
                new ItemBuilder(Material.CHAINMAIL_LEGGINGS).enchantment(Enchantment.UNBREAKING, 1).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.RED).enchantment(Enchantment.UNBREAKING, 1).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.IRON_HELMET).build()
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
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Cyclops");
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (profile.getCurrentKit().equals(kit)){
            if (event.getPlayer().getItemInHand().isSimilar(this.getCombatEquipments().get(1)) && (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))){
                event.setCancelled(true);
                player.updateInventory();
                if (SoupPvP.getInstance().getTimersHandler().hasTimer(player.getUniqueId(), "Laser Beam", true)) {
                    player.sendMessage(ChatColor.RED + "You can't use this for another " + ChatColor.YELLOW + DurationFormatter.getRemaining(SoupPvP.getInstance().getTimersHandler().getRemaining(player.getUniqueId(), "Laser Beam", true), true) + ChatColor.RED + ".");
                    return;
                }
                SoupPvP.getInstance().getTimersHandler().addPlayerTimer(player.getUniqueId(), new Timer("Laser Beam", TimeUnit.SECONDS.toMillis(45)), true);
                XPBarTimer.runXpBar(player, 45);
                PlayerUtil.playSound(player, Sound.ENTITY_GHAST_SCREAM);
                for (Entity entity : player.getNearbyEntities(5, 5, 5)){
                    if (entity instanceof Player){
                        PlayerUtil.playSound((Player) entity, Sound.ENTITY_ZOMBIE_VILLAGER_CURE);
                    }
                }
                new BukkitRunnable() {
                    int i = 0;
                    @Override
                    public void run() {
                        if(i >= 30) {
                            cancel();
                        }
                        ++i;
                        if (new Random().nextDouble() <= 0.5){
                            FallingBlock block = event.getPlayer().getWorld().spawnFallingBlock(player.getEyeLocation(), Material.RED_STAINED_GLASS, (byte) 14);
                            block.setMetadata("laser_beam", new FixedMetadataValue(SoupPvP.getInstance(), player.getUniqueId()));
                            block.setDropItem(false);
                            player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F);

                            block.setVelocity(event.getPlayer().getEyeLocation().getDirection().multiply(2).add(new Vector(0, 0.3, 0)));

                            new BukkitRunnable() {

                                @Override
                                public void run() {
                                    if (block.isDead() || !block.isValid() || !player.isOnline()) {
                                        cancel();
                                        return;
                                    }

                                    for(Entity entity : block.getNearbyEntities(2, 2, 2)) {
                                        if(entity instanceof Player) {
                                            Profile profile1 = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getPlayer().getUniqueId());
                                            if(entity.getUniqueId().equals(player.getUniqueId()) || profile1.getProfileState() == ProfileState.SPAWN) {
                                                continue;
                                            }

                                            block.remove();
                                            cancel();

                                            Player found = (Player) entity;
                                            found.damage(4, player);

                                            break;
                                        }
                                    }
                                }
                            }.runTaskTimer(SoupPvP.getInstance(), 2L, 2L);
                        } else {
                            FallingBlock block = event.getPlayer().getWorld().spawnFallingBlock(player.getEyeLocation(), Material.RED_STAINED_GLASS, (byte) 1);
                            block.setMetadata("laser_beam", new FixedMetadataValue(SoupPvP.getInstance(), player.getUniqueId()));
                            block.setDropItem(false);
                            player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F);

                            block.setVelocity(event.getPlayer().getEyeLocation().getDirection().multiply(2).add(new Vector(0, 0.3, 0)));

                            new BukkitRunnable() {

                                @Override
                                public void run() {
                                    if (block.isDead() || !block.isValid() || !player.isOnline()) {
                                        cancel();
                                        return;
                                    }

                                    for(Entity entity : block.getNearbyEntities(2, 2, 2)) {
                                        if(entity instanceof Player) {
                                            if(entity.getUniqueId().equals(player.getUniqueId())) {
                                                continue;
                                            }

                                            block.remove();
                                            cancel();

                                            Player found = (Player) entity;
                                            found.damage(4, player);

                                            break;
                                        }
                                    }
                                }
                            }.runTaskTimer(SoupPvP.getInstance(), 2L, 2L);
                        }
                    }
                }.runTaskTimer(SoupPvP.getInstance(), 1L, 1L);
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        if(event.getEntityType() == EntityType.FALLING_BLOCK && event.getEntity().hasMetadata("laser_beam")) {
            event.getEntity().remove();
            event.setCancelled(true);

        }
    }

}
