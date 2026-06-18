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
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RogueKit extends Kit {

    @Override
    public String getName() {
        return "Rogue";
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
        return new ItemBuilder(Material.GOLDEN_SWORD).build();
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Go rogue and back stab any enemies you see. With every backstab,");
        description.add("&7deal a large amount of damage towards enemies to catch them off guard.");
        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.IRON_SWORD).enchantment(Enchantment.SHARPNESS, 1).enchantment(Enchantment.UNBREAKING, 3).build());
        itemStacks.add(new ItemBuilder(Material.GOLDEN_SWORD).name(CC.translate("&6Backstab Dagger")).enchantment(Enchantment.UNBREAKING, 10).build());
        return itemStacks;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.CHAINMAIL_BOOTS).enchantment(Enchantment.PROTECTION, 1).build(),
                new ItemBuilder(Material.CHAINMAIL_LEGGINGS).enchantment(Enchantment.PROTECTION, 1).build(),
                new ItemBuilder(Material.IRON_CHESTPLATE).enchantment(Enchantment.PROTECTION, 1).build(),
                new ItemBuilder(Material.LEATHER_HELMET).color(Color.BLACK).enchantment(Enchantment.PROTECTION, 1).enchantment(Enchantment.UNBREAKING, 3).build()
        };
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        List<PotionEffect> potionEffects = new ArrayList<>();
        potionEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        potionEffects.add(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0));
        return potionEffects;
    }

    @Override
    public void onSelect(Player player) {

    }

    @EventHandler(priority= EventPriority.MONITOR)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Profile damagerProfile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(damager.getUniqueId());
            Player victim = (Player) event.getEntity();
            Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Rogue");
            if (damagerProfile.isInEvent() || damagerProfile.getProfileState() == ProfileState.SPAWN) return;
            if (damagerProfile.getCurrentKit().equals(kit)){
                if (damager.getItemInHand() != null && damager.getItemInHand().getType() == this.getCombatEquipments().get(1).getType()){
                    if (SoupPvP.getInstance().getTimersHandler().hasTimer(damager.getUniqueId(), "Back Stabber", true)) {
                        damager.sendMessage(ChatColor.RED + "You can't use this for another " + ChatColor.YELLOW + DurationFormatter.getRemaining(SoupPvP.getInstance().getTimersHandler().getRemaining(damager.getUniqueId(), "Back Stabber", true), true) + ChatColor.RED + ".");
                        return;
                    }
                    Vector playerVector = damager.getLocation().getDirection();
                    Vector entityVector = victim.getLocation().getDirection();

                    playerVector.setY(0F);
                    entityVector.setY(0F);

                    double degrees = playerVector.angle(entityVector);

                    if (Math.abs(degrees) < 1.4) {

                        SoupPvP.getInstance().getTimersHandler().addPlayerTimer(damager.getUniqueId(), new Timer("Back Stabber", TimeUnit.SECONDS.toMillis(30)), true);
                        XPBarTimer.runXpBar(damager, 30);

                        damager.playSound(damager.getLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
                        damager.getWorld().playEffect(victim.getEyeLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);

                        if (victim.getHealth() - 8D <= 0) {
                            victim.damage(8, damager);
                        } else {
                            event.setDamage(0D);
                        }
                        victim.setHealth(victim.getHealth() - 8D);
                        damager.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 2 * 20, 1));
                    } else {
                        damager.sendMessage(CC.translate("&cBackstab failed!"));
                    }
                }
            }
        }
    }

}
