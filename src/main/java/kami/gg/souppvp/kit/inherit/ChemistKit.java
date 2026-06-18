package kami.gg.souppvp.kit.inherit;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.KitCategory;
import kami.gg.souppvp.kit.KitRarity;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class ChemistKit extends Kit {

    @Override
    public String getName() {
        return "Chemist";
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
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override
    public ItemStack getIcon() {
        ItemStack potion = new ItemStack(Material.SPLASH_POTION);

        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setBasePotionType(PotionType.HARMING);
        potion.setItemMeta(meta);

        return potion;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Begin with splash potions of instant damage and");
        description.add("&7poisons. After each kill, get a refill of splash");
        description.add("&7potions to continue irritating enemies.");

        return description;
    }



    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> items = new ArrayList<>();

        items.add(new ItemBuilder(Material.IRON_SWORD)
                .enchantment(Enchantment.SHARPNESS, 1)
                .enchantment(Enchantment.UNBREAKING, 3)
                .build());

        ItemStack poison = new ItemStack(Material.SPLASH_POTION, 2);
        PotionMeta poisonMeta = (PotionMeta) poison.getItemMeta();
        poisonMeta.setBasePotionType(PotionType.POISON);
        poisonMeta.setMaxStackSize(2);
        poison.setItemMeta(poisonMeta);

        ItemStack harming = new ItemStack(Material.SPLASH_POTION, 1);
        PotionMeta harmingMeta = (PotionMeta) harming.getItemMeta();
        harmingMeta.setBasePotionType(PotionType.HARMING);
        harmingMeta.setMaxStackSize(2);
        harming.setItemMeta(harmingMeta);

        items.add(poison);
        items.add(harming);

        return items;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.CHAINMAIL_BOOTS).enchantment(Enchantment.PROTECTION, 1).build(),
                new ItemBuilder(Material.CHAINMAIL_LEGGINGS).enchantment(Enchantment.PROTECTION, 1).build(),
                new ItemBuilder(Material.IRON_CHESTPLATE).enchantment(Enchantment.PROTECTION, 1).build(),
                new ItemBuilder(Material.CHAINMAIL_HELMET).enchantment(Enchantment.PROTECTION, 1).build()
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

    private void givePotion(Player player, PotionType type, int amount) {
        Inventory inventory = player.getInventory();

        // Try to stack with existing potion first
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() != Material.SPLASH_POTION) {
                continue;
            }

            if (!(item.getItemMeta() instanceof PotionMeta meta)) {
                continue;
            }

            if (meta.getBasePotionType() == type) {
                item.setAmount(Math.min(item.getAmount() + amount, 2));
                return;
            }
        }

        ItemStack potion = new ItemStack(Material.SPLASH_POTION, amount);

        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setBasePotionType(type);
        meta.setMaxStackSize(2);
        potion.setItemMeta(meta);

        // Empty slot available
        if (inventory.firstEmpty() != -1) {
            inventory.addItem(potion);
            return;
        }

        // Replace bowl or soup if inventory is full
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);

            if (item == null) {
                continue;
            }

            if (item.getType() == Material.BOWL
                    || item.getType() == Material.MUSHROOM_STEW) {

                inventory.setItem(i, potion);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();

        if (killer == null) {
            return;
        }

        Profile profile = SoupPvP.getInstance()
                .getProfilesHandler()
                .getProfileByUUID(killer.getUniqueId());

        if (profile == null
                || profile.isInEvent()
                || profile.getProfileState() == ProfileState.SPAWN) {
            return;
        }

        Kit chemist = SoupPvP.getInstance()
                .getKitsHandler()
                .getKitByName("Chemist");

        if (!chemist.equals(profile.getCurrentKit())) {
            return;
        }

        int poisonCount = 0;
        int harmingCount = 0;

        for (ItemStack item : killer.getInventory().getContents()) {
            if (item == null || item.getType() != Material.SPLASH_POTION) {
                continue;
            }

            if (!(item.getItemMeta() instanceof PotionMeta meta)) {
                continue;
            }

            switch (meta.getBasePotionType()) {
                case POISON -> poisonCount += item.getAmount();
                case HARMING -> harmingCount += item.getAmount();
            }
        }

        // Restore to exactly 2 poison and 1 harming
        if (poisonCount < 2) {
            givePotion(killer, PotionType.POISON, 2 - poisonCount);
        }

        if (harmingCount < 1) {
            givePotion(killer, PotionType.HARMING, 1 - harmingCount);
        }
    }
}
