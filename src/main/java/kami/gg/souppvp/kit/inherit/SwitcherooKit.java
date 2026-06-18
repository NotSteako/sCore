package kami.gg.souppvp.kit.inherit;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.KitCategory;
import kami.gg.souppvp.kit.KitRarity;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.timer.Timer;
import kami.gg.souppvp.util.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SwitcherooKit extends Kit {

    @Override
    public String getName() {
        return "Switcheroo";
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
        return new ItemBuilder(Material.SNOWBALL).build();
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Your main ability is being extremely sneaky. Catch your enemies off");
        description.add("&7guard and swap locations with them to give yourself a positional advantage");
        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.DIAMOND_SWORD).enchantment(Enchantment.SHARPNESS, 1).build());
        itemStacks.add(new ItemBuilder(Material.SNOWBALL).name(CC.translate("&9Switcheroo")).amount(3).build());
        return itemStacks;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.CHAINMAIL_BOOTS).build(),
                new ItemBuilder(Material.CHAINMAIL_LEGGINGS).build(),
                new ItemBuilder(Material.DIAMOND_CHESTPLATE).build(),
                new ItemBuilder(Material.IRON_HELMET).build()
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
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Switcheroo");
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;
        if (profile.getCurrentKit().equals(kit)){
            if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
                if (event.getPlayer().getItemInHand().isSimilar(this.getCombatEquipments().get(1))) {
                    if (profile.getProfileState() == ProfileState.SPAWN) {
                        player.sendMessage(CC.translate("&cYou can't do this in Spawn."));
                    }
                    if (SoupPvP.getInstance().getTimersHandler().hasTimer(player.getUniqueId(), "Switcheroo", true)) {
                        player.sendMessage(ChatColor.RED + "You can't use this for another " + ChatColor.YELLOW + DurationFormatter.getRemaining(SoupPvP.getInstance().getTimersHandler().getRemaining(player.getUniqueId(), "Switcheroo", true), true) + ChatColor.RED + ".");
                        event.setCancelled(true);
                        event.setUseItemInHand(Event.Result.DENY);
                        player.updateInventory();
                        return;
                    }
                    PlayerUtil.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING);
                } else {
                    return;
                }
                player.updateInventory();
            }
        }
    }

    @EventHandler
    public void onSnowballHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Snowball snowball)) {
            return;
        }

        if (!(event.getEntity() instanceof Player damaged)) {
            return;
        }

        if (!(snowball.getShooter() instanceof Player shooter)) {
            return;
        }

        Profile profile = SoupPvP.getInstance()
                .getProfilesHandler()
                .getProfileByUUID(shooter.getUniqueId());

        Kit kit = SoupPvP.getInstance()
                .getKitsHandler()
                .getKitByName("Switcheroo");

        if (profile == null || profile.isInEvent()) {
            return;
        }

        if (!profile.getCurrentKit().equals(kit)) {
            return;
        }

        if (SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(damaged)) {
            shooter.sendMessage(CC.translate("&cYou cannot switch players into spawn."));
            return;
        }

        if (SoupPvP.getInstance().getTimersHandler().hasTimer(shooter.getUniqueId(), "Switcheroo", true)) {
            return;
        }

        // Prevent snowball knockback/damage
        event.setCancelled(true);

        SoupPvP.getInstance().getTimersHandler().addPlayerTimer(
                shooter.getUniqueId(),
                new Timer("Switcheroo", TimeUnit.SECONDS.toMillis(45)),
                true
        );

        XPBarTimer.runXpBar(shooter, 45);

        Location shooterLoc = shooter.getLocation().clone();
        Location targetLoc = damaged.getLocation().clone();

        shooter.teleport(targetLoc);
        damaged.teleport(shooterLoc);

        PlayerUtil.playSound(shooter, Sound.ENTITY_CHICKEN_EGG);
        PlayerUtil.playSound(damaged, Sound.ENTITY_CHICKEN_EGG);

        snowball.remove();
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event){
        Player killer = event.getEntity().getKiller();
        Player target = event.getEntity();
        if (killer == null){
            return;
        }
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(killer.getUniqueId());
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Switcheroo");
        if (!profile.isInEvent()){
            if (profile.getCurrentKit().equals(kit)){
                for (ItemStack itemStack : killer.getInventory().getContents()){
                    if (itemStack.isSimilar(this.getCombatEquipments().get(1))){
                        if (itemStack.getAmount() == 3){
                            return;
                        }
                    } else {
                        killer.getInventory().setItem(1, this.getCombatEquipments().get(1));
                    }
                }
            }
        }
    }

}
