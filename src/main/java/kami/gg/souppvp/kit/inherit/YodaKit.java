package kami.gg.souppvp.kit.inherit;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.KitCategory;
import kami.gg.souppvp.kit.KitRarity;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.timer.Timer;
import kami.gg.souppvp.util.*;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class YodaKit extends Kit {

    @Override
    public String getName() {
        return "Yoda";
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
        return new ItemBuilder(Material.INK_SAC).durability(2).build();
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7With the capabilities of Yoda, the jet master,");
        description.add("&7create a pulling force to pull enemies towards you.");
        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.DIAMOND_SWORD).build());
        itemStacks.add(new ItemBuilder(Material.INK_SAC).durability(2).name(CC.translate("&aThe Force")).build());
        return itemStacks;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.IRON_BOOTS).build(),
                new ItemBuilder(Material.IRON_LEGGINGS).build(),
                new ItemBuilder(Material.IRON_CHESTPLATE).build(),
                new ItemBuilder(Material.LEATHER_HELMET).color(Color.GREEN).enchantment(Enchantment.UNBREAKING, 3).build()
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
    public void onPlayerInteractEvent(PlayerInteractEvent event){
        Player player = event.getPlayer();
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Yoda");
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;
        if (profile.getCurrentKit().equals(kit)){
            if (event.getPlayer().getItemInHand().isSimilar(this.getCombatEquipments().get(1)) && (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))){
                event.setCancelled(true);
                player.updateInventory();
                if (SoupPvP.getInstance().getTimersHandler().hasTimer(player.getUniqueId(), "The Force", true)) {
                    player.sendMessage(ChatColor.RED + "You can't use this for another " + ChatColor.YELLOW + DurationFormatter.getRemaining(SoupPvP.getInstance().getTimersHandler().getRemaining(player.getUniqueId(), "The Force", true), true) + ChatColor.RED + ".");
                    return;
                }
                SoupPvP.getInstance().getTimersHandler().addPlayerTimer(player.getUniqueId(), new Timer("The Force", TimeUnit.SECONDS.toMillis(45)), true);
                XPBarTimer.runXpBar(player, 45);
                PlayerUtil.playSound(player, Sound.ENTITY_ENDERMAN_STARE);
                for (Entity entity : player.getNearbyEntities(10, 10, 10)){
                    if (entity instanceof Player){
                        PlayerUtil.playSound((Player) entity, Sound.ENTITY_ENDERMAN_STARE);
                        if (SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(entity)) return;
                        ((Player) entity).sendMessage(CC.translate("&cYou are being pulled by The Force."));
                    }
                }
                Location location = player.getLocation();
                new BukkitRunnable() {
                    int i = 0;
                    @Override
                    public void run() {
                        if(i >= 50) {
                            cancel();
                        }
                        ++i;
                        for (Player player1 : Bukkit.getOnlinePlayers()){
                            if (player1.getLocation().distance(location) <= 10 && player1 != player){
                                Profile profile1 = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player1.getUniqueId());
                                if (profile1.getProfileState() == ProfileState.SPAWN){
                                    return;
                                }
                                moveToward(player1, location, 0.5);
                            }
                        }
                    }
                }.runTaskTimer(SoupPvP.getInstance(), 2L, 2L);
            }
        }
    }

    private void moveToward(Entity entity, Location to, double speed){
        Location loc = entity.getLocation();
        double x = loc.getX() - to.getX();
        double y = loc.getY() - to.getY();
        double z = loc.getZ() - to.getZ();
        Vector velocity = new Vector(x, y, z).normalize().multiply(-speed);
        entity.setVelocity(velocity);
    }

}
