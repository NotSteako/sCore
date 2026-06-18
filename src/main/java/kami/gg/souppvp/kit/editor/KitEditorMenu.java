package kami.gg.souppvp.kit.editor;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class KitEditorMenu {

    public static final int EDIT_START = 0;   // editable hotbar preview = slots 0..8
    public static final int EDIT_END   = 8;
    public static final int RESET_SLOT = 18;
    public static final int SAVE_SLOT  = 22;
    public static final int CANCEL_SLOT = 26;

    public static NamespacedKey indexKey() {
        return new NamespacedKey(SoupPvP.getInstance(), "kiteditor_index");
    }

    public static NamespacedKey controlKey() {
        return new NamespacedKey(SoupPvP.getInstance(), "kiteditor_control");
    }

    public static void open(Player player, Kit kit) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());

        KitEditorHolder holder = new KitEditorHolder(player, kit);
        String title = CC.translate("&8Kit Editor: " + kit.getRarityType().getColor() + kit.getName());
        if (title.length() > 32) title = title.substring(0, 32);

        Inventory inventory = Bukkit.createInventory(holder, 27, title);
        holder.setInventory(inventory);

        // Filler panes (everything that isn't editable or a control button)
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 9; i <= 26; i++) {
            if (i == RESET_SLOT || i == SAVE_SLOT || i == CANCEL_SLOT) continue;
            inventory.setItem(i, filler);
        }

        // Place the kit's hotbar items according to the saved layout
        List<ItemStack> equipments = kit.getCombatEquipments();
        List<Integer> layout = profile.getKitLayouts() == null ? null : profile.getKitLayouts().get(kit.getName());
        boolean[] used = new boolean[9];

        for (int i = 0; i < equipments.size() && i < 9; i++) {
            int slot = -1;
            if (layout != null && i < layout.size() && layout.get(i) != null) {
                int desired = layout.get(i);
                if (desired >= 0 && desired <= 8 && !used[desired]) slot = desired;
            }
            if (slot == -1) {
                if (i <= 8 && !used[i]) slot = i;
                else for (int s = 0; s < 9; s++) if (!used[s]) { slot = s; break; }
            }
            if (slot == -1) slot = i;
            used[slot] = true;
            inventory.setItem(slot, tag(equipments.get(i).clone(), i));
        }

        // Control buttons
        inventory.setItem(SAVE_SLOT, control(new ItemBuilder(Material.LIME_DYE)
                .name("&a&lSave Layout")
                .lore(Arrays.asList("&7Save this hotbar arrangement",
                        "&7for the " + kit.getRarityType().getColor() + kit.getName() + " &7kit.")).build()));

        inventory.setItem(RESET_SLOT, control(new ItemBuilder(Material.BARRIER)
                .name("&c&lReset to Default")
                .lore(Arrays.asList("&7Restore the original item order.")).build()));

        inventory.setItem(CANCEL_SLOT, control(new ItemBuilder(Material.RED_DYE)
                .name("&c&lCancel")
                .lore(Arrays.asList("&7Close without saving.")).build()));

        player.openInventory(inventory);
    }

    private static ItemStack tag(ItemStack item, int index) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(indexKey(), PersistentDataType.INTEGER, index);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack control(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(controlKey(), PersistentDataType.INTEGER, 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isControl(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(controlKey(), PersistentDataType.INTEGER);
    }

    public static Integer getIndex(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(indexKey(), PersistentDataType.INTEGER);
    }
}