package kami.gg.souppvp.guild.listener;

import kami.gg.souppvp.guild.Guild;
import kami.gg.souppvp.guild.GuildText;
import kami.gg.souppvp.guild.gui.GuildFightGUI;
import kami.gg.souppvp.handlers.GuildsHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Standalone listener for Guild Fight mechanics.
 * - Handles GuildFightGUI clicks
 * - Combat-tags players inside the "guilds" Multiverse world only
 * - Zero scoreboard involvement
 */
public class GuildFightListener implements Listener {

    private static final String GUILD_WORLD = "guilds";
    private static final int COMBAT_TAG_SECONDS = 15;

    private final JavaPlugin plugin;
    private final GuildsHandler guildsHandler;

    /** UUID -> expiry task ID. Present = player is combat tagged. */
    private final Map<UUID, Integer> combatTagged = new HashMap<>();

    public GuildFightListener(JavaPlugin plugin, GuildsHandler guildsHandler) {
        this.plugin = plugin;
        this.guildsHandler = guildsHandler;
    }

    // -------------------------------------------------------------------------
    // GUI click handling
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof GuildFightGUI)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = meta.getDisplayName();

        // Red = offline, don't allow challenge
        if (displayName.startsWith(ChatColor.RED.toString())) {
            player.sendMessage(GuildText.translate("&cThat guild is offline and cannot be challenged."));
            return;
        }

        // Strip colour codes to get the raw guild name from "[GuildName]"
        String rawName = ChatColor.stripColor(displayName).replace("[", "").replace("]", "").trim();
        Guild targetGuild = guildsHandler.getByName(rawName);

        if (targetGuild == null) {
            player.sendMessage(GuildText.translate("&cThat guild no longer exists."));
            player.closeInventory();
            return;
        }

        Guild challengerGuild = guildsHandler.getByPlayer(player.getUniqueId());
        if (challengerGuild == null) {
            player.sendMessage(GuildText.translate("&cYou are not in a guild!"));
            player.closeInventory();
            return;
        }

        player.closeInventory();
        sendChallenge(challengerGuild, targetGuild);
    }

    private void sendChallenge(Guild challenger, Guild target) {
        broadcastToGuild(challenger, GuildText.translate(
                "&e[Guild] &fYou have challenged &a[" + target.getName() + "] &fto a fight!"));
        broadcastToGuild(target, GuildText.translate(
                "&e[Guild] &a[" + challenger.getName() + "] &fhas challenged your guild to a fight!"));
    }

    private void broadcastToGuild(Guild guild, String message) {
        for (UUID uuid : guild.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(message);
        }
    }

    // -------------------------------------------------------------------------
    // Combat tagging — guilds world only
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!isGuildWorld(victim) || !isGuildWorld(attacker)) return;

        applyTag(attacker);
        applyTag(victim);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!isGuildWorld(player)) return;
        if (!isCombatTagged(player.getUniqueId())) return;

        int taskId = combatTagged.remove(player.getUniqueId());
        Bukkit.getScheduler().cancelTask(taskId);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isGuildWorld(p)) {
                p.sendMessage(GuildText.translate("&c" + player.getName() + " &7logged out while in combat!"));
            }
        }
    }

    public void applyTag(Player player) {
        UUID uuid = player.getUniqueId();

        if (combatTagged.containsKey(uuid)) {
            Bukkit.getScheduler().cancelTask(combatTagged.get(uuid));
        } else {
            player.sendMessage(GuildText.translate(
                    "&c\u2694 You are now combat tagged for &e" + COMBAT_TAG_SECONDS + " &cseconds!"));
        }

        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                combatTagged.remove(uuid);
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    p.sendMessage(GuildText.translate("&a\u2714 You are no longer combat tagged."));
                }
            }
        }.runTaskLater(plugin, COMBAT_TAG_SECONDS * 20L).getTaskId();

        combatTagged.put(uuid, taskId);
    }

    public void removeTag(UUID uuid) {
        Integer taskId = combatTagged.remove(uuid);
        if (taskId != null) Bukkit.getScheduler().cancelTask(taskId);
    }

    public boolean isCombatTagged(UUID uuid) {
        return combatTagged.containsKey(uuid);
    }

    private boolean isGuildWorld(Player player) {
        return player.getWorld().getName().equalsIgnoreCase(GUILD_WORLD);
    }
}