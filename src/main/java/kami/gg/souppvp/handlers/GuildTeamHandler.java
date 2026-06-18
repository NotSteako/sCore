package kami.gg.souppvp.handlers;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.module.team.TeamMember;
import com.lunarclient.apollo.module.team.TeamModule;
import com.lunarclient.apollo.player.ApolloPlayer;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GuildTeamHandler {

    private static final Color COLOR_LEADER = new Color(255, 170, 0);  // Gold  (&6)
    private static final Color COLOR_MEMBER  = new Color(85, 255, 85); // Green (&a)

    private final TeamModule teamModule;

    public GuildTeamHandler() {
        teamModule = Apollo.getModuleManager().getModule(TeamModule.class);

        // Refresh every tick async
        Bukkit.getScheduler().runTaskTimerAsynchronously(
                SoupPvP.getInstance(),
                this::refreshAll,
                20L, 1L
        );
    }

    private void refreshAll() {
        GuildsHandler guildsHandler = SoupPvP.getInstance().getGuildsHandler();
        if (guildsHandler == null) return;

        for (Guild guild : guildsHandler.getGuildsByLowerName().values()) {
            refreshGuild(guild);
        }
    }

    private void refreshGuild(Guild guild) {
        List<Player> onlineMembers = new ArrayList<>();
        for (UUID uuid : guild.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) onlineMembers.add(p);
        }

        if (onlineMembers.size() < 2) {
            // Nothing to show if fewer than 2 online — clear for the one online member if any
            for (Player viewer : onlineMembers) {
                Optional<ApolloPlayer> ap = Apollo.getPlayerManager().getPlayer(viewer.getUniqueId());
                ap.ifPresent(teamModule::resetTeamMembers);
            }
            return;
        }

        for (Player viewer : onlineMembers) {
            Optional<ApolloPlayer> apolloPlayerOpt = Apollo.getPlayerManager().getPlayer(viewer.getUniqueId());
            if (!apolloPlayerOpt.isPresent()) continue;

            List<TeamMember> teammates = new ArrayList<>();

            for (Player member : onlineMembers) {
                if (member == viewer) continue;
                if (!viewer.canSee(member)) continue;
                if (!viewer.getWorld().getName().equals(member.getWorld().getName())) continue;

                Color markerColor = guild.isLeader(member.getUniqueId()) ? COLOR_LEADER : COLOR_MEMBER;
                boolean nearbyTracking = isWithinTrackingRange(viewer, member);

                TeamMember.TeamMemberBuilder builder = TeamMember.builder()
                        .playerUuid(member.getUniqueId())
                        .markerColor(markerColor);

                if (!nearbyTracking) {
                    Location loc = member.getLocation();
                    builder.location(com.lunarclient.apollo.common.location.ApolloLocation.builder()
                            .world(loc.getWorld().getName())
                            .x(loc.getX())
                            .y(loc.getY())
                            .z(loc.getZ())
                            .build());
                    builder.displayName(net.kyori.adventure.text.Component.text()
                            .content(member.getName())
                            .color(guild.isLeader(member.getUniqueId())
                                    ? net.kyori.adventure.text.format.NamedTextColor.GOLD
                                    : net.kyori.adventure.text.format.NamedTextColor.GREEN)
                            .build());
                }

                teammates.add(builder.build());
            }

            teamModule.updateTeamMembers(apolloPlayerOpt.get(), teammates);
        }
    }

    /**
     * Within 48 blocks = server is already tracking the entity client-side,
     * so no need to send a location override.
     */
    private boolean isWithinTrackingRange(Player viewer, Player member) {
        double dx = viewer.getLocation().getX() - member.getLocation().getX();
        double dz = viewer.getLocation().getZ() - member.getLocation().getZ();
        return (dx * dx + dz * dz) <= (48.0 * 48.0);
    }
}