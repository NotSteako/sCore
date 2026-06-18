package kami.gg.souppvp.perk.menu;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.perk.menu.button.ActivePerkSlotButton;
import kami.gg.souppvp.perk.menu.button.EmptyPerkSlotButton;
import kami.gg.souppvp.perk.menu.button.WhatArePerksButton;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.Menu;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PerksMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return "Perk Shop";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        HashMap<Integer, Button> buttonHashMap = new HashMap<>();
        buttonHashMap.put(4, new WhatArePerksButton());
        if (SoupPvP.getInstance().getPerksHandler().getPerkByName(profile.getActivePerks().get(0)) == null){
            buttonHashMap.put(19, new EmptyPerkSlotButton(1));
        } else {
            buttonHashMap.put(19, new ActivePerkSlotButton(1));
        }
        if (SoupPvP.getInstance().getPerksHandler().getPerkByName(profile.getActivePerks().get(1)) == null){
            buttonHashMap.put(22, new EmptyPerkSlotButton(2));
        } else {
            buttonHashMap.put(22, new ActivePerkSlotButton(2));
        }
        if (SoupPvP.getInstance().getPerksHandler().getPerkByName(profile.getActivePerks().get(2)) == null){
            buttonHashMap.put(25, new EmptyPerkSlotButton(3));
        } else {
            buttonHashMap.put(25, new ActivePerkSlotButton(3));
        }
        setPlaceholder(true);
        return buttonHashMap;
    }

    @Override
    public int size(Map<Integer, Button> buttons) {
        return 45;
    }
}
