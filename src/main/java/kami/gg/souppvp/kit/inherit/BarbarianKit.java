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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BarbarianKit extends Kit {

    @Override
    public String getName() {
        return "Barbarian";
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
        return new ItemBuilder(Material.INK_SAC).durability(6).build();
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Although you may be seen as vulnerable without");
        description.add("&7armour, spawn swarms on silverfish to torture enemies");
        description.add("&7and deal large heaps of damage.");
        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.STONE_SWORD).enchantment(Enchantment.SHARPNESS, 2).enchantment(Enchantment.UNBREAKING, 10).build());
        itemStacks.add(new ItemBuilder(Material.INK_SAC).name(CC.translate("&9Silverfish Swarm")).durability((short) 6).build());
        return itemStacks;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[4];
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        List<PotionEffect> potionEffects = new ArrayList<>();
        potionEffects.add(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 2));
        return potionEffects;
    }

    @Override
    public void onSelect(Player player) {

    }

    @EventHandler
    public void onPlayerInteract (PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Barbarian");
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (item != null && item.isSimilar(this.getCombatEquipments().get(1)) && profile.getCurrentKit() != null && profile.getCurrentKit().equals(kit)) {
                if (profile.getProfileState() == ProfileState.SPAWN && SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player)) {
                    player.sendMessage(CC.translate("&cYou can't do this in Spawn."));
                    return;
                }
                if (SoupPvP.getInstance().getTimersHandler().hasTimer(player.getUniqueId(), "Silverfish Swarm", true)) {
                    player.sendMessage(ChatColor.RED + "You can't use this for another " + ChatColor.YELLOW + DurationFormatter.getRemaining(SoupPvP.getInstance().getTimersHandler().getRemaining(player.getUniqueId(), "Silverfish Swarm", true), true) + ChatColor.RED + ".");
                    return;
                }

                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 6, 0), true);

                Player closest = null;
                double lastDistance = 0;

                for(Entity entity : player.getNearbyEntities(10, 10, 10)) {
                    if(!(entity instanceof Player)) {
                        continue;
                    }

                    Player possiblePlayer = (Player) entity;

                    Profile profile1 = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getPlayer().getUniqueId());
                    if(player.getUniqueId().equals(possiblePlayer.getUniqueId()) || profile1.getProfileState() == ProfileState.SPAWN) {
                        continue;
                    }

                    double possibleNewDistance = player.getLocation().distance(possiblePlayer.getLocation());

                    if(closest == null) {
                        closest = possiblePlayer;
                        lastDistance = possibleNewDistance;
                        continue;
                    }

                    if(possibleNewDistance < lastDistance) {
                        closest = possiblePlayer;
                        lastDistance = possibleNewDistance;
                    }
                }

                for(int i = 0; i < 4; i++) {
                    Entity entity = player.getWorld().spawnEntity(player.getLocation(), EntityType.SILVERFISH);
                    entity.setMetadata("owner", new FixedMetadataValue(SoupPvP.getInstance(), player.getUniqueId().toString()));
                    ((Silverfish) entity).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 10, 3));
                    if(closest != null) {
                        ((Silverfish) entity).setTarget(closest);
                    }

                    Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
                        if(entity.isValid()) {
                            entity.remove();
                        }
                    }, 20L * 10); // remove after 5 seconds
                }

                player.playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 1F, 1F);
                SoupPvP.getInstance().getTimersHandler().addPlayerTimer(player.getUniqueId(), new Timer("Silverfish Swarm", TimeUnit.SECONDS.toMillis(35)), true);
                XPBarTimer.runXpBar(player, 35);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Silverfish && event.getDamager() instanceof Player) {
            Silverfish silverfish = (Silverfish) event.getEntity();
            Player player = (Player) event.getDamager();

            if(silverfish.hasMetadata("owner") && UUID.fromString(silverfish.getMetadata("owner").get(0).asString()).equals(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot damage your own silverfish.");
            }
        } else if(event.getEntity() instanceof Player && event.getDamager() instanceof Silverfish) {
            Player player = (Player) event.getEntity();
            Silverfish silverfish = (Silverfish) event.getDamager();

            event.setCancelled(true);

            if(silverfish.hasMetadata("owner")) {
                Player owner = Bukkit.getPlayer(UUID.fromString(silverfish.getMetadata("owner").get(0).asString()));

                if(owner != null) {
                    player.damage(4, owner);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        event.setDroppedExp(0);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if(event.getEntity() instanceof Silverfish
                && event.getTarget() instanceof Player
                && event.getEntity().hasMetadata("owner")
                && UUID.fromString(event.getEntity().getMetadata("owner").get(0).asString()).equals(event.getTarget().getUniqueId())) {
            event.setCancelled(true);
        }
    }

}
