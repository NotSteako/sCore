package kami.gg.souppvp.kit.inherit;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.KitCategory;
import kami.gg.souppvp.kit.KitRarity;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.timer.Timer;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.DurationFormatter;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.XPBarTimer;
import kami.gg.souppvp.util.projectile.TypedRunnable;
import kami.gg.souppvp.util.projectile.event.CustomProjectileHitEvent;
import kami.gg.souppvp.util.projectile.projectile.ItemProjectile;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
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

public class NinjaKit extends Kit {

    @Override
    public String getName() {
        return "Ninja";
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
        return new ItemBuilder(Material.NETHER_STAR).build();
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Throwing shurikens, blinding your enemies as you attack.");
        description.add("&7Gain +30 armor durability per kill!");
        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.IRON_SWORD).enchantment(Enchantment.SHARPNESS, 1).build());
        itemStacks.add(new ItemBuilder(Material.NETHER_STAR).name(CC.translate("&bShuriken")).amount(4).build());
        return itemStacks;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.LEATHER_BOOTS).color(Color.BLACK).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.LEATHER_LEGGINGS).color(Color.BLACK).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.BLACK).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.LEATHER_HELMET).color(Color.BLACK).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build()
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
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player killer = event.getEntity().getKiller();
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(killer.getUniqueId());
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Ninja");
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;
        if (profile.getCurrentKit() == kit) {
            for(ItemStack item : killer.getInventory().getArmorContents()) {
                if(item != null) {
                    item.setDurability((short) (Math.min(item.getType().getMaxDurability(), item.getDurability() - 30)));
                }
            }
            for (ItemStack itemStack : killer.getInventory()) {
                if(itemStack != null && itemStack.getType() == Material.NETHER_STAR) {
                    itemStack.setAmount(Math.max(1, 4));
                    break;
                }
            }
            Bukkit.getScheduler().runTask(SoupPvP.getInstance(), killer::updateInventory);
            killer.sendMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "JUTSU! " + ChatColor.YELLOW + "You earned an extra shuriken star!");
            killer.updateInventory();
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Ninja");
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) {
            return;
        }
        if (profile.getCurrentKit() == kit){
            if((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && item != null && item.isSimilar(this.getCombatEquipments().get(1))) {
                if (profile.getProfileState() == ProfileState.SPAWN && SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player)) {
                    player.sendMessage(CC.translate("&cYou can't do this in Spawn."));
                    return;
                }

                if(SoupPvP.getInstance().getTimersHandler().hasTimer(player.getUniqueId(), "Shuriken", true)) {
                    player.sendMessage(ChatColor.RED + "You can't use this for another " + ChatColor.YELLOW + DurationFormatter.getRemaining(SoupPvP.getInstance().getTimersHandler().getRemaining(player.getUniqueId(), "Shuriken", true), true) + ChatColor.RED + ".");
                    return;
                }

                player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1F, 1F);

                ItemProjectile projectile = new ItemProjectile("SHURIKEN", player, new ItemStack(Material.NETHER_STAR), 2);
                projectile.addTypedRunnable((TypedRunnable<ItemProjectile>) o -> o.getEntity().getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, o.getEntity().getLocation(), 1));

                if (player.getItemInHand().getAmount() == 1) player.setItemInHand(null);
                player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);

                SoupPvP.getInstance().getTimersHandler().addPlayerTimer(player.getUniqueId(), new Timer("Shuriken", TimeUnit.SECONDS.toMillis(10)), true);
                XPBarTimer.runXpBar(player, 10);
                Bukkit.getScheduler().runTask(SoupPvP.getInstance(), player::updateInventory);
            }
        }

    }

    @EventHandler
    public void onHit(CustomProjectileHitEvent event){
        if (event.getHitType() == CustomProjectileHitEvent.HitType.ENTITY){
            if (event.getProjectile().getProjectileName().equals("SHURIKEN") && event.getHitEntity() != event.getProjectile().getShooter()){
                event.getHitEntity().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 3 * 20, 2));
                event.getHitEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 3 * 20, 2));
                if (event.getHitEntity() instanceof Player){
                    if (SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(event.getHitEntity())) return;
                    Player found = (Player) event.getHitEntity();
                    found.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1)); // 5 seconds
                    found.playSound(event.getProjectile().getShooter().getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
                    found.playSound(found.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
                    found.damage(4, found);
                }
            }
        }
    }

}
