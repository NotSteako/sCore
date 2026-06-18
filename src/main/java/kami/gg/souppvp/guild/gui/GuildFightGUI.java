package kami.gg.souppvp.guild.gui;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.Guild;
import kami.gg.souppvp.guild.GuildStats;
import kami.gg.souppvp.guild.GuildText;
import kami.gg.souppvp.handlers.GuildsHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuildFightGUI implements InventoryHolder {

    private static final String TITLE = ChatColor.DARK_RED + "" + ChatColor.BOLD + "Guild Fight";
    private static final int SIZE = 54;

    private final Inventory inventory;

    public GuildFightGUI(GuildsHandler guildsHandler, Player viewer) {
        this.inventory = Bukkit.createInventory(this, SIZE, TITLE);
        populate(guildsHandler, viewer);
    }

    private void populate(GuildsHandler guildsHandler, Player viewer) {
        Guild viewerGuild = guildsHandler.getByPlayer(viewer.getUniqueId());

        int slot = 0;
        for (Guild guild : guildsHandler.getGuildsByLowerName().values()) {
            if (slot >= SIZE) break;

            // Don't show the viewer's own guild
            if (viewerGuild != null && guild.getName().equalsIgnoreCase(viewerGuild.getName())) {
                continue;
            }

            boolean online = isOnline(guild);

            // &a[NAME] for online, &c[NAME] for offline — using the guild's coloured tag
            String displayName = online
                    ? GuildText.translate("&a[" + guild.getName() + "]")
                    : GuildText.translate("&c[" + guild.getName() + "]");

            ItemStack item = new ItemStack(online ? Material.LIME_WOOL : Material.RED_WOOL);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(displayName);

                // Pull aggregated stats from Mongo (same as /guild stats)
                GuildStats stats = SoupPvP.getInstance().getGuildsHandler().aggregateStats(guild);

                List<String> lore = new ArrayList<>();
                lore.add(GuildText.translate("&7Members: &f" + getOnlineCount(guild) + "&7/&f" + guild.getMembers().size()));
                lore.add(GuildText.translate("&7Kills: &a" + stats.totalKills));
                lore.add(GuildText.translate("&7Deaths: &c" + stats.totalDeaths));
                lore.add(GuildText.translate("&7K/D: &e" + String.format("%.2f", stats.averageKDR())));
                lore.add("");
                lore.add(online
                        ? GuildText.translate("&aClick to challenge this guild!")
                        : GuildText.translate("&cThis guild is offline."));

                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            inventory.setItem(slot, item);
            slot++;
        }

        // Fill remaining slots with a glass pane
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        for (int i = slot; i < SIZE; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    /** A guild is online if at least one member is currently on the server. */
    private boolean isOnline(Guild guild) {
        for (UUID uuid : guild.getMembers()) {
            if (Bukkit.getPlayer(uuid) != null) return true;
        }
        return false;
    }

    private int getOnlineCount(Guild guild) {
        int count = 0;
        for (UUID uuid : guild.getMembers()) {
            if (Bukkit.getPlayer(uuid) != null) count++;
        }
        return count;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static String getTitle() {
        return TITLE;
    }
}