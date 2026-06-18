package kami.gg.souppvp.kit.menu;

import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.button.BackToCosmeticsButton;
import kami.gg.souppvp.kit.button.CosmeticSelectButton;
import kami.gg.souppvp.kit.cosmetic.CosmeticSkin;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CosmeticSkinMenu extends Menu {

    private final Kit kit;

    public CosmeticSkinMenu(Kit kit) {
        this.kit = kit;
    }

    @Override
    public String getTitle(Player player) {
        return CC.translate("&5&l" + kit.getName() + " &d&lSkins");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        List<CosmeticSkin> cosmetics = kit.getAvailableCosmetics();

        // --- Border: row 0 and row 5 (top + bottom) in purple glass ---
        for (int i = 0; i < 9; i++) {
            buttons.put(i, Button.placeholder(Material.PURPLE_STAINED_GLASS_PANE, (byte) 10, " "));
            buttons.put(45 + i, Button.placeholder(Material.PURPLE_STAINED_GLASS_PANE, (byte) 10, " "));
        }

        // --- Left and right column borders in magenta glass ---
        for (int row = 1; row <= 4; row++) {
            buttons.put(row * 9, Button.placeholder(Material.MAGENTA_STAINED_GLASS_PANE, (byte) 2, " "));
            buttons.put(row * 9 + 8, Button.placeholder(Material.MAGENTA_STAINED_GLASS_PANE, (byte) 2, " "));
        }

        // --- Back button in top-left corner ---
        buttons.put(0, new BackToCosmeticsButton());

        // --- Kit info display item centered in top row ---
//        buttons.put(4, new KitInfoButton(kit));

        // --- Center skins in the 7-wide interior (cols 1-7, rows 1-4 = 28 slots) ---
        // Use slots 10-16, 19-25, 28-34, 37-43
        int[] skinSlots = buildCenteredSlots(cosmetics.size());

        for (int i = 0; i < Math.min(cosmetics.size(), skinSlots.length); i++) {
            buttons.put(skinSlots[i], new CosmeticSelectButton(kit, cosmetics.get(i)));
        }

        // --- Fill remaining interior with black glass for a dark backing ---
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 7; col++) {
                buttons.putIfAbsent(
                        row * 9 + col,
                        Button.placeholder(Material.BLACK_STAINED_GLASS_PANE, (byte) 15, " ")
                );
            }
        }

        return buttons;
    }

    /**
     * Returns slot indices that center the skins horizontally
     * across the 7-wide interior (cols 1–7), row by row.
     */
    private int[] buildCenteredSlots(int count) {
        int cols = 7;
        // All available interior slots across rows 1-4
        List<Integer> slots = new ArrayList<>();

        int itemsPerRow = Math.min(count, cols);
        int startCol = 1 + (cols - itemsPerRow) / 2; // center in row

        // Single row centering for <=7 items, multi-row for more
        if (count <= 7) {
            for (int i = 0; i < count; i++) {
                slots.add(9 + startCol + i); // row 1 (offset by 9 for top border row)
            }
        } else {
            for (int row = 1; row <= 4 && slots.size() < count; row++) {
                int rowCount = Math.min(cols, count - slots.size());
                int rowStart = 1 + (cols - rowCount) / 2;
                for (int col = 0; col < rowCount; col++) {
                    slots.add(row * 9 + rowStart + col);
                }
            }
        }

        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public int size(Map<Integer, Button> buttons) {
        return 54;
    }
}