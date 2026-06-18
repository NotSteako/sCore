package kami.gg.souppvp.killstreak.inherit;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.killstreak.Killstreak;
import kami.gg.souppvp.perk.Perk;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ThirtyKillstreak extends Killstreak implements Listener {

    @Getter
    private HashMap<UUID, List<Wolf>> hashMap = new HashMap<>();

    @Override
    public String getName() {
        return "Attack Dogs";
    }

    @Override
    public int getRequired() {
        return 30;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.BAT_SPAWN_EGG)
                .durability(95)
                .name(CC.translate("&a" + getName()))
                .lore(Arrays.asList(CC.MENU_BAR, CC.translate("&7Spawns a squad of loyal wild wolves,"), CC.translate("&7protecting you until they die."), CC.MENU_BAR, "", CC.translate("&fKillstreak Required: &d" + getRequired()), "")).build();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeathEvent(PlayerDeathEvent event){
        if (event.getEntity().getKiller() == null) return;
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getEntity().getKiller().getUniqueId());
        Perk hardlinePerk = SoupPvP.getInstance().getPerksHandler().getPerkByName("Hardline");
        if (SoupPvP.getInstance().getPerksHandler().getPerkByName(profile.getActivePerks().get(1)) == hardlinePerk){
            if (profile.getCurrentKillstreak() == getRequired()-1){
                event.getEntity().getKiller().sendMessage(CC.translate("&aYou've received the &d" + getName() + " &aperk for reaching a &d" + getRequired() + " &akillstreak!"));
                List<Wolf> wolves = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    Wolf wolf = event.getEntity().getKiller().getWorld().spawn(event.getEntity().getKiller().getLocation(), Wolf.class);
                    wolf.setOwner(event.getEntity().getKiller());
                    wolf.setCustomName(CC.translate("&cAttack Dog"));
                    wolf.setTamed(true);
                    wolf.setAgeLock(true);
                    wolf.setAdult();
                    wolf.setAngry(true);
                    wolf.setMaxHealth(200);
                    wolf.setHealth(wolf.getMaxHealth());
                    wolf.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1));
                    wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                    wolf.setAngry(true);
                    wolves.add(wolf);
                }
                hashMap.put(event.getEntity().getKiller().getUniqueId(), wolves);
            }
        } else {
            if (profile.getCurrentKillstreak() == getRequired()){
                event.getEntity().getKiller().sendMessage(CC.translate("&aYou've received the &d" + getName() + " &aperk for reaching a &d" + getRequired() + " &akillstreak!"));
                List<Wolf> wolves = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    Wolf wolf = event.getEntity().getKiller().getWorld().spawn(event.getEntity().getKiller().getLocation(), Wolf.class);
                    wolf.setOwner(event.getEntity().getKiller());
                    wolf.setCustomName(CC.translate("&cAttack Dog"));
                    wolf.setTamed(true);
                    wolf.setAgeLock(true);
                    wolf.setAdult();
                    wolf.setAngry(true);
                    wolf.setMaxHealth(200);
                    wolf.setHealth(wolf.getMaxHealth());
                    wolf.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1));
                    wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                    wolf.setAngry(true);
                    wolves.add(wolf);
                }
                hashMap.put(event.getEntity().getKiller().getUniqueId(), wolves);
            }
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event){
        if (hashMap.isEmpty()) return;
        if (hashMap.containsKey(event.getPlayer().getUniqueId())){
            for (Wolf wolf : hashMap.get(event.getPlayer().getUniqueId())){
                wolf.remove();
            }
            hashMap.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerDeathEventRemoveFromHashmap(PlayerDeathEvent event){
        if (hashMap.isEmpty()) return;
        if (hashMap.containsKey(event.getEntity().getUniqueId())){
            for (Wolf wolf : hashMap.get(event.getEntity().getUniqueId())){
                wolf.remove();
            }
            hashMap.remove(event.getEntity().getUniqueId());
        }
    }

}
