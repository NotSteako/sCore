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
import kami.gg.souppvp.util.particles.ParticleEffect;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AvatarKit extends Kit {

    @Override
    public String getName() {
        return "Avatar";
    }

    @Override
    public KitCategory getCategory() {
        return KitCategory.ALL;
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
        return new ItemBuilder(Material.INK_SAC).durability(12).build();
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Have the ability to control elements. Shoot water");
        description.add("&7guns to slow down enemies. Jump over enemies to set");
        description.add("&7them on fire.");
        return description;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(new ItemBuilder(Material.DIAMOND_SWORD).build());
        itemStacks.add(new ItemBuilder(Material.INK_SAC).name(CC.translate("&bWater Gun")).durability((short) 12).build());
        return itemStacks;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.IRON_BOOTS).build(),
                new ItemBuilder(Material.CHAINMAIL_LEGGINGS).build(),
                new ItemBuilder(Material.IRON_CHESTPLATE).build(),
                new ItemBuilder(Material.CHAINMAIL_HELMET).build()
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

    ArrayList<BlockState> toRollback = new ArrayList<>();

    public ArrayList<Location> getSurroundingLocations(Location location) {
        ArrayList<Location> list = new ArrayList<Location>();
        list.add(location.clone().add(1.0D, 1.0D, -1.0D));
        list.add(location.clone().add(-1.0D, 1.0D, -1.0D));
        list.add(location.clone().add(1.0D, 1.0D, 1.0D));
        list.add(location.clone().add(-1.0D, 1.0D, 1.0D));
        list.add(location.clone().add(0.0D, 1.0D, 0.0D));
        list.add(location.clone().add(-1.0D, 1.0D, 0.0D));
        list.add(location.clone().add(1.0D, 1.0D, 0.0D));
        list.add(location.clone().add(0.0D, 1.0D, -1.0D));
        list.add(location.clone().add(0.0D, 1.0D, 1.0D));

        list.add(location.clone().add(-1.0D, 1.0D, 0.0D));
        list.add(location.clone().add(0.0D, 1.0D, 1.0D));
        list.add(location.clone().add(0.0D, 1.0D, -1.0D));
        list.add(location.clone().add(1.0D, 1.0D, -1.0D));
        list.add(location.clone().add(1.0D, 1.0D, 1.0D));
        list.add(location.clone().add(-0.0D, 1.0D, 1.0D));
        list.add(location.clone().add(-1.0D, 1.0D, -1.0D));
        list.add(location.clone().add(1.0D, 1.0D, -1.0D));
        list.add(location.clone().add(1.0D, 1.0D, 0.0D));
        return list;
    }

    public void returnBack(BlockState blockState) {
        if (blockState instanceof Sign) {
            final Sign sign = (Sign)blockState;
            final Location location = sign.getLocation();
            location.getWorld().getBlockAt(location).setType(blockState.getType());
            final Sign sign2 = (Sign)location.getWorld().getBlockAt(location).getState();
            for (int i = 0; i < 4; ++i) {
                sign2.setLine(i, sign.getLines()[i]);
            }
            sign2.update(true);
        }
        else {
            blockState.update(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player) {
            Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Avatar");
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getEntity().getUniqueId());
            if(profile.getCurrentKit() != null && profile.getCurrentKit().equals(kit)) {
                if(event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    event.setDamage(event.getDamage() / 2.2);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player killer = event.getEntity().getKiller();
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Avatar");
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(killer.getUniqueId());
        if(profile.getCurrentKit() != null && profile.getCurrentKit().equals(kit)) {
            for(ItemStack item : killer.getInventory().getArmorContents()) {
                if(item != null) {
                    item.setDurability((short) (Math.min(item.getType().getMaxDurability(), item.getDurability() - 15)));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Avatar");
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (item != null && item.isSimilar(this.getCombatEquipments().get(1)) && profile.getCurrentKit() != null && profile.getCurrentKit().equals(kit)) {

                if (SoupPvP.getInstance().getTimersHandler().hasTimer(player.getUniqueId(), "Water Gun", true)) {
                    player.sendMessage(ChatColor.RED + "You can't use this for another " + ChatColor.YELLOW + DurationFormatter.getRemaining(SoupPvP.getInstance().getTimersHandler().getRemaining(player.getUniqueId(), "Water Gun", true), true) + ChatColor.RED + ".");
                    return;
                }
                @SuppressWarnings("deprecation")
                FallingBlock block = event.getPlayer().getWorld().spawnFallingBlock(player.getEyeLocation(), Material.RED_STAINED_GLASS, (byte) 3);
                block.setMetadata("avatar", new FixedMetadataValue(SoupPvP.getInstance(), player.getUniqueId()));
                block.setDropItem(false);
                player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1F, 1F);

                block.setVelocity(event.getPlayer().getEyeLocation().getDirection().multiply(2.3).add(new Vector(0, 0.3, 0)));

                new BukkitRunnable() {

                    @Override
                    public void run() {
                        if (block.isDead() || !block.isValid() || !player.isOnline()) {
                            cancel();
                            return;
                        }

                        for(Entity entity : block.getNearbyEntities(3, 3, 3)) {
                            if (entity instanceof Player) {
                                Profile profile1 = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getPlayer().getUniqueId());
                                if(entity.getUniqueId().equals(player.getUniqueId()) || profile1.getProfileState() == ProfileState.SPAWN) {
                                    continue;
                                }

                                block.remove();
                                cancel();

                                Player found = (Player) entity;
                                found.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 4));

                                Location location = found.getLocation();
                                ArrayList<BlockState> blockList = new ArrayList<>();
                                Block block = location.getBlock();

                                if(block.getType() == Material.WATER || block.getType() == Material.WATER || block.getType() == Material.LAVA || block.getType() == Material.LAVA) {
                                    location = location.add(0, 1.0D, 0);
                                }

                                for(Location scan : getSurroundingLocations(location)) {
                                    if(scan.getBlock().getType().equals(Material.WATER)) {
                                        continue;
                                    }

                                    if(scan.getBlock().getType() != Material.AIR) continue;

                                    blockList.add(scan.getBlock().getState());
                                }

                                for (BlockState blockState : blockList) {
                                    toRollback.add(blockState);
                                    blockState.getBlock().setType(Material.WATER);
                                }

                                Bukkit.getScheduler().scheduleSyncDelayedTask(SoupPvP.getInstance(), () -> {
                                    for (BlockState blockState : blockList) {
                                        returnBack(blockState);
                                        toRollback.remove(blockState);
                                    }
                                }, 120L);

                                break;
                            }
                        }
                    }
                }.runTaskTimer(SoupPvP.getInstance(), 2L, 2L);
                SoupPvP.getInstance().getTimersHandler().addPlayerTimer(player.getUniqueId(), new Timer("Water Gun", TimeUnit.SECONDS.toMillis(30)), true);
                XPBarTimer.runXpBar(player, 30);
            }
        }
    }


    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Avatar");
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) {
            return;
        }
        if (event.isSneaking() && (profile.getCurrentKit() != null && profile.getCurrentKit().equals(kit) && !player.isOnGround())) {
            if (SoupPvP.getInstance().getTimersHandler().hasTimer(player.getUniqueId(), "Avatar Jump", false)) {
                player.sendMessage(ChatColor.RED + "You can't use this for another " + ChatColor.YELLOW + DurationFormatter.getRemaining(SoupPvP.getInstance().getTimersHandler().getRemaining(player.getUniqueId(), "Avatar Jump", false), true) + ChatColor.RED + ".");
                return;
            }
            SoupPvP.getInstance().getTimersHandler().addPlayerTimer(player.getUniqueId(), new Timer("Avatar Jump", TimeUnit.SECONDS.toMillis(15)), false);
            player.setVelocity(event.getPlayer().getEyeLocation().getDirection().multiply(1.9));
            player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1F, 1F);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if(!player.isOnline() || player.isOnGround()) {
                        cancel();
                        return;
                    }

                    player.getWorld().playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);

                    player.getNearbyEntities(1.0, 1.0, 1.0).stream()
                            .filter(entity -> entity instanceof Player)
                            .filter(entity -> entity.getFireTicks() <= 0)
                            .forEach(entity -> entity.setFireTicks(20 * 5));
                }
            }.runTaskTimer(SoupPvP.getInstance(), 0L, 1L);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("avatar")) {
            try {
                ParticleEffect.SMOKE_NORMAL.sendToPlayers(Bukkit.getOnlinePlayers(), player.getLocation(), 0, 0, 0, 0, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (player.getLocation().subtract(0, 1, 0).getBlock().getType() != Material.AIR) {
                player.removeMetadata("avatar", SoupPvP.getInstance());
            }
        }
    }


    @EventHandler
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.FALLING_BLOCK && event.getEntity().hasMetadata("avatar")) {
            event.getEntity().remove();
            event.setCancelled(true);

        }
    }

}
