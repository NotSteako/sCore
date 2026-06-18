package kami.gg.souppvp.gui;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

/**
 * /warp GUI — 9-slot menu with 5 warp locations.
 * North/East/South/West use MHF arrow player-heads; Central uses a Diamond Block.
 *
 * Layout (slots 0-8):
 *   [1] [_] [2] [_] [3] [_] [4] [_] [5]
 *    N       E       C       W       S
 *
 * On click: teleport the player AND equip their current kit
 * (kit equip avoids items jumping out of the inventory on teleport).
 *
 * ─────────────────────────────────────────────────────────────────
 *  HARDCODED LOCATIONS — edit the static fields below to match your
 *  world and arena coordinates before compiling.
 * ─────────────────────────────────────────────────────────────────
 */
public class WarpGUI implements Listener {

    // ── CONFIGURE THESE ──────────────────────────────────────────
    private static final String WORLD_NAME = "world"; // world name

    // ─────────────────────────────────────────────────────────────

    private static final String GUI_TITLE = ChatColor.GREEN + "" + ChatColor.BOLD + "Warps";

    // Slot indices for each warp button (spread evenly across 9 slots)
    private static final int SLOT_NORTH   = 1;
    private static final int SLOT_EAST    = 3;
    private static final int SLOT_SOUTH   = 5;
    private static final int SLOT_WEST    = 7;


    // ── Static helper: build and open the inventory ───────────────

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, GUI_TITLE);

        inv.setItem(SLOT_NORTH, grassBlock(ChatColor.AQUA + "North"));
        inv.setItem(SLOT_EAST, grassBlock(ChatColor.YELLOW + "East"));
     //   inv.setItem(SLOT_CENTRAL, diamondBlock(ChatColor.GREEN + "Central"));
        inv.setItem(SLOT_WEST, grassBlock(ChatColor.LIGHT_PURPLE + "West"));
        inv.setItem(SLOT_SOUTH, grassBlock(ChatColor.RED + "South"));

        player.openInventory(inv);
    }

    private static ItemStack grassBlock(String displayName) {
        ItemStack item = new ItemStack(Material.GRASS_BLOCK);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Click to teleport!"));
            item.setItemMeta(meta);
        }

        return item;
    }
    /** Creates a Diamond Block item. */
    private static ItemStack diamondBlock(String displayName) {
        ItemStack item = new ItemStack(Material.DIAMOND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Click to teleport!"));
            item.setItemMeta(meta);
        }
        return item;
    }

    // ── Listener: handle clicks ───────────────────────────────────

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Only intercept our GUI (title check is the standard guard)
        if (!ChatColor.stripColor(event.getView().getTitle())
                .equals(ChatColor.stripColor(GUI_TITLE))) return;

        event.setCancelled(true); // block any item movement

        if (event.getCurrentItem() == null
                || event.getCurrentItem().getType() == Material.AIR) return;

        int slot = event.getSlot();
        Location dest = resolveDestination(slot);
        if (dest == null) return;

        player.closeInventory();
        player.teleport(dest);

        player.sendMessage(ChatColor.GREEN + "Teleported to " + destinationName(slot) + ChatColor.GREEN + "!");
    }

    /** Returns the Location for a given slot, or null if not a warp slot. */
    private Location resolveDestination(int slot) {
        World world = Bukkit.getWorld(WORLD_NAME);
        if (world == null) return null;

        return switch (slot) {
            case SLOT_NORTH   -> new Location(world, 0, 92, -95, 179, 3 );
            case SLOT_EAST    -> new Location(world, 95, 92, 0, -90, -1 );
            case SLOT_WEST    -> new Location(world, -95, 92, 0, 91, -5);
            case SLOT_SOUTH   -> new Location(world, 0, 92, 95, 0, -1);
            default -> null;
        };
    }

    private String destinationName(int slot) {
        return switch (slot) {
            case SLOT_NORTH   -> ChatColor.AQUA    + "North";
            case SLOT_EAST    -> ChatColor.YELLOW  + "East";
            case SLOT_WEST    -> ChatColor.LIGHT_PURPLE + "West";
            case SLOT_SOUTH   -> ChatColor.RED     + "South";
            default -> "Unknown";
        };
    }
}