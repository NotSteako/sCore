package kami.gg.souppvp.kit.menu;

import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.Menu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class KitPreviewMenu extends Menu {

    private final Kit kit;

    private static final Map<UUID, ArmorStand> PREVIEW_STANDS = new HashMap<>();

    public KitPreviewMenu(Kit kit) {
        this.kit = kit;
    }

    @Override
    public String getTitle(Player player) {
        return "§6Preview: §e" + kit.getName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        // Glass pane border across the whole GUI
        for (int i = 0; i < 54; i++) {
            buttons.put(i, Button.placeholder(
                    Material.GRAY_STAINED_GLASS_PANE,
                    (byte) 7,
                    " "
            ));
        }

        // Centre info item
        buttons.put(22, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                ItemStack info = kit.getIcon(); // use the kit's own icon
                var meta = info.getItemMeta();
                meta.setDisplayName("§e" + kit.getName());
                meta.setLore(Arrays.asList(
                        "",
                        "§7A preview armour-stand has been",
                        "§7spawned in front of you.",
                        "",
                        "§aClose this menu to despawn it."
                ));
                info.setItemMeta(meta);
                return info;
            }
        });

        // Close / back button
        buttons.put(49, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                ItemStack back = new ItemStack(Material.ARROW);
                var meta = back.getItemMeta();
                meta.setDisplayName("§cClose Preview");
                back.setItemMeta(meta);
                return back;
            }

            public void clicked(Player player, int slot, ClickType clickType) {
                player.closeInventory();
            }
        });

        return buttons;
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────

    @Override
    public void onOpen(Player player) {
        spawnPreviewStand(player);
    }


    public void onClose(Player player, InventoryCloseEvent event) {
        despawnPreviewStand(player);
    }

    // ── ArmorStand helpers ─────────────────────────────────────────────────

    private void spawnPreviewStand(Player player) {
        despawnPreviewStand(player); // remove old stand first

        // 2 blocks directly in front of the player, same Y
        Location loc = player.getLocation()
                .add(player.getLocation().getDirection().setY(0).normalize().multiply(2));

        // Face the stand back toward the player
        float yawTowardPlayer = player.getLocation().getYaw() + 180f;

        ArmorStand stand = player.getWorld().spawn(loc, ArmorStand.class, as -> {
            as.setVisible(true);
            as.setGravity(false);
            as.setCanPickupItems(false);
            as.setCustomName("§e" + kit.getName() + " §7Preview");
            as.setCustomNameVisible(true);

            Location standLoc = as.getLocation();
            standLoc.setYaw(yawTowardPlayer);
            as.teleport(standLoc);

            applyKitEquipment(as, player);
        });

        PREVIEW_STANDS.put(player.getUniqueId(), stand);

        // Safety auto-despawn after 60 s in case onClose is missed
        new BukkitRunnable() {
            @Override
            public void run() {
                ArmorStand current = PREVIEW_STANDS.get(player.getUniqueId());
                if (current != null && current.equals(stand) && !stand.isDead()) {
                    despawnPreviewStand(player);
                }
            }
        }.runTaskLater(JavaPlugin.getProvidingPlugin(getClass()), 20L * 60);
    }

    /**
     * Reads armour from Kit#getArmor(Player) — returns ItemStack[4] ordered
     * [boots, leggings, chestplate, helmet] (standard Bukkit armour contents).
     * Main-hand is taken from the first slot of getCombatEquipments().
     */
    private void applyKitEquipment(ArmorStand stand, Player player) {
        EntityEquipment eq = stand.getEquipment();

        // getArmor(Player) → ItemStack[4]: index 0 = boots … 3 = helmet
        ItemStack[] armor = kit.getArmor(player);
        if (armor != null) {
            if (armor.length > 3 && armor[3] != null) eq.setHelmet(armor[3]);
            if (armor.length > 2 && armor[2] != null) eq.setChestplate(armor[2]);
            if (armor.length > 1 && armor[1] != null) eq.setLeggings(armor[1]);
            if (armor.length > 0 && armor[0] != null) eq.setBoots(armor[0]);
        }

        // First combat equipment slot → main hand (e.g. sword)
        List<ItemStack> combatItems = kit.getCombatEquipments();
        if (combatItems != null && !combatItems.isEmpty() && combatItems.get(0) != null) {
            eq.setItemInMainHand(combatItems.get(0));
        }
    }

    private static void despawnPreviewStand(Player player) {
        ArmorStand stand = PREVIEW_STANDS.remove(player.getUniqueId());
        if (stand != null && !stand.isDead()) {
            stand.remove();
        }
    }

    /** Call from your plugin onDisable() to clean up all stands on shutdown. */
    public static void despawnAll() {
        for (ArmorStand stand : PREVIEW_STANDS.values()) {
            if (stand != null && !stand.isDead()) stand.remove();
        }
        PREVIEW_STANDS.clear();
    }

    @Override
    public int size(Map<Integer, Button> buttons) {
        return 54;
    }
}