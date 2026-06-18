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
import java.util.concurrent.TimeUnit;

public class MelonKit extends Kit {

    @Override
    public String getName() {
        return "Melon";
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
        return new ItemBuilder(Material.MELON).build();
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Chop your enemies up with your melon and shoot");
        description.add("&7your melon tosser and toss your enemies into");
        description.add("&7the air.");
        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.MELON).enchantment(Enchantment.SHARPNESS, 4).build());
        itemStacks.add(new ItemBuilder(Material.GLISTERING_MELON_SLICE).name(CC.translate("&2Melon Toss")).build());
        return itemStacks;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.IRON_BOOTS).build(),
                new ItemBuilder(Material.IRON_LEGGINGS).build(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.GREEN).enchantment(Enchantment.UNBREAKING, 1).enchantment(Enchantment.UNBREAKING, 10).build(),
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
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Melon");
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;
        if (profile.getCurrentKit() == kit && event.getPlayer().getItemInHand().isSimilar(this.getCombatEquipments().get(1)) && (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))){
            event.setCancelled(true);
            player.updateInventory();
            if (SoupPvP.getInstance().getTimersHandler().hasTimer(player.getUniqueId(), "Melon Tosser", true)) {
                player.sendMessage(ChatColor.RED + "You can't use this for another " + ChatColor.YELLOW + DurationFormatter.getRemaining(SoupPvP.getInstance().getTimersHandler().getRemaining(player.getUniqueId(), "Melon Tosser", true), true) + ChatColor.RED + ".");
                return;
            }
            if (SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(BlockUtil.getTargetBlock(player, 20).getLocation())){
                player.sendMessage(CC.translate("&cYou can't do this in spawn."));
            } else {
                SoupPvP.getInstance().getTimersHandler().addPlayerTimer(player.getUniqueId(), new Timer("Melon Tosser", TimeUnit.SECONDS.toMillis(30)), true);
                XPBarTimer.runXpBar(player, 30);
                PlayerUtil.playSound(player, Sound.ENTITY_GENERIC_EXPLODE);

                FallingBlock block = event.getPlayer().getWorld().spawnFallingBlock(player.getEyeLocation(), Material.MELON, (byte) 0);
                block.setMetadata("melon_tosser", new FixedMetadataValue(SoupPvP.getInstance(), player.getUniqueId()));
                block.setDropItem(false);
                player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F);

                block.setVelocity(event.getPlayer().getEyeLocation().getDirection().multiply(2.5).add(new Vector(0, 0.3, 0)));

                new BukkitRunnable() {

                    @Override
                    public void run() {
                        if (block.isDead() || !block.isValid() || !player.isOnline()) {
                            cancel();
                            return;
                        }

                        for(Entity entity : block.getNearbyEntities(3, 3, 3)) {
                            if(entity instanceof Player) {
                                Profile profile1 = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getPlayer().getUniqueId());
                                if(entity.getUniqueId().equals(player.getUniqueId()) || profile1.getProfileState() == ProfileState.SPAWN) {
                                    continue;
                                }

                                block.remove();
                                cancel();

                                Player found = (Player) entity;
                                Vector velocity = found.getLocation().toVector().subtract(player.getLocation().toVector())
                                        .multiply(0.3)
                                        .setY(1.5);
                                found.setVelocity(velocity);

                                break;
                            }
                        }
                    }
                }.runTaskTimer(SoupPvP.getInstance(), 2L, 2L);
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        if(event.getEntityType() == EntityType.FALLING_BLOCK && event.getEntity().hasMetadata("melon_tosser")) {
            event.getEntity().remove();
            event.setCancelled(true);

        }
    }


}
