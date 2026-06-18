package kami.gg.souppvp.tablist;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.profile.Profile;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.refinedev.api.tablist.adapter.TabAdapter;
import xyz.refinedev.api.tablist.setup.TabEntry;
import xyz.refinedev.phoenix.Phoenix;
import xyz.refinedev.phoenix.profile.IProfile;
import xyz.refinedev.phoenix.profile.tag.ITag;
import xyz.refinedev.phoenix.rank.IRank;

import java.util.*;
import java.util.stream.Collectors;

public class RankedTabAdapter implements TabAdapter {

    // Vanilla tab = 4 columns x 20 rows
    private static final int ROWS = 20;
    private static final int COLUMNS = 4;
    private static final int MAX_SLOTS = ROWS * COLUMNS;

    private final SoupPvP plugin;
    private final Phoenix phoenix;
    private final RankComparator rankComparator;

    public RankedTabAdapter(SoupPvP plugin) {
        this.plugin = plugin;
        this.phoenix = Phoenix.getInstance();
        this.rankComparator = new RankComparator(plugin);
    }

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private String mm(String text) {
        return LegacyComponentSerializer.legacySection().serialize(
                MM.deserialize(text)
        );
    }

    private static final String HEADER =
            LegacyComponentSerializer.legacySection().serialize(
                    MM.deserialize(
                            "\n<bold><gradient:#0d98ba:#1fced1>Beta Test</gradient></bold>\n" +
                                    "<white>\uD83C\uDF5C </white><gradient:#0d98ba:#1fced1>SoupPvP</gradient><white> \uD83C\uDF5C</white>\n"
                    )
            );

    @Override
    public String getHeader(Player player) {
        return HEADER;
    }

    @Override
    public String getFooter(Player player) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        Kit kit = profile.getCurrentKit();

        return mm(
                "\n" +
                        "<gray>Online:</gray> <gradient:#0d98ba:#1fced1>" + Bukkit.getOnlinePlayers().size()  +"</gradient>     " +
                        "<gray>Current Kit:</gray> <bold><gradient:#0d98ba:#1fced1>" + (kit != null ? kit.getName() : "None") +  "</gradient></bold>     " +
                        "<gray>Kill Streak:</gray> <gradient:#0d98ba:#1fced1>"+ profile.getCurrentKillstreak() + "</gradient>\n"
        );
    }
    @Override
    public List<TabEntry> getLines(Player viewer) {

        List<TabEntry> entries = new ArrayList<>();

        List<String> lines = buildTablistContent(viewer);

        int slot = 0;

        for (String line : lines) {

            if (slot >= MAX_SLOTS) {
                break;
            }

            int column = slot / ROWS;
            int row = slot % ROWS;

            if (line.startsWith("PLAYER:")) {

                String[] split = line.substring(7).split("\\|", 2);

                String playerName = split[0];
                String displayText = split.length > 1
                        ? split[1]
                        : playerName;

                Player target = Bukkit.getPlayerExact(playerName);

                IProfile profile = target != null
                        ? phoenix.getProfileHandler().getProfile(target.getUniqueId())
                        : null;

                entries.add(new TabEntry(
                        column,
                        row,
                        cc(displayText),
                        target != null ? target.getPing() : 0,
                        null
                ));

            } else {

                entries.add(new TabEntry(
                        column,
                        row,
                        cc(line),
                        0,
                        null
                ));
            }

            slot++;
        }

        while (slot < MAX_SLOTS) {

            int column = slot / ROWS;
            int row = slot % ROWS;

            entries.add(new TabEntry(
                    column,
                    row,
                    " ",
                    0,
                    null
            ));

            slot++;
        }

        return entries;
    }

    private List<String> buildTablistContent(Player viewer) {

        List<String> lines = new ArrayList<>();

        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        if (onlinePlayers.isEmpty()) {
            lines.add("&7No players online");
            return lines;
        }

        Map<IRank, List<Player>> playersByRank = new LinkedHashMap<>();
        Map<UUID, IProfile> profileCache = new HashMap<>();

        for (Player player : onlinePlayers) {

            IProfile profile = phoenix.getProfileHandler()
                    .getProfile(player.getUniqueId());

            if (profile != null) {
                profileCache.put(player.getUniqueId(), profile);
            }

            IRank rank = null;

            if (profile != null
                    && profile.getBestGrant() != null
                    && profile.getBestGrant().getRank() != null) {

                rank = profile.getBestGrant().getRank();
            }

            if (rank == null) {
                rank = phoenix.getRankHandler().getDefaultRank();
            }

            // Hide vanished players from normal players
            if (profile != null
                    && profile.isVanished()
                    && !viewer.hasPermission("core.staff")) {

                continue;
            }

            playersByRank.computeIfAbsent(rank, r -> new ArrayList<>())
                    .add(player);
        }

        List<IRank> sortedRanks = playersByRank.keySet()
                .stream()
                .sorted(rankComparator)
                .collect(Collectors.toList());

        for (int i = 0; i < sortedRanks.size(); i++) {

            IRank rank = sortedRanks.get(i);

            List<Player> players = playersByRank.get(rank);

            players.sort(Comparator.comparing(Player::getName));

            String prefix = "";

            if (rank.getDisplayName().equalsIgnoreCase("Owner")) {
                prefix = "&f★ ";
            }



            // Rank Header
            lines.add(
                    prefix +
                            rank.getColor() +
                            rank.getDisplayName()
            );

            for (Player player : players) {

                IProfile profile = profileCache.get(player.getUniqueId());

                IRank playerRank = rank;

                if (profile != null
                        && profile.getBestGrant() != null
                        && profile.getBestGrant().getRank() != null) {

                    playerRank = profile.getBestGrant().getRank();
                }

                boolean vanished = profile != null
                        && profile.isVanished();

                String color = "&7";

                if (playerRank != null
                        && playerRank.getColor() != null) {

                    color = playerRank.getColor();
                }

                String vanishPrefix = "";

                // Staff can see vanished players
                if (vanished && viewer.hasPermission("core.staff")) {
                    vanishPrefix = "&l⧓ &r";
                }

                // Tags
                String tagText = "";
                if (profile != null && profile.getTag() != null) {

                    ITag tag = profile.getTag();

                    tagText = " " + tag.getPrefix();

                }

                String playerLine =
                        "PLAYER:" + player.getName() + "|" +
                                vanishPrefix +
                                color +
                                player.getName() +
                                tagText;

                lines.add(playerLine);
            }

            if (i != sortedRanks.size() - 1) {
                lines.add(" ");
            }
        }

        return lines;
    }

    private String cc(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}