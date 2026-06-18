package kami.gg.souppvp.timer;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.command.admin.CooldownCommand;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.PlayerUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * @author hieu
 * @date 24/06/2023
 */

public class TimersHandler {

    @Getter public HashMap<UUID, Timer> primaryAbilitiesHashMap;
    @Getter public HashMap<UUID, Timer> secondaryAbilitiesHashMap;

    public TimersHandler(){
        this.primaryAbilitiesHashMap = new HashMap<>();
        this.secondaryAbilitiesHashMap = new HashMap<>();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(SoupPvP.getInstance(), this::clearTimersCache, 2L, 2L);
    }

    public void clearTimersCache(){
        Iterator<Map.Entry<UUID, Timer>> primaryIt = this.getPrimaryAbilitiesHashMap().entrySet().iterator();
        while (primaryIt.hasNext()) {
            Map.Entry<UUID, Timer> entry = primaryIt.next();
            UUID uuid = entry.getKey();

            if (Bukkit.getPlayer(uuid) == null){
                primaryIt.remove();
                continue;
            }
            if (entry.getValue().getCooldown() <= System.currentTimeMillis()){
                Bukkit.getPlayer(uuid).sendMessage(CC.translate("&eYou may now use &d" + entry.getValue().getAbilityName() + "&e!"));
                PlayerUtil.playSound(Bukkit.getPlayer(uuid), Sound.ENTITY_CHICKEN_EGG);
                primaryIt.remove();
            }
        }

        Iterator<Map.Entry<UUID, Timer>> secondaryIt = this.getSecondaryAbilitiesHashMap().entrySet().iterator();
        while (secondaryIt.hasNext()) {
            Map.Entry<UUID, Timer> entry = secondaryIt.next();
            UUID uuid = entry.getKey();

            if (Bukkit.getPlayer(uuid) == null){
                secondaryIt.remove();
                continue;
            }
            if (entry.getValue().getCooldown() <= System.currentTimeMillis()){
                Bukkit.getPlayer(uuid).sendMessage(CC.translate("&eYou may now use &d" + entry.getValue().getAbilityName() + "&e!"));
                PlayerUtil.playSound(Bukkit.getPlayer(uuid), Sound.ENTITY_CHICKEN_EGG);
                secondaryIt.remove();
            }
        }
    }

    public boolean containsInHashMapPlayer(UUID uuid){
        return this.getPrimaryAbilitiesHashMap().containsKey(uuid) || this.getSecondaryAbilitiesHashMap().containsKey(uuid);
    }

    public boolean containsPlayer(UUID uuid, boolean primaryAbility){
        if (primaryAbility){
            return this.getPrimaryAbilitiesHashMap().containsKey(uuid);
        } else {
            return this.getSecondaryAbilitiesHashMap().containsKey(uuid);
        }
    }

    public boolean hasTimer(UUID uuid, String abilityName, boolean primaryAbility){
        if (primaryAbility){
            Timer timer = this.getPrimaryAbilitiesHashMap().get(uuid);
            return timer != null && timer.getAbilityName().equals(abilityName) && timer.getCooldown() > System.currentTimeMillis();
        } else {
            Timer timer = this.getSecondaryAbilitiesHashMap().get(uuid);
            return timer != null && timer.getAbilityName().equals(abilityName) && timer.getCooldown() > System.currentTimeMillis();
        }
    }

    public long getRemaining(UUID uuid, String abilityName, boolean primaryAbility) {
        if (primaryAbility){
            Timer timer = this.getPrimaryAbilitiesHashMap().get(uuid);
            if (timer != null && timer.getAbilityName().equals(abilityName)){
                return Math.max(0L, timer.getCooldown() - System.currentTimeMillis());
            }
        } else {
            Timer timer = this.getSecondaryAbilitiesHashMap().get(uuid);
            if (timer != null && timer.getAbilityName().equals(abilityName)){
                return Math.max(0L, timer.getCooldown() - System.currentTimeMillis());
            }
        }
        return 0L;
    }

    public void addPlayerTimer(UUID uuid, Timer timer, boolean primaryAbility){
        // If cooldowns are globally disabled, don't register the timer at all.
        if (CooldownCommand.isBypassed()) return;

        if (primaryAbility) {
            this.getPrimaryAbilitiesHashMap().put(uuid, timer);
        } else {
            this.getSecondaryAbilitiesHashMap().put(uuid, timer);
        }
    }

    public void removePlayerTimer(UUID uuid, boolean primaryAbility){
        if (primaryAbility) {
            this.getPrimaryAbilitiesHashMap().remove(uuid);
        } else {
            this.getSecondaryAbilitiesHashMap().remove(uuid);
        }
    }

    public void removeAllPlayerTimers(UUID uuid){
        this.getPrimaryAbilitiesHashMap().remove(uuid);
        this.getSecondaryAbilitiesHashMap().remove(uuid);
    }

}