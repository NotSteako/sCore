package kami.gg.souppvp.listener;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.perk.Perk;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import xyz.refinedev.phoenix.Phoenix;

public class KillStreakAnnouncerListener implements Listener {

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event){

        if (event.getEntity().getKiller() != null){
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getEntity().getKiller().getUniqueId());
            Perk currentTier3Perk = SoupPvP.getInstance().getPerksHandler().getPerkByName(profile.getActivePerks().get(2));
            Perk incognitoPerk = SoupPvP.getInstance().getPerksHandler().getPerkByName("Incognito");

            Phoenix api = Phoenix.getInstance();
            xyz.refinedev.phoenix.profile.IProfile profile2 = api.getProfileHandler().getProfile(profile.getUuid());

            if (currentTier3Perk == incognitoPerk) return;
            if (profile.getCurrentKillstreak() % 5 == 0 && profile.getCurrentKillstreak() > 0){
                for (Player player : Bukkit.getOnlinePlayers()){
                    Profile playerProfile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
                    if (playerProfile.getEnableKillstreakMessages()){
                        player.sendMessage(CC.translate(profile2.getHighestRank().getColor() + profile.getUsername() + "&e is on a &a" + profile.getCurrentKillstreak() + "&e Killstreak!"));
                    }
                }
            }
        }
    }

}
