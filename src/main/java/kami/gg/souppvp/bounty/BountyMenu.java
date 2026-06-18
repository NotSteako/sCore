package kami.gg.souppvp.bounty;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.bounty.button.BountyPlayerButton;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.pagination.PaginatedMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BountyMenu extends PaginatedMenu {

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "Players with a bounty";
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        HashMap<Integer, Button> buttonHashMap = new HashMap<>();
        for (Player player1 : Bukkit.getOnlinePlayers()){
            Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player1.getUniqueId());
            if (profile.getBounty() > 0){
                buttonHashMap.put(buttonHashMap.size(), new BountyPlayerButton(player1));
            }
        }
        return buttonHashMap;
    }

}
