package kami.gg.souppvp.profile;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class ProfileListeners implements Listener {

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        Player player = Bukkit.getPlayer(event.getUniqueId());
        String name = event.getName(); // or event.getPlayerProfile().getName()
        UUID uuid = event.getUniqueId();

        if (name == null || name.isEmpty() || name.equals("null")) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    CC.RED + "Authentication failed. Please reconnect.");
            return;
        }

        if (player != null && player.isOnline()) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(CC.RED + "You tried to login too quickly after disconnecting.\nTry again in a few seconds.");
            SoupPvP.getInstance().getServer().getScheduler().runTask(SoupPvP.getInstance(), () -> player.kickPlayer(CC.RED + "You tried to login too quickly after disconnecting.\nTry again in a few seconds."));
            return;
        }
        Profile profile = null;
        try {
            profile = new Profile(event.getUniqueId());
            if (!profile.getLoaded()) {
                event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                event.setKickMessage(CC.translate("&cFailed to load your profile."));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (profile == null || !profile.getLoaded()) {
            event.setKickMessage(CC.translate("&cFailed to load your profile."));
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            return;
        }
        SoupPvP.getInstance().getProfilesHandler().getProfiles().add(profile);
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event){
        Player player = event.getPlayer();
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (profile.isCombatTagged()) {
            profile.setCurrentKillstreak(0);
            profile.setDeaths(profile.getDeaths() + 1);
        }
        profile.saveProfile();
        SoupPvP.getInstance().getProfilesHandler().getProfiles().remove(profile);
        SoupPvP.getInstance().getCombatTagsHandler().getCombatTags().remove(player.getUniqueId());
        SoupPvP.getInstance().getNoFallDamageHandler().getNoFallDamage().remove(player.getUniqueId());
        SoupPvP.getInstance().getSpawnTeleportationHandler().getSpawnTeleporataion().remove(player.getUniqueId());
    }

}
