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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class FiremanKit extends Kit {

    @Override
    public String getName() {
        return "Fireman";
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
        return new ItemBuilder(Material.LAVA_BUCKET).build();
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Become immune whenever coming into contact with fire however,");
        description.add("&7when coming into contact with water, there are consequences.");
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
                new ItemBuilder(Material.LEATHER_BOOTS).color(Color.RED).enchantment(Enchantment.PROTECTION, 1).enchantment(Enchantment.UNBREAKING, 3).build(),
                new ItemBuilder(Material.LEATHER_LEGGINGS).color(Color.RED).build(),
                new ItemBuilder(Material.IRON_CHESTPLATE).enchantment(Enchantment.PROTECTION, 1).build(),
                new ItemBuilder(Material.LEATHER_HELMET).color(Color.RED).enchantment(Enchantment.UNBREAKING, 3).build()
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
    public void EntityDamageEvent(EntityDamageEvent event){
        if (event.getEntity() instanceof Player){
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getEntity().getUniqueId());
            Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Fireman");
            if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;
            if (event.getCause() == EntityDamageEvent.DamageCause.LAVA || event.getCause() == EntityDamageEvent.DamageCause.FIRE || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK){
                if (profile.getCurrentKit() == kit){
                    event.setCancelled(true);
                }
            }
        }
    }

}
