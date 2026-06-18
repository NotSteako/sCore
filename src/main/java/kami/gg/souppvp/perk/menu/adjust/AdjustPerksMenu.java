package kami.gg.souppvp.perk.menu.adjust;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.perk.Perk;
import kami.gg.souppvp.perk.menu.PerksMenu;
import kami.gg.souppvp.perk.menu.adjust.button.AdjustActivePerkSlotButton;
import kami.gg.souppvp.perk.menu.adjust.button.AdjustEmptyPerkSlotButton;
import kami.gg.souppvp.perk.menu.adjust.button.AdjustResetPerkButton;
import kami.gg.souppvp.perk.menu.adjust.button.PerkButton;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.Menu;
import kami.gg.souppvp.util.menu.button.BackButton;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class AdjustPerksMenu extends Menu {

    private int tier;

    public AdjustPerksMenu(int tier){
        this.tier = tier;
    }

    @Override
    public String getTitle(Player player) {
        String color = null;
        if (tier == 1) color = "&e";
        if (tier == 2) color = "&c";
        if (tier == 3) color = "&5";
        return CC.translate(color + "Tier " + tier + " Perks");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        Perk currentTierPerk = SoupPvP.getInstance().getPerksHandler().getPerkByName(profile.getActivePerks().get(tier-1));
        HashMap<Integer, Button> buttonHashMap = new HashMap<>();
        buttonHashMap.put(0, new BackButton(new PerksMenu()));
        if (currentTierPerk == null) {
            buttonHashMap.put(4, new AdjustEmptyPerkSlotButton(tier));
        } else {
            buttonHashMap.put(4, new AdjustActivePerkSlotButton(tier));
        }
        buttonHashMap.put(8, new AdjustResetPerkButton(tier));
        for (int i=0; i<27; i++){
            buttonHashMap.putIfAbsent(i, Button.placeholder(Material.RED_STAINED_GLASS_PANE, (byte) 15, " "));
        }
        for (int i=27; i<54; i++){
            if (i == 27 || i == 35 || i == 36 || i >= 44){
                buttonHashMap.put(i, Button.placeholder(Material.RED_STAINED_GLASS_PANE, (byte) 15, " "));
            }
        }
        int i=28;
        for (Perk perk : SoupPvP.getInstance().getPerksHandler().getPerks()){
            if (perk.getTier() == tier){
                buttonHashMap.putIfAbsent(i, new PerkButton(perk, perk.getTier()));
                if (i == 34){
                    i = 37;
                } else {
                    i++;
                }
            }
        }
        return buttonHashMap;
    }

    @Override
    public int size(Map<Integer, Button> buttons) {
        return 54;
    }
}
