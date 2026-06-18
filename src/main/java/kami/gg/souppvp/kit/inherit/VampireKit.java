package kami.gg.souppvp.kit.inherit;

import com.google.common.collect.Lists;
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
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VampireKit extends Kit {

    @Override public String getName() { return "Vampire"; }
    @Override public KitRarity getRarityType() { return KitRarity.ULTIMATE; }
    @Override public Integer getPrice() { return getRarityType().getPrice(); }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override public ItemStack getIcon() {
        return new ItemBuilder(Material.BAT_SPAWN_EGG).build();
    }

    @Override public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Gain a bat shooting ability with a perk");
        description.add("&7of Regen V after each kill.");
        return description;
    }

    @Override public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.IRON_SWORD).enchantment(Enchantment.SHARPNESS, 1).enchantment(Enchantment.UNBREAKING, 1).build());
        itemStacks.add(new ItemBuilder(Material.BAT_SPAWN_EGG).name(CC.translate("&9Bat Blast")).build());
        return itemStacks;
    }

    @Override public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.CHAINMAIL_BOOTS).enchantment(Enchantment.PROTECTION, 1).build(),
                new ItemBuilder(Material.CHAINMAIL_LEGGINGS).enchantment(Enchantment.PROTECTION, 1).build(),
                new ItemBuilder(Material.CHAINMAIL_CHESTPLATE).enchantment(Enchantment.PROTECTION, 1).build(),
                new ItemBuilder(Material.CHAINMAIL_HELMET).enchantment(Enchantment.PROTECTION, 1).build()
        };
    }

    @Override public List<PotionEffect> getPotionEffects() {
        List<PotionEffect> potionEffects = new ArrayList<>();
        potionEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
        return potionEffects;
    }

    @Override public void onSelect(Player player) {}

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Vampire");
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;
        if (profile.getCurrentKit() == kit && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                && item != null && item.isSimilar(this.getCombatEquipments().get(1))) {
            event.setCancelled(true);
            if (profile.getProfileState() == ProfileState.SPAWN && SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player)) {
                player.sendMessage(CC.translate("&cYou can't do this in Spawn."));
                return;
            }
            if (player.hasMetadata("requireLand")) {
                player.sendMessage(ChatColor.RED + "You must land on the ground once you leave Spawn to use this!");
                return;
            }
            if (SoupPvP.getInstance().getTimersHandler().hasTimer(player.getUniqueId(), "Bat Blast", true)) {
                player.sendMessage(ChatColor.RED + "You can't use this for another " + ChatColor.YELLOW
                        + DurationFormatter.getRemaining(SoupPvP.getInstance().getTimersHandler().getRemaining(player.getUniqueId(), "Bat Blast", true), true)
                        + ChatColor.RED + ".");
                return;
            }
            List<Entity> entities = Lists.newArrayList();
            for (int i = 1; i <= 6; i++) {
                entities.add(player.getWorld().spawnEntity(player.getLocation(), EntityType.BAT));
            }
            new BukkitRunnable() {
                final long stayTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.toMillis(2000L);
                final long boostTime = System.currentTimeMillis() + 100L;
                final Location location = player.getEyeLocation().clone();
                long ticks = 0L;
                @Override
                public void run() {
                    if (System.currentTimeMillis() >= this.stayTime) {
                        this.cancel();
                        entities.forEach(Entity::remove);
                        return;
                    }
                    entities.stream()
                            .filter(Entity::isValid)
                            .forEach(bat -> {
                                if (System.currentTimeMillis() < this.boostTime) {
                                    bat.setVelocity(this.location.getDirection().clone().multiply(1.9));
                                }
                                bat.setVelocity(this.location.getDirection().clone().multiply(0.4));
                                bat.getNearbyEntities(3, 3, 3).stream()
                                        .filter(entity -> entity instanceof Player && !SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(entity))
                                        .map(Player.class::cast)
                                        .filter(found -> !found.getUniqueId().equals(player.getUniqueId()))
                                        .forEach(found -> {
                                            bat.setVelocity(bat.getVelocity().add(new Vector(0, 0.5, 0)));
                                            try { bat.getWorld().playSound(bat.getLocation(), Sound.ENTITY_BAT_HURT, 0.1F, 0.1F); } catch (Throwable ignored) {}
                                            found.setVelocity(bat.getVelocity().clone().add(new Vector(0, 0.07, 0)));
                                            if (ticks % 3 == 0) {
                                                found.damage(10, player);
                                            }
                                        });
                            });
                    ticks++;
                }
            }.runTaskTimer(SoupPvP.getInstance(), 1L, 1L);
            try { player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1F, 1F); } catch (Throwable ignored) {}
            SoupPvP.getInstance().getTimersHandler().addPlayerTimer(player.getUniqueId(), new Timer("Bat Blast", TimeUnit.SECONDS.toMillis(45)), true);
            XPBarTimer.runXpBar(player, 45);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player killer = event.getEntity().getKiller();
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(killer.getUniqueId());
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Vampire");
        if (!killer.getUniqueId().equals(event.getEntity().getUniqueId()) && profile.getCurrentKit() == kit) {
            killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 10, 4));
        }
    }
}
