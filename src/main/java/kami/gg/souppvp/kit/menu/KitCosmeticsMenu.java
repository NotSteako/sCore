package kami.gg.souppvp.kit.menu;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.KitCategory;
import kami.gg.souppvp.kit.button.*;
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

public class KitCosmeticsMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return CC.translate("&dKit Skins");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {

        Profile profile = SoupPvP.getInstance()
                .getProfilesHandler()
                .getProfileByUUID(player.getUniqueId());

        Map<Integer, Button> buttonMap = new HashMap<>();

        // Top row nav buttons
//        buttonMap.put(0, new CategoryButton(profile));
//        buttonMap.put(2, new RandomKitButton());
//        buttonMap.put(4, new BackToKitsButton());
//        buttonMap.put(6, new SelectPreviousKitButton(profile));
        buttonMap.put(3, new BackToKitsButton());
        buttonMap.put(5, new CosmeticsMenuButton());

        // Fill first 2 rows with red glass
        for (int i = 0; i < 18; i++) {
            buttonMap.putIfAbsent(i, Button.placeholder(
                    Material.RED_STAINED_GLASS_PANE,
                    (byte) 15,
                    " "
            ));
        }

        // Build category → kits map, filtering to only kits with cosmetics
        Map<KitCategory, List<Kit>> kitsByCategory = new LinkedHashMap<>();

        for (Kit kit : SoupPvP.getInstance()
                .getKitsHandler()
                .getKits()) {

            if (kit.getAvailableCosmetics().isEmpty()) {
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

            List<Kit> kits = kitsByCategory.getOrDefault(
                    category,
                    Collections.emptyList()
            );

            // Skip categories that have no cosmetic kits
            if (kits.isEmpty()) continue;

            int baseSlot = row * 9;
            int page = profile.getCategoryPage(category);
            int startIndex = page * 6;

            // Col 0 — category icon
            buttonMap.put(baseSlot, new CategoryRandomButton(category));

            // Cols 1-6 — kits with cosmetics, clicking opens CosmeticSkinMenu
            for (int i = 0; i < 6; i++) {
                int kitIndex = startIndex + i;
                if (kitIndex >= kits.size()) break;

                buttonMap.put(
                        baseSlot + 1 + i,
                        new CosmeticKitButton(kits.get(kitIndex))
                );
            }

//            // Col 7 — previous page
//            buttonMap.put(baseSlot + 7, new CategoryPreviousButton(category));
//
//            // Col 8 — next page
//            buttonMap.put(baseSlot + 8, new CategoryNextButton(category));

            row++;
        }

        // Fill remaining slots with gray glass
        for (int i = 18; i < 54; i++) {
            buttonMap.putIfAbsent(i, Button.placeholder(
                    Material.GRAY_STAINED_GLASS_PANE,
                    (byte) 7,
                    " "
            ));
        }

        return buttonMap;
    }

    @Override
    public int size(Map<Integer, Button> buttons) {
        return 54;
    }
}