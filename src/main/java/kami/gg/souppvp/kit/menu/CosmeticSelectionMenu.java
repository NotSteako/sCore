package kami.gg.souppvp.kit.menu;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.KitCategory;
import kami.gg.souppvp.kit.button.CosmeticKitButton;
import kami.gg.souppvp.kit.button.CosmeticSelectButton;
import kami.gg.souppvp.kit.button.category.CategoryNextButton;
import kami.gg.souppvp.kit.button.category.CategoryPreviousButton;
import kami.gg.souppvp.kit.button.category.CategoryRandomButton;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class CosmeticSelectionMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return CC.translate("Kit Skins");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {

        Profile profile = SoupPvP.getInstance()
                .getProfilesHandler()
                .getProfileByUUID(player.getUniqueId());

        Map<Integer, Button> buttonMap = new HashMap<>();

        // Top two rows — red glass border (same as KitsSelectMenu)
        for (int i = 0; i < 18; i++) {
            buttonMap.put(i, Button.placeholder(
                    Material.RED_STAINED_GLASS_PANE,
                    (byte) 15,
                    " "
            ));
        }

        // Optional: reuse your existing top-row nav buttons if desired
        // buttonMap.put(0, new CategoryButton(profile));
        // buttonMap.put(8, new BackToKitsButton());

        // Build map of category → kits that have cosmetics
        Map<KitCategory, List<Kit>> kitsByCategory = new LinkedHashMap<>();

        for (Kit kit : SoupPvP.getInstance()
                .getKitsHandler()
                .getKits()) {

            // Only include kits that actually have cosmetics
            if (kit.getAvailableCosmetics() == null || kit.getAvailableCosmetics().isEmpty()) {
                continue;
            }

            kitsByCategory
                    .computeIfAbsent(kit.getCategory(), k -> new ArrayList<>())
                    .add(kit);
        }

        kitsByCategory.values().forEach(list ->
                list.sort(Comparator.comparing(Kit::getRarityType))
        );

        int row = 2;

        for (KitCategory category : KitCategory.values()) {

            if (category == KitCategory.ALL) continue;
            if (row > 5) break;

            List<Kit> kits = kitsByCategory.getOrDefault(category, Collections.emptyList());

            // Skip categories with no cosmetic kits entirely
            if (kits.isEmpty()) continue;

            int baseSlot = row * 9;

            int page = profile.getCategoryPage(category);
            int startIndex = page * 6;

            // Col 0 — category icon (same CategoryRandomButton as main menu)
            buttonMap.put(baseSlot, new CategoryRandomButton(category));

            // Cols 1-6 — kits that have cosmetics, opening CosmeticSkinMenu on click
            for (int i = 0; i < 6; i++) {
                int kitIndex = startIndex + i;
                if (kitIndex >= kits.size()) break;

                Kit kit = kits.get(kitIndex);
                buttonMap.put(
                        baseSlot + 1 + i,
                        new CosmeticKitButton(kit) // see note below
                );
            }

            // Cols 7-8 — pagination arrows (same as main menu)
            buttonMap.put(baseSlot + 7, new CategoryPreviousButton(category));
            buttonMap.put(baseSlot + 8, new CategoryNextButton(category));

            row++;
        }

        return buttonMap;
    }

    @Override
    public int size(Map<Integer, Button> buttons) {
        return 54;
    }
}