package kami.gg.souppvp.events.menu.button;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.colourshuffle.ColourShuffle;
import kami.gg.souppvp.events.impl.colourshuffle.ColourShuffleState;
import kami.gg.souppvp.events.impl.colourshuffle.task.ColourShuffleStartTask;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.PlayerUtil;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ColourShuffleEventButton extends Button {


    private static final Material[] WOOLS = {
            Material.WHITE_WOOL,
            Material.ORANGE_WOOL,
            Material.MAGENTA_WOOL,
            Material.LIGHT_BLUE_WOOL,
            Material.YELLOW_WOOL,
            Material.LIME_WOOL,
            Material.PINK_WOOL,
            Material.GRAY_WOOL,
            Material.LIGHT_GRAY_WOOL,
            Material.CYAN_WOOL,
            Material.PURPLE_WOOL,
            Material.BLUE_WOOL,
            Material.BROWN_WOOL,
            Material.GREEN_WOOL,
            Material.RED_WOOL,
            Material.BLACK_WOOL
    };

    @Override
    public ItemStack getButtonItem(Player player) {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        List<String> lore = new ArrayList<>();
        lore.add(CC.translate("&7Stand on the called colour"));
        lore.add(CC.translate("&7or fall through the floor."));
        lore.add(CC.translate("&7Last player standing wins."));
        lore.add("");

        int index = (int) ((System.currentTimeMillis() / 500L) % WOOLS.length);
        Material wool = WOOLS[index];

        ColourShuffle ev = SoupPvP.getInstance().getColourShuffleHandler().getActiveEvent();
        if (ev != null) {
            lore.add(CC.translate("&dOngoing Colour Shuffle Event:"));
            lore.add(CC.translate("&7• &fHost: &d" + ev.getHost().getUsername()));
            lore.add(CC.translate("&7• &fParticipants: &d" + ev.getEventPlayers().size() + "&f/&d" + ev.getMaxPlayers()));
            if (ev.getState() == ColourShuffleState.WAITING) {
                lore.add(CC.translate("&7• &fState: &dWaiting..."));
            } else {
                lore.add(CC.translate("&7• &fState: &dRunning (round " + ev.getCurrentRound() + ")"));
            }
            lore.add("");
            if (profile.getColourShuffleEvent() != null) {
                lore.add(CC.translate("&eYou're in this event!"));
            } else {
                lore.add(CC.translate("&eClick to join!"));
            }
        } else {
            lore.add(CC.translate("&eClick to host!"));
        }
        return new ItemBuilder(wool)
                .name(CC.translate("&dWool Shuffle &7Event"))
                .lore(lore)
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (!clickType.isLeftClick()) return;
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
        if (!profile.getProfileState().equals(ProfileState.SPAWN)) {
            playFail(player);
            player.sendMessage(CC.translate("&cYou can only do this at spawn."));
            return;
        }

        ColourShuffle existing = SoupPvP.getInstance().getColourShuffleHandler().getActiveEvent();
        if (existing == null) {
            if (!player.hasPermission("souppvp.colourshufflehost")) {
                PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK);
                player.sendMessage(CC.translate("&cYou don't have permission to host a Colour Shuffle event."));
                return;
            }
            if (!SoupPvP.getInstance().getColourShuffleHandler().isConfigured()) {
                PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK);
                player.sendMessage(CC.translate("&cColour Shuffle isn't fully configured. Set spawn + floor corners first."));
                return;
            }
            ColourShuffle event = new ColourShuffle(player);
            SoupPvP.getInstance().getColourShuffleHandler().setActiveEvent(event);
            event.setEventTask(new ColourShuffleStartTask(event));
            event.handleJoin(player);
            PlayerUtil.playSound(player, Sound.UI_BUTTON_CLICK);
            return;
        }

        // Active event exists — try to join.
        if (profile.getColourShuffleEvent() != null) {
            PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK);
            return;
        }
        if (existing.getState() != ColourShuffleState.WAITING) {
            PlayerUtil.playSound(player, Sound.BLOCK_GRASS_BREAK);
            player.sendMessage(CC.translate("&cThat event has already started."));
            return;
        }
        existing.handleJoin(player);
        PlayerUtil.playSound(player, Sound.UI_BUTTON_CLICK);
    }
}
