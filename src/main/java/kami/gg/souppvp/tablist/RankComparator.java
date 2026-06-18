package kami.gg.souppvp.tablist;

import kami.gg.souppvp.SoupPvP;
import xyz.refinedev.phoenix.rank.IRank;

import java.util.Comparator;

/**
 * Comparator for sorting ranks by weight (priority)
 * Higher weight = higher priority (displayed first)
 */
public class RankComparator implements Comparator<IRank> {

    private final SoupPvP plugin;

    public RankComparator(SoupPvP plugin) {
        this.plugin = plugin;
    }

    @Override
    public int compare(IRank rank1, IRank rank2) {
        // Compare by weight (higher weight comes first)
        // PhoenixAPI uses weight system where higher numbers = higher priority
        int weight1 = rank1.getPriority();
        int weight2 = rank2.getPriority();

        // Sort in descending order (highest weight first)
        int weightComparison = Integer.compare(weight2, weight1);

        if (weightComparison != 0) {
            return weightComparison;
        }

        // If weights are equal, sort alphabetically by display name
        return rank1.getDisplayName().compareToIgnoreCase(rank2.getDisplayName());
    }
}