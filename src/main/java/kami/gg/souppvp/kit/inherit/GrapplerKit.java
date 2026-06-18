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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class GrapplerKit extends Kit {

    @Override
    public String getName() {
        return "Grappler";
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
        return new ItemBuilder(Material.FISHING_ROD).build();
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Have the ability to take daily vacations. Using your grappler, you can");
        description.add("&7hook yourself onto locations and can travel a vast distance if accurate.");
        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.IRON_SWORD).enchantment(Enchantment.SHARPNESS, 1).enchantment(Enchantment.UNBREAKING, 3).build());
        itemStacks.add(new ItemBuilder(Material.FISHING_ROD).name(CC.translate("&aGrappler")).build());
        return itemStacks;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.IRON_BOOTS).build(),
                new ItemBuilder(Material.CHAINMAIL_LEGGINGS).enchantment(Enchantment.UNBREAKING, 3).build(),
                new ItemBuilder(Material.GOLDEN_CHESTPLATE).enchantment(Enchantment.UNBREAKING, 3).build(),
                new ItemBuilder(Material.CHAINMAIL_HELMET).build()
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
    public void onGrappleLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player && event.getEntity().getType() == EntityType.FISHING_BOBBER){
            Player shooter = (Player) event.getEntity().getShooter();
            Profile shooterProfile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(shooter.getUniqueId());
            Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Grappler");
            if (shooterProfile.isInEvent() || shooterProfile.getProfileState() == ProfileState.SPAWN) return;
            if (shooterProfile.getCurrentKit().equals(kit)){
                if (SoupPvP.getInstance().getTimersHandler().hasTimer(shooter.getUniqueId(), "Grappler", true)) {
                    shooter.sendMessage(ChatColor.RED + "You can't use this for another " + ChatColor.YELLOW + DurationFormatter.getRemaining(SoupPvP.getInstance().getTimersHandler().getRemaining(shooter.getUniqueId(), "Grappler", true), true) + ChatColor.RED + ".");
                    event.setCancelled(true);
                }
            }
        }
    }

    private final Set<UUID> grapplerNoFall = new HashSet<>();
    @EventHandler
    public void onPlayerFishEvent(PlayerFishEvent event) {

        Player player = event.getPlayer();
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Grappler");
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;
        if (profile.getCurrentKit().equals(kit)){
            if (event.getPlayer().getItemInHand().getType() == this.getCombatEquipments().get(1).getType()) {
                if (event.getHook().isValid()) {
                    if (event.getState() == PlayerFishEvent.State.FISHING) {
                        return;
                    }
                    final Entity hooked = event.getHook();
                    if (hooked == null) {
                        return;
                    }
                    if (event.getCaught() != null) {
                        event.getHook().remove();
                        return;
                    }
                    final Location location = player.getLocation();
                    final Location hookedLocation = hooked.getLocation();
                    final Vector velocity = new Vector(
                            hookedLocation.getX() - location.getX(),
                            hookedLocation.getY() - location.getY() + 2.5,
                            hookedLocation.getZ() - location.getZ()
                    ).multiply(0.25);


                    player.setVelocity(velocity);

                    grapplerNoFall.add(player.getUniqueId());

                    Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
                        grapplerNoFall.remove(player.getUniqueId());
                    }, 60L); // adjust duration (3 seconds)

                    player.setFallDistance(0f);
                    hooked.remove();
                    SoupPvP.getInstance().getTimersHandler().addPlayerTimer(player.getUniqueId(), new Timer("Grappler", TimeUnit.SECONDS.toMillis(30)), true);
                    XPBarTimer.runXpBar(player, 30);
                    PlayerUtil.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
                }
            }
        }

    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        if (grapplerNoFall.remove(player.getUniqueId())) {
            event.setCancelled(true);
            player.setFallDistance(0f);
        }
    }
}
