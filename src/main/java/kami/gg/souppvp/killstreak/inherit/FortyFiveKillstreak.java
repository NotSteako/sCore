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
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class FortyFiveKillstreak extends Killstreak implements Listener {

    @Getter
    private HashMap<UUID, Snowman> hashMap = new HashMap<>();

    @Override
    public String getName() {
        return "Angry Snowman";
    }

    @Override
    public int getRequired() {
        return 45;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.SNOWBALL)
                .name(CC.translate("&a" + getName()))
                .lore(Arrays.asList(CC.MENU_BAR, CC.translate("&7Spawns a Angry Snowman which shoots"), CC.translate("&7strong snowballs for 5 minutes."), CC.MENU_BAR, "", CC.translate("&fKillstreak Required: &d" + getRequired()), "")).build();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeathEvent(PlayerDeathEvent event){
        if (event.getEntity().getKiller() == null) return;
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getEntity().getKiller().getUniqueId());
        Perk hardlinePerk = SoupPvP.getInstance().getPerksHandler().getPerkByName("Hardline");
        if (SoupPvP.getInstance().getPerksHandler().getPerkByName(profile.getActivePerks().get(1)) == hardlinePerk){
            if (profile.getCurrentKillstreak() == getRequired()-1){
                event.getEntity().getKiller().sendMessage(CC.translate("&aYou've received the &d" + getName() + " &aperk for reaching a &d" + getRequired() + " &akillstreak!"));
                Player closest = null;
                double lastDistance = 0;
                for(Entity entity : event.getEntity().getKiller().getNearbyEntities(10, 10, 10)) {
                    if(!(entity instanceof Player)) {
                        continue;
                    }
                    Player possiblePlayer = (Player) entity;
                    if(event.getEntity().getKiller().getUniqueId().equals(possiblePlayer.getUniqueId())) {
                        continue;
                    }
                    double possibleNewDistance = event.getEntity().getKiller().getLocation().distance(possiblePlayer.getLocation());
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
                Snowman snowman = (Snowman) event.getEntity().getKiller().getWorld().spawnEntity(event.getEntity().getKiller().getLocation(), EntityType.SNOW_GOLEM);
                snowman.setMetadata("owner", new FixedMetadataValue(SoupPvP.getInstance(), event.getEntity().getKiller().getUniqueId().toString()));
                snowman.setCustomName(CC.translate("&b&l" + event.getEntity().getKiller().getName() + "'s Angry Snowman"));
                snowman.setMaxHealth(500);
                snowman.setHealth(snowman.getMaxHealth());
                hashMap.put(event.getEntity().getKiller().getUniqueId(), snowman);
                if(closest != null) {
                    snowman.setTarget(closest);
                }
                Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
                    if(snowman.isValid()) {
                        snowman.remove();
                    }
                }, 20L * 60 * 5); // remove after 5 minutes
            }
        } else {
            if (profile.getCurrentKillstreak() == getRequired()){
                event.getEntity().getKiller().sendMessage(CC.translate("&aYou've received the &d" + getName() + " &aperk for reaching a &d" + getRequired() + " &akillstreak!"));
                Player closest = null;
                double lastDistance = 0;
                for(Entity entity : event.getEntity().getKiller().getNearbyEntities(10, 10, 10)) {
                    if(!(entity instanceof Player)) {
                        continue;
                    }
                    Player possiblePlayer = (Player) entity;
                    if(event.getEntity().getKiller().getUniqueId().equals(possiblePlayer.getUniqueId())) {
                        continue;
                    }
                    double possibleNewDistance = event.getEntity().getKiller().getLocation().distance(possiblePlayer.getLocation());
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
                Snowman snowman = (Snowman) event.getEntity().getKiller().getWorld().spawnEntity(event.getEntity().getKiller().getLocation(), EntityType.SNOW_GOLEM);
                snowman.setMetadata("owner", new FixedMetadataValue(SoupPvP.getInstance(), event.getEntity().getKiller().getUniqueId().toString()));
                snowman.setCustomName(CC.translate("&b&l" + event.getEntity().getKiller().getName() + "'s Angry Snowman"));
                snowman.setMaxHealth(500);
                snowman.setHealth(snowman.getMaxHealth());
                hashMap.put(event.getEntity().getKiller().getUniqueId(), snowman);
                if(closest != null) {
                    snowman.setTarget(closest);
                }
                Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
                    if(snowman.isValid()) {
                        snowman.remove();
                    }
                }, 20L * 60 * 5); // remove after 5 minutes
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Snowman && event.getDamager() instanceof Player) {
            Snowman snowman = (Snowman) event.getEntity();
            Player player = (Player) event.getDamager();

            if(snowman.hasMetadata("owner") && UUID.fromString(snowman.getMetadata("owner").get(0).asString()).equals(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot damage your own Angry Snowman.");
            }
        } else if(event.getEntity() instanceof Player && event.getDamager() instanceof Snowman) {
            Player player = (Player) event.getEntity();
            Snowman snowman = (Snowman) event.getDamager();

            event.setCancelled(true);

            if(snowman.hasMetadata("owner")) {
                Player owner = Bukkit.getPlayer(UUID.fromString(snowman.getMetadata("owner").get(0).asString()));

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
        if(event.getEntity() instanceof Snowman
                && event.getTarget() instanceof Player
                && event.getEntity().hasMetadata("owner")
                && UUID.fromString(event.getEntity().getMetadata("owner").get(0).asString()).equals(event.getTarget().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void snowball(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Player) {
            if(event.getDamager() instanceof Snowball) {
                Snowball snowball = (Snowball) event.getDamager();
                if(snowball.getShooter() instanceof Snowman) {
                    event.setDamage(4);
                }
            }
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
