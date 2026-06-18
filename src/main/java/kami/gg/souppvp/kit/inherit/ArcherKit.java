package kami.gg.souppvp.kit.inherit;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.KitCategory;
import kami.gg.souppvp.kit.KitRarity;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ArcherKit extends Kit {

    private HashMap<UUID, Location> locations = new HashMap<>();

    @Override
    public String getName() {
        return "Archer";
    }

    @Override
    public KitRarity getRarityType() {
        return KitRarity.UNCOMMON;
    }

    @Override
    public Integer getPrice() {
        return getRarityType().getPrice();
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.BOW).build();
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Become robin hood and snipe enemies. Depending");
        description.add("&7on range, the further the range, the more damage");
        description.add("&7your bow deals.");
        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.IRON_SWORD).enchantment(Enchantment.UNBREAKING, 2).build());
        itemStacks.add(new ItemBuilder(Material.BOW).enchantment(Enchantment.UNBREAKING, 3).enchantment(Enchantment.INFINITY, 1).build());
        itemStacks.add(new ItemBuilder(Material.ARROW).amount(64).build());
        return itemStacks;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[] {
                new ItemBuilder(Material.LEATHER_BOOTS).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.LEATHER_LEGGINGS).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.LEATHER_HELMET).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build()
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


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Arrow && event.getEntity().getShooter() != null) {
            Arrow arrow = (Arrow) event.getEntity();
            if (!(event.getEntity().getShooter() instanceof Player player)) {
                return;
            }
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
            Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Archer");
            if (profile.getProfileState() == ProfileState.SPAWN) {
                player.sendMessage(CC.translate("&cYou can't do this in Spawn."));
                event.setCancelled(true);
                return;
            }
            if (profile.getCurrentKit().equals(kit)){
                this.locations.put(player.getUniqueId(), player.getLocation());
            }

        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityDamage(EntityDamageByEntityEvent event) {

        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if (entity instanceof Player && damager instanceof Arrow) {
            if (!(SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(entity))){
                Arrow arrow = (Arrow) damager;
                ProjectileSource source = arrow.getShooter();
                if (source instanceof Player) {
                    Player damaged = (Player) event.getEntity();
                    Player shooter = (Player) source;
                    Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(shooter.getUniqueId());
                    if (!profile.getCurrentKit().getName().equalsIgnoreCase("Archer"))

                    if (damaged.getName().equalsIgnoreCase(shooter.getName())) {
                        return;
                    }
                    if (this.locations.get(shooter.getUniqueId()) == null) {
                        return;
                    }
                    int distance = (int) this.locations.get(shooter.getUniqueId()).distance(damaged.getLocation());
                    this.locations.remove(shooter.getUniqueId());
                    if (distance >= 30) {
                        event.setDamage(4.0);
                    } else if (distance >= 25) {
                        event.setDamage(3.0);
                    } else if (distance >= 20) {
                        event.setDamage(2.5);
                    } else if (distance >= 10) {
                        event.setDamage(2.0);
                    } else {
                        event.setDamage(1.5);
                    }
                }
            } else {
                event.setCancelled(true);
                event.setDamage(0);
                Arrow arrow = (Arrow) damager;
                ProjectileSource source = arrow.getShooter();
                if (source instanceof Player){
                    ((Player) source).sendMessage(CC.translate("&cYou can't damage players in spawn."));
                }
            }
        }
    }

}
