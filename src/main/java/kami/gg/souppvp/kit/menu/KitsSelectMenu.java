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

public class KitsSelectMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return CC.translate("Select a kit to equip");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {

        Profile profile = SoupPvP.getInstance()
                .getProfilesHandler()
                .getProfileByUUID(player.getUniqueId());

        Map<Integer, Button> buttonMap = new HashMap<>();

//        buttonMap.put(0, new CategoryButton(profile));
//        buttonMap.put(2, new RandomKitButton());
//        buttonMap.put(4, new BackToKitsButton());
//        buttonMap.put(6, new SelectPreviousKitButton(profile));
        buttonMap.put(4, new CosmeticsMenuButton());

        for (int i = 0; i < 18; i++) {
            buttonMap.putIfAbsent(
                    i,
                    Button.placeholder(
                            Material.RED_STAINED_GLASS_PANE,
                            (byte) 15,
                            " "
                    )
            );
        }

        Map<KitCategory, List<Kit>> kitsByCategory = new LinkedHashMap<>();

        for (Kit kit : SoupPvP.getInstance()
                .getKitsHandler()
                .getKits()) {

            kitsByCategory
                    .computeIfAbsent(
                            kit.getCategory(),
                            k -> new ArrayList<>()
                    )
                    .add(kit);
        }

        kitsByCategory.values().forEach(list ->
                list.sort(Comparator.comparing(Kit::getRarityType))
        );

        int row = 2;

        for (KitCategory category : KitCategory.values()) {

            if (category == KitCategory.ALL) {
                continue;
            }

            if (row > 5) {
                break;
            }

            int baseSlot = row * 9;

            List<Kit> kits = kitsByCategory.getOrDefault(
                    category,
                    Collections.emptyList()
            );

            int page = profile.getCategoryPage(category); // you'll need this

            int startIndex = page * 6;

            // Category icon (random kit from category)
            buttonMap.put(
                    baseSlot,
                    new CategoryRandomButton(category)
            );

            // Kits
            for (int i = 0; i < 6; i++) {

                int kitIndex = startIndex + i;

                if (kitIndex >= kits.size()) {
                    break;
                }

                buttonMap.put(
                        baseSlot + 1 + i,
                        new KitButton(kits.get(kitIndex))
                );
            }

//            // Previous page
//            buttonMap.put(
//                    baseSlot + 7,
//                    new CategoryPreviousButton(category)
//            );
//
//            // Next page
//            buttonMap.put(
//                    baseSlot + 8,
//                    new CategoryNextButton(category)
//            );

            row++;
        }

        return buttonMap;
    }

    @Override
    public int size(Map<Integer, Button> buttons) {
        return 54;
    }
}