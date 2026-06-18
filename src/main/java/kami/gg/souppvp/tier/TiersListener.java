package kami.gg.souppvp.tier;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.PlayerUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class TiersListener implements Listener {

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event){
        if (event.getEntity().getKiller() != null){
            Player killer = event.getEntity().getKiller();
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(killer.getUniqueId());
            Integer experiences = profile.getExperiences();
            for (Tiers tiers : Tiers.values()){
                if (experiences > tiers.getRequiredExperiences() && (experiences - tiers.getRequiredExperiences() <= 3)){
                    profile.setTier(tiers);
                    killer.sendMessage(CC.translate("&aYou've successfully leveled up to tier &7" + tiers.getTierLevel() + "&a!"));
                    PlayerUtil.playSound(killer, Sound.ENTITY_PLAYER_LEVELUP);
                }
            }
        }
    }

}
