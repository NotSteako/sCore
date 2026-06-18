package kami.gg.souppvp.kit.editor;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitEditorListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof KitEditorHolder)) return;

        KitEditorHolder holder = (KitEditorHolder) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();
        ClickType click = event.getClick();
        int raw = event.getRawSlot();
        int topSize = event.getView().getTopInventory().getSize();

        // Block any action that could pull items out of the GUI
        if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT
                || click == ClickType.NUMBER_KEY || click == ClickType.DOUBLE_CLICK
                || click == ClickType.DROP || click == ClickType.CONTROL_DROP
                || click == ClickType.SWAP_OFFHAND
                || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
            return;
        }

        // Anything in the player's own inventory -> blocked
        if (raw >= topSize) {
            event.setCancelled(true);
            return;
        }

        boolean holdingEditorItem = KitEditorMenu.getIndex(event.getCursor()) != null;

        // Control buttons
        if (raw == KitEditorMenu.SAVE_SLOT || raw == KitEditorMenu.RESET_SLOT || raw == KitEditorMenu.CANCEL_SLOT) {
            event.setCancelled(true);
            if (holdingEditorItem) { // must place the held item back first
                player.playSound(player.getLocation(), Sound.BLOCK_GRASS_BREAK, 1F, 0.5F);
                return;
            }
            if (raw == KitEditorMenu.SAVE_SLOT)   save(player, holder);
            if (raw == KitEditorMenu.RESET_SLOT)  reset(player, holder);
            if (raw == KitEditorMenu.CANCEL_SLOT) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
                player.closeInventory();
            }
            return;
        }

        // Editable hotbar region (slots 0..8): allow plain pickup/place only
        if (raw >= KitEditorMenu.EDIT_START && raw <= KitEditorMenu.EDIT_END) {
            if (click == ClickType.LEFT || click == ClickType.RIGHT) {
                ItemStack cursor = event.getCursor();
                // Never allow inserting a non-editor item into the layout
                if (cursor != null && !cursor.getType().isAir() && KitEditorMenu.getIndex(cursor) == null) {
                    event.setCancelled(true);
                }
                return; // allowed swap/pickup/place
            }
            event.setCancelled(true);
            return;
        }

        // Filler panes etc.
        event.setCancelled(true);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof KitEditorHolder)) return;
        // Block drags to avoid stack-splitting; reordering is click-based
        event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof KitEditorHolder)) return;
        Player player = (Player) event.getPlayer();

        // Make sure no decorative editor item leaks into the world / inventory
        if (KitEditorMenu.getIndex(player.getItemOnCursor()) != null) {
            player.setItemOnCursor(null);
        }
        for (ItemStack item : player.getInventory().getContents()) {
            if (KitEditorMenu.getIndex(item) != null) {
                player.getInventory().remove(item);
            }
        }
        player.updateInventory();
    }

    private void reset(Player player, KitEditorHolder holder) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (profile.getKitLayouts() != null) {
            profile.getKitLayouts().remove(holder.getKit().getName());
        }
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
        player.sendMessage(CC.translate("&aReset the &f" + holder.getKit().getName() + " &alayout to default."));
        new BukkitRunnable() {
            @Override public void run() { KitEditorMenu.open(player, holder.getKit()); }
        }.runTaskLater(SoupPvP.getInstance(), 1L);
    }

    private void save(Player player, KitEditorHolder holder) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        Inventory inv = holder.getInventory();
        Kit kit = holder.getKit();
        int equipmentCount = kit.getCombatEquipments().size();

        // Start with identity layout, then apply current positions
        List<Integer> layout = new ArrayList<>();
        for (int i = 0; i < equipmentCount; i++) layout.add(i);

        Map<Integer, Integer> indexToSlot = new HashMap<>();
        for (int slot = KitEditorMenu.EDIT_START; slot <= KitEditorMenu.EDIT_END; slot++) {
            Integer index = KitEditorMenu.getIndex(inv.getItem(slot));
            if (index != null) indexToSlot.put(index, slot);
        }
        for (Map.Entry<Integer, Integer> e : indexToSlot.entrySet()) {
            if (e.getKey() >= 0 && e.getKey() < layout.size()) layout.set(e.getKey(), e.getValue());
        }

        if (profile.getKitLayouts() == null) profile.setKitLayouts(new HashMap<>());
        profile.getKitLayouts().put(kit.getName(), layout);

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1.5F);
        player.sendMessage(CC.translate("&aSaved your hotbar layout for the &f" + kit.getName() + " &akit!"));
        player.closeInventory();
    }
}