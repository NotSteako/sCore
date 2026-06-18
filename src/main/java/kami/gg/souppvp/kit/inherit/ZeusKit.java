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
import org.bukkit.event.block.BlockIgniteEvent;
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

public class ZeusKit extends Kit {

    @Override
    public String getName() {
        return "Zeus";
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
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
        return new ItemBuilder(Material.GLOWSTONE_DUST).build();
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7With the immense ability of a god, you possess control over lightning,");
        description.add("&7striking anyone who stands in front of you.");
        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.DIAMOND_SWORD).build());
        itemStacks.add(new ItemBuilder(Material.GLOWSTONE_DUST).name(CC.translate("&6Lightning Bolt")).build());
        return itemStacks;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.LEATHER_BOOTS).color(Color.YELLOW).enchantment(Enchantment.UNBREAKING, 20).build(),
                new ItemBuilder(Material.IRON_LEGGINGS).build(),
                new ItemBuilder(Material.IRON_CHESTPLATE).build(),
                new ItemBuilder(Material.LEATHER_HELMET).color(Color.YELLOW).enchantment(Enchantment.FIRE_PROTECTION, 1).build()
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
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Zeus");
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;
        if (profile.getCurrentKit().equals(kit)){
            if (event.getPlayer().getItemInHand().isSimilar(this.getCombatEquipments().get(1)) && (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))){
                event.setCancelled(true);
                player.updateInventory();
                if (SoupPvP.getInstance().getTimersHandler().hasTimer(player.getUniqueId(), "Lightning Bolt", true)) {
                    player.sendMessage(ChatColor.RED + "You can't use this for another " + ChatColor.YELLOW + DurationFormatter.getRemaining(SoupPvP.getInstance().getTimersHandler().getRemaining(player.getUniqueId(), "Lightning Bolt", true), true) + ChatColor.RED + ".");
                    return;
                }
                if (SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(BlockUtil.getTargetBlock(player, 10).getLocation())){
                    player.sendMessage(CC.translate("&cYou can't do this in spawn."));
                } else {
                    SoupPvP.getInstance().getTimersHandler().addPlayerTimer(player.getUniqueId(), new Timer("Lightning Bolt", TimeUnit.SECONDS.toMillis(45)), true);
                    XPBarTimer.runXpBar(player, 45);
                    PlayerUtil.playSound(player, Sound.ENTITY_LIGHTNING_BOLT_THUNDER);
                    FallingBlock block = event.getPlayer().getWorld().spawnFallingBlock(player.getEyeLocation(), Material.RED_STAINED_GLASS, (byte) 4);
                    block.setMetadata("lightning", new FixedMetadataValue(SoupPvP.getInstance(), player.getUniqueId()));
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
                                    found.damage(8, player);
                                    player.getWorld().strikeLightning(found.getLocation());

                                    break;
                                }
                            }
                        }
                    }.runTaskTimer(SoupPvP.getInstance(), 2L, 2L);
                }
            }
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        if(event.getEntityType() == EntityType.FALLING_BLOCK && event.getEntity().hasMetadata("lightning")) {
            event.getEntity().remove();
            event.setCancelled(true);

        }
    }

}
