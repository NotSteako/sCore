package kami.gg.souppvp.juggernaut;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.profile.Profile;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * @author hieu
 * @date 10/06/2023
 */
public class JuggernautListener implements Listener {

    public void onPlayerDeathEvent(PlayerDeathEvent event){
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getEntity().getUniqueId());
        if (profile.isJuggernaut()){
            profile.setJuggernaut(false);
        }
    }

}
