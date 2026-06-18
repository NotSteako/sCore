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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class PalioxisKit extends Kit {

    @Override
    public String getName() {
        return "Palioxis";
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
        return new ItemBuilder(Material.ENDER_PEARL).build();
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Like and enderman, have the ability to teleport quickly");
        description.add("&7and efficiently, to get to your desired destinations.");
        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.DIAMOND_SWORD).enchantment(Enchantment.SHARPNESS, 1).build());
        itemStacks.add(new ItemBuilder(Material.ENDER_PEARL).amount(1).build());
        return itemStacks;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.IRON_BOOTS).build(),
                new ItemBuilder(Material.IRON_LEGGINGS).build(),
                new ItemBuilder(Material.IRON_CHESTPLATE).build(),
                new ItemBuilder(Material.LEATHER_HELMET).color(Color.BLACK).enchantment(Enchantment.PROTECTION, 1).enchantment(Enchantment.UNBREAKING, 10).build()
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
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.getKiller() != null) {
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getKiller().getUniqueId());
            Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Palioxis");
            if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;
            if (profile.getCurrentKit() == kit) {
                Player killer = player.getKiller();
                ItemStack enderPearl = new ItemStack(Material.ENDER_PEARL);
                if (killer.getInventory().firstEmpty() == -1){
                    for (int i=0; i<killer.getInventory().getSize(); i++){
                        ItemStack item = killer.getInventory().getItem(i);
                        if (item != null){
                            if (item.getType() == Material.BOWL || item.getType() == Material.MUSHROOM_STEW){
                                killer.getInventory().setItem(i, enderPearl);
                                return;
                            }
                            if (item.isSimilar(enderPearl)){
                                killer.getInventory().setItem(i, enderPearl);
                                return;
                            }
                        }
                    }
                } else {
                    killer.getInventory().addItem(enderPearl);
                }
                for(ItemStack item : killer.getInventory().getArmorContents()) {
                    if(item != null) {
                        item.setDurability((short) (Math.min(item.getType().getMaxDurability(), item.getDurability() - 10)));
                    }
                }
                killer.updateInventory();
                int speed = killer.getActivePotionEffects().stream()
                        .filter(pot -> pot.getType() == PotionEffectType.SPEED && pot.getAmplifier() == 2)
                        .mapToInt(PotionEffect::getDuration)
                        .sum() + (20 * 10);
                killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, speed, 2), true);
            }
        }

    }

}
