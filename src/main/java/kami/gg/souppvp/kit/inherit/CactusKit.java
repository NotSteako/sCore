package kami.gg.souppvp.kit.inherit;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.KitCategory;
import kami.gg.souppvp.kit.KitRarity;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.util.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CactusKit extends Kit {

    @Override
    public String getName() {
        return "Cactus";
    }

    @Override
    public KitRarity getRarityType() {
        return KitRarity.MYTHICAL;
    }

    @Override
    public Integer getPrice() {
        return getRarityType().getPrice();
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.CACTUS).build();
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Become a cactus and reflect a percentage of the");
        description.add("&7damage from enemies back to themselves.");
        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.DIAMOND_SWORD).enchantment(Enchantment.SHARPNESS, 1).build());
        return itemStacks;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.LEATHER_BOOTS).color(Color.GREEN).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.LEATHER_LEGGINGS).color(Color.GREEN).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.GREEN).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.LEATHER_HELMET).color(Color.GREEN).enchantment(Enchantment.PROTECTION, 2).enchantment(Enchantment.UNBREAKING, 10).build()
        };
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        List<PotionEffect> potionEffects = new ArrayList<>();
        potionEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
        potionEffects.add(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0));
        return potionEffects;
    }

    @Override
    public void onSelect(Player player) {

    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player player = (Player) event.getEntity();
            Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Cactus");
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
            if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) {
                return;
            }
            if (!(SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player))){
                if (profile.getCurrentKit() == kit) {
                    if (new Random().nextDouble() <= 0.25) {
                        ((Player) event.getDamager()).damage(event.getDamage() * 0.25);
                    }
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

}
