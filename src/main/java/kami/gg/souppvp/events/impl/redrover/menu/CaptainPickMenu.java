package kami.gg.souppvp.events.impl.redrover.menu;

import kami.gg.souppvp.events.impl.redrover.RedRover;
import kami.gg.souppvp.events.impl.redrover.player.RedRoverPlayer;
import kami.gg.souppvp.events.impl.redrover.player.RedRoverTeam;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CaptainPickMenu extends Menu {

    private final RedRover redRover;

    public CaptainPickMenu(RedRover redRover) {
        this.redRover = redRover;
        setAutoUpdate(true);
    }

    @Override
    public String getTitle(Player player) { return CC.translate("Pick a player"); }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> map = new HashMap<>();
        int slot = 10;
        for (RedRoverPlayer target : redRover.getUndraftedPlayers()) {
            if (slot % 9 == 8) slot += 2;
            if (slot >= 44) break;
            map.put(slot, new PickButton(redRover, target.getUuid(), target.getUsername()));
            slot++;
        }
        setPlaceholder(true);
        return map;
    }

    @Override
    public int size(Map<Integer, Button> buttons) { return 54; }

    private static class PickButton extends Button {
        private final RedRover redRover;
        private final UUID targetUuid;
        private final String targetName;

        PickButton(RedRover redRover, UUID targetUuid, String targetName) {
            this.redRover = redRover;
            this.targetUuid = targetUuid;
            this.targetName = targetName;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwner(targetName);
                RedRoverPlayer rp = redRover.getEventPlayers().get(targetUuid);
                RedRoverTeam pickTeam = redRover.getPickingTeam();
                meta.setDisplayName(CC.translate(pickTeam.getColor() + targetName));
                List<String> lore = new ArrayList<>();
                lore.add(CC.translate("&7Click to draft to team " + pickTeam.getColor() + pickTeam.getReadable() + "&7."));
                if (rp != null) lore.add(CC.translate("&7Round wins: &f" + rp.getRoundWins()));
                meta.setLore(lore);
                skull.setItemMeta(meta);
            }
            return skull;
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            playNeutral(player);
            redRover.pickPlayer(player, targetUuid);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.5F);
            player.closeInventory();
        }
    }

}