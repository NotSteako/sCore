package kami.gg.souppvp.scoreboard;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.module.richpresence.RichPresenceModule;
import com.lunarclient.apollo.module.richpresence.ServerRichPresence;
import com.lunarclient.apollo.player.ApolloPlayer;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.sumo.SumoState;
import kami.gg.souppvp.kit.valorant.JettKit;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.timer.Timer;
import kami.gg.souppvp.timer.TimersHandler;
import kami.gg.souppvp.util.TimeUtil;
import kami.gg.souppvp.util.MM;
import kami.gg.souppvp.util.assemble.AssembleAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static me.clip.placeholderapi.util.Msg.color;

public class ScoreboardAdapter implements AssembleAdapter {

    private final RichPresenceModule richPresenceModule =
            Apollo.getModuleManager().getModule(RichPresenceModule.class);

    // ----------------------------
    // TITLE
    // ----------------------------
    @Override
    public Component getTitle(Player player) {
        Profile profile = getProfile(player);

        return MM.parse("<gradient:#0d98ba:#1fced1><bold>SoupPvP</bold></gradient>");
    }

    // ----------------------------
    // LINES
    // ----------------------------
    @Override
    public List<Component> getLines(Player player) {
        List<Component> lines = new ArrayList<>();
        Profile profile = getProfile(player);

        if (profile == null || !profile.getEnableScoreboard()) {
            return lines;
        }

        if (!profile.getLoaded()) {
            return loadingBoard();
        }

        // DEFAULT BOARD
        lines.add(MM.parse("<gray><strikethrough>                                </strikethrough></gray>"));
        lines.add(MM.parse("<white>Kills: <aqua>" + profile.getKills() + "</aqua></white>"));
        lines.add(MM.parse("<white>Killstreak: <aqua>" + profile.getCurrentKillstreak() + "</aqua></white>"));
        lines.add(MM.parse("<white>Deaths: <aqua>" + profile.getDeaths() + "</aqua></white>"));
        lines.add(MM.parse("<white>Credits: <aqua>" + profile.getCredits() + "</aqua></white>"));

        if (SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player)
                && profile.getCurrentKit() != null) {
            lines.add(MM.parse("<white>Current Kit: <aqua>" + profile.getCurrentKit().getName() + "</aqua></white>"));
        }

        long combatExpiry = SoupPvP.getInstance()
                .getCombatTagsHandler()
                .getCombatTags()
                .getOrDefault(player.getUniqueId(), 0L);

        if (combatExpiry - System.currentTimeMillis() > 0) {
            long seconds = (combatExpiry - System.currentTimeMillis()) / 1000;
            lines.add(MM.parse("<white>Combat Tag: <aqua>" + TimeUtil.convertToHhMmSs(seconds)+ "</aqua></white>"));
        }

        if (profile.getBounty() > 0) {
            lines.add(MM.parse("<white>Bounty: <aqua>" + profile.getBounty() + "</aqua></white>"));
        }

        TimersHandler timers = SoupPvP.getInstance().getTimersHandler();
        UUID uuid = player.getUniqueId();

        Timer primary = timers.getPrimaryAbilitiesHashMap().get(uuid);
        if (primary != null && primary.getCooldown() > System.currentTimeMillis()) {
            long remaining = primary.getCooldown() - System.currentTimeMillis();
            lines.add(MM.parse("<white>" + primary.getAbilityName() + ": <red>"
                    + TimeUtil.convertToHhMmSs(remaining / 1000) + "</red></white>"));
        }

        Timer secondary = timers.getSecondaryAbilitiesHashMap().get(uuid);
        if (secondary != null && secondary.getCooldown() > System.currentTimeMillis()) {
            long remaining = secondary.getCooldown() - System.currentTimeMillis();
            lines.add(MM.parse("<white>" + secondary.getAbilityName() + ": <red>"
                    + TimeUtil.convertToHhMmSs(remaining / 1000) + "</red></white>"));
        }

        lines.add(Component.empty());
        lines.add(MM.parse("<aqua>SoupPvP.org</aqua>"));
        lines.add(MM.parse("<gray><strikethrough>                                </strikethrough></gray>"));

        return lines;
    }

    // ----------------------------
    // LOADING BOARD
    // ----------------------------
    private List<Component> loadingBoard() {
        List<Component> lines = new ArrayList<>();

        lines.add(MM.parse("<gray><strikethrough>--------------------</strikethrough></gray>"));
        lines.add(MM.parse("<red><bold>Loading Your Profile:</bold></red>"));
        lines.add(MM.parse("<red>We are currently loading</red>"));
        lines.add(MM.parse("<red>your profile. Please wait.</red>"));
        lines.add(Component.empty());
        lines.add(MM.parse("<gold>stew.rip</gold>"));
        lines.add(MM.parse("<gray><strikethrough>--------------------</strikethrough></gray>"));

        return lines;
    }

    // ----------------------------
    // UTIL
    // ----------------------------
    private Profile getProfile(Player player) {
        return SoupPvP.getInstance()
                .getProfilesHandler()
                .getProfileByUUID(player.getUniqueId());
    }
}