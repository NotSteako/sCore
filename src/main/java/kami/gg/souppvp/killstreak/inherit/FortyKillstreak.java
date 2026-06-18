package kami.gg.souppvp.killstreak.inherit;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.killstreak.Killstreak;
import kami.gg.souppvp.perk.Perk;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class FortyKillstreak extends Killstreak implements Listener {

    @Getter private HashMap<UUID, Zombie> hashMap = new HashMap<>();

    @Override
    public String getName() {
        return "Security Guard";
    }

    @Override
    public int getRequired() {
        return 40;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.PLAYER_HEAD)
                .durability(3)
                .name(CC.translate("&a" + getName()))
                .lore(Arrays.asList(CC.MENU_BAR, CC.translate("&7Spawns a Security Guard with the only"), CC.translate("&7goal is to protect you."), CC.MENU_BAR, "", CC.translate("&fKillstreak Required: &d" + getRequired()), "")).build();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeathEvent(PlayerDeathEvent event){
        if (event.getEntity().getKiller() == null) return;
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getEntity().getKiller().getUniqueId());
        Perk hardlinePerk = SoupPvP.getInstance().getPerksHandler().getPerkByName("Hardline");
        if (SoupPvP.getInstance().getPerksHandler().getPerkByName(profile.getActivePerks().get(1)) == hardlinePerk){
            if (profile.getCurrentKillstreak() == getRequired()-1){
                event.getEntity().getKiller().sendMessage(CC.translate("&aYou've received the &d" + getName() + " &aperk for reaching a &d" + getRequired() + " &akillstreak!"));
                Zombie zombie = (Zombie) event.getEntity().getKiller().getWorld().spawnEntity(event.getEntity().getKiller().getLocation(), EntityType.ZOMBIE);
                zombie.setMetadata("owner", new FixedMetadataValue(SoupPvP.getInstance(), event.getEntity().getKiller().getUniqueId().toString()));
                zombie.setMaxHealth(1000);
                zombie.setHealth(zombie.getMaxHealth());
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0));
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
                zombie.setCustomName(CC.translate("&b&l" + event.getEntity().getKiller().getName() + "'s Security Guard"));
                EntityEquipment entityEquipment = zombie.getEquipment();
                entityEquipment.setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.BLUE).build());
                entityEquipment.setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).color(Color.BLUE).build());
                entityEquipment.setBoots(new ItemBuilder(Material.LEATHER_BOOTS).color(Color.BLUE).build());
                hashMap.put(event.getEntity().getKiller().getUniqueId(), zombie);
            }
        } else {
            if (profile.getCurrentKillstreak() == getRequired()){
                event.getEntity().getKiller().sendMessage(CC.translate("&aYou've received the &d" + getName() + " &aperk for reaching a &d" + getRequired() + " &akillstreak!"));
                Zombie zombie = (Zombie) event.getEntity().getKiller().getWorld().spawnEntity(event.getEntity().getKiller().getLocation(), EntityType.ZOMBIE);
                zombie.setMetadata("owner", new FixedMetadataValue(SoupPvP.getInstance(), event.getEntity().getKiller().getUniqueId().toString()));
                zombie.setMaxHealth(1000);
                zombie.setHealth(zombie.getMaxHealth());
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0));
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
                zombie.setCustomName(CC.translate("&b&l" + event.getEntity().getKiller().getName() + "'s Security Guard"));
                EntityEquipment entityEquipment = zombie.getEquipment();
                entityEquipment.setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).color(Color.BLUE).build());
                entityEquipment.setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).color(Color.BLUE).build());
                entityEquipment.setBoots(new ItemBuilder(Material.LEATHER_BOOTS).color(Color.BLUE).build());
                hashMap.put(event.getEntity().getKiller().getUniqueId(), zombie);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Zombie && event.getDamager() instanceof Player) {
            Zombie zombie = (Zombie) event.getEntity();
            Player player = (Player) event.getDamager();

            if(zombie.hasMetadata("owner") && UUID.fromString(zombie.getMetadata("owner").get(0).asString()).equals(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot damage your own Security Guard.");
            }
        } else if(event.getEntity() instanceof Player && event.getDamager() instanceof Zombie) {
            Player player = (Player) event.getEntity();
            Zombie zombie = (Zombie) event.getDamager();

            event.setCancelled(true);

            if(zombie.hasMetadata("owner")) {
                Player owner = Bukkit.getPlayer(UUID.fromString(zombie.getMetadata("owner").get(0).asString()));

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
        if(event.getEntity() instanceof Zombie
                && event.getTarget() instanceof Player
                && event.getEntity().hasMetadata("owner")
                && UUID.fromString(event.getEntity().getMetadata("owner").get(0).asString()).equals(event.getTarget().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event){
        if (hashMap.isEmpty()) return;
        if (hashMap.containsKey(event.getPlayer().getUniqueId())){
            if (event.getPlayer().getLocation().distance(hashMap.get(event.getPlayer().getUniqueId()).getLocation()) > 15){
                hashMap.get(event.getPlayer().getUniqueId()).teleport(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event){
        if (hashMap.isEmpty()) return;
        if (hashMap.containsKey(event.getPlayer().getUniqueId())){
            hashMap.get(event.getPlayer().getUniqueId()).remove();
            hashMap.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerDeathEventRemoveFromHashmap(PlayerDeathEvent event){
        if (hashMap.isEmpty()) return;
        if (hashMap.containsKey(event.getEntity().getUniqueId())){
            hashMap.get(event.getEntity().getUniqueId()).remove();
            hashMap.remove(event.getEntity().getUniqueId());
        }
    }

}
