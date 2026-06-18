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
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DwarfKit extends Kit {

    @Override
    public String getName() {
        return "Dwarf";
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
        return new ItemStack(Material.GOLDEN_AXE);
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Crouch to charge up a blast that");
        description.add("&7knocks players away.");

        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.GOLDEN_AXE).name(ChatColor.BLUE + "Dwarf's Axe").enchantment(Enchantment.SHARPNESS, 3).enchantment(Enchantment.UNBREAKING, 10).build());
        return itemStacks;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.IRON_BOOTS).build(),
                new ItemBuilder(Material.IRON_LEGGINGS).build(),
                new ItemBuilder(Material.IRON_CHESTPLATE).build(),
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

    @Getter
    public static final HashMap<UUID, Float> chargeUp = new HashMap<>();

    @Override
    public void setup() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
                    Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Dwarf");
                    if (profile.getCurrentKit() == kit && !SoupPvP.getInstance().getTimersHandler().hasTimer(player.getUniqueId(), "Charge Up", true)) {
                        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN){
                            return;
                        }
                        if (!player.isSneaking()) {
                            chargeUp.put(player.getUniqueId(), Math.max(0.0F, chargeUp.getOrDefault(player.getUniqueId(), 0F) - 0.1F));
                        } else {
                            if (chargeUp.getOrDefault(player.getUniqueId(), 0F) < 1.0F) {
                                chargeUp.put(player.getUniqueId(), chargeUp.getOrDefault(player.getUniqueId(), 0F) + 0.1F);
                                if (chargeUp.get(player.getUniqueId()) >= 1.0F) {
                                    player.sendMessage(CC.translate("&6&lYou are fully Charged Up!"));
                                }

                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1f, 1f);
                            }
                        }

                        player.setExp(chargeUp.getOrDefault(player.getUniqueId(), 0F));
                    }
                }
            }
        }.runTaskTimerAsynchronously(SoupPvP.getInstance(), 0L, 20L);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(damager.getUniqueId());
            Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Dwarf");
            if (profile.getCurrentKit() == kit && damager.getItemInHand() != null && damager.getItemInHand().getType() == this.getCombatEquipments().get(0).getType() && chargeUp.getOrDefault(damager.getUniqueId(), 0F) >= 1.0F) {
                Player player = (Player) event.getEntity();
                if (SoupPvP.getInstance().getTimersHandler().hasTimer(player.getUniqueId(), "Charge Up", true)) {
                    player.sendMessage(ChatColor.RED + "You can't use this for another " + ChatColor.YELLOW + DurationFormatter.getRemaining(SoupPvP.getInstance().getTimersHandler().getRemaining(player.getUniqueId(), "Charged Up", true), true) + ChatColor.RED + ".");
                    return;
                }
                if (profile.getProfileState() == ProfileState.SPAWN && SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player)) {
                    player.sendMessage(CC.translate("&cYou can't do this in Spawn."));
                    return;
                }
                Vector velocity = player.getLocation().toVector().subtract(damager.getLocation().toVector())
                        .multiply(0.8)
                        .setY(1.9);
                Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
                    damager.setExp(0);
                    damager.setLevel(0);
                    player.setVelocity(velocity);
                    player.getWorld().playEffect(event.getEntity().getLocation(), Effect.SMOKE, 1, 10);
                    player.playSound(event.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);

                    SoupPvP.getInstance().getTimersHandler().addPlayerTimer(player.getUniqueId(), new Timer("Charged Up", TimeUnit.SECONDS.toMillis(10)), true);
                    XPBarTimer.runXpBar(damager, 10);

                    chargeUp.put(damager.getUniqueId(), 0F);
                }, 3L);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player) {
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getEntity().getUniqueId());
            Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Dwarf");
            if (profile.getCurrentKit() == kit) {
                if(event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    event.setDamage(event.getDamage() / 3.0);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event){
        if (chargeUp.isEmpty()) return;
        chargeUp.remove(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event){
        if (chargeUp.isEmpty()) return;
        chargeUp.remove(event.getPlayer().getUniqueId());
    }

}
