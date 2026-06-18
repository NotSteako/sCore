package kami.gg.souppvp.kit.inherit;

import com.google.common.collect.Lists;
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
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class StomperKit extends Kit {

    @Override
    public String getName() {
        return "Stomper";
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
        return new ItemBuilder(Material.ANVIL).build();
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7You depend on locations with high altitudes. You take no fall damage");
        description.add("&7and for each fall damage taken, you deal that amount to whoever you are");
        description.add("&7stomping on top off.");
        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.IRON_SWORD).enchantment(Enchantment.SHARPNESS, 1).enchantment(Enchantment.UNBREAKING, 1).build());
        itemStacks.add(new ItemBuilder(Material.ANVIL).name(CC.translate("&6Stomper")).build());
        return itemStacks;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.IRON_BOOTS).build(),
                new ItemBuilder(Material.GOLDEN_LEGGINGS).enchantment(Enchantment.PROTECTION, 1).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.GOLDEN_CHESTPLATE).enchantment(Enchantment.PROTECTION, 1).enchantment(Enchantment.UNBREAKING, 10).build(),
                new ItemBuilder(Material.LEATHER_HELMET).enchantment(Enchantment.PROTECTION, 1).enchantment(Enchantment.UNBREAKING, 10).build()
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

    public static final List<UUID> usedStomper = Lists.newArrayList();
    public static final List<UUID> usedShift = Lists.newArrayList();

    @EventHandler
    public void onWater(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Stomper");
        if (event.getTo().getBlock().isLiquid() && profile.getCurrentKit() == kit) {
            usedStomper.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Stomper");
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (item != null && item.getType() == Material.ANVIL && item.hasItemMeta() && item.isSimilar(this.getCombatEquipments().get(1)) && profile.getCurrentKit() == kit) {
                if (profile.getProfileState() == ProfileState.SPAWN && SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player)) {
                    player.sendMessage(CC.translate("&cYou can't do this in Spawn."));
                    return;
                }

                if (SoupPvP.getInstance().getTimersHandler().hasTimer(player.getUniqueId(), "Stomper Charge", true)) {
                    player.sendMessage(ChatColor.RED + "You can't use this for another " + ChatColor.YELLOW + DurationFormatter.getRemaining(SoupPvP.getInstance().getTimersHandler().getRemaining(player.getUniqueId(), "Stomper Charge", true), true) + ChatColor.RED + ".");
                    return;
                }

                usedStomper.add(player.getUniqueId());
                usedShift.remove(player.getUniqueId());

                event.getPlayer().setVelocity(new Vector(0, 1.7, 0));

                SoupPvP.getInstance().getTimersHandler().addPlayerTimer(player.getUniqueId(), new Timer("Stomper Charge", TimeUnit.SECONDS.toMillis(25)), true);
                XPBarTimer.runXpBar(player, 25);
                player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1F, 1F);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
            Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Stomper");
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL && profile.getCurrentKit() == kit) {
                this.usedStomper.remove(player.getUniqueId());

                if (this.usedShift.contains(player.getUniqueId())) {
                    for (Entity entity : player.getNearbyEntities(3, 3, 3)) {
                        if (entity instanceof LivingEntity) {
                            Profile profile1 = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(entity.getUniqueId());
                            boolean inSpawn = profile1.getProfileState() == ProfileState.SPAWN && SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player);

                            if (inSpawn) {
                                continue;
                            }

                            ((LivingEntity) entity).damage(((Player) entity).isSneaking() ? ((Player) entity).isBlocking() ? 6 : 12 : event.getDamage(), player);
                        }
                    }

                    event.setCancelled(true);

                    this.usedShift.remove(player.getUniqueId());

                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                    player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, 2);
                } else {
                    event.setDamage(event.getDamage() / 2.0);
                }
            }
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Stomper");
        if (event.isSneaking() && profile.getCurrentKit() == kit) {
            if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) {
                return;
            }
            if (profile.getProfileState() == ProfileState.SPAWN && SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player))
                return; // Prevents players getting a cooldown in Spawn monkaS

            if (player.getLocation().getBlockY() >= 176) {
                player.sendMessage(ChatColor.RED + "Stomper is blocked at this y level.");
                return;
            }

            if (!this.usedStomper.contains(player.getUniqueId())) {
                return;
            }

            player.setVelocity(new Vector(0, -6, 0));

            this.usedStomper.remove(player.getUniqueId());
            this.usedShift.add(player.getUniqueId());
        }
    }

}
