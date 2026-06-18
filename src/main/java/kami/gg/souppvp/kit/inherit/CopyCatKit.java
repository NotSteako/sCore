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
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class CopyCatKit extends Kit {

    @Override
    public String getName() {
        return "CopyCat";
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
        return new ItemBuilder(Material.BAT_SPAWN_EGG).durability(98).build();
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Start with the Default kit, then after");
        description.add("&7every kill, you will receive your victim's kit.");

        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.DIAMOND_SWORD).enchantment(Enchantment.SHARPNESS, 1).enchantment(Enchantment.UNBREAKING, 3).build());
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
        player.setMetadata("CopyCat", new FixedMetadataValue(SoupPvP.getInstance(), null));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() != null){
            Player player = event.getEntity();
            Player killer = event.getEntity().getKiller();
            Profile killerProfile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(killer.getUniqueId());
            Profile deathProfile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
            Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("CopyCat");
            if (killerProfile.isInEvent() || killerProfile.getProfileState() == ProfileState.SPAWN) return;
            if (killerProfile.getCurrentKit() == kit){
                if (!killerProfile.getUnlockedKits().contains(deathProfile.getCurrentKit().getName())){
                    killer.sendMessage(CC.translate("&cFailed to CopyCat " + player.getName() + "'s " + deathProfile.getCurrentKit().getName() + " kit."));
                    killer.playSound(killer.getLocation(), Sound.BLOCK_GRASS_BREAK, 1F, 1F);
                    return;
                }
                killer.getInventory().clear();
                killerProfile.setCurrentKit(deathProfile.getCurrentKit());
                killerProfile.setPreviousKit(kit);
                deathProfile.getCurrentKit().equipKit(killer);
                killer.playSound(killer.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1F, 1F);
                Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), killerProfile::saveProfile, 10L);
                Bukkit.getScheduler().scheduleSyncDelayedTask(SoupPvP.getInstance(), () -> {
                    if (player.hasMetadata("CopyCat")) {
                        player.playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, 1F, 1F);
                    }
                }, 40L);
            }
        }
    }

}
