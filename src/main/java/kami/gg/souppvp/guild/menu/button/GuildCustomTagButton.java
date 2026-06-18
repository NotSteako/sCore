package kami.gg.souppvp.guild.menu.button;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.Guild;
import kami.gg.souppvp.guild.GuildText;
import kami.gg.souppvp.guild.listener.GuildHexChatHandler;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Glass block "Custom" — closes the menu and asks the player to type their
 * desired hex code in chat (or "cancel" to abort).
 */
public class GuildCustomTagButton extends Button {

    private final Guild guild;

    public GuildCustomTagButton(Guild guild) {
        this.guild = guild;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        List<String> lore = new ArrayList<>();
        lore.add(GuildText.translate("&7Set a custom hex tag colour."));
        lore.add(GuildText.translate("&7You will be asked to type"));
        lore.add(GuildText.translate("&7the hex code in chat."));
        lore.add("");
        if (guild != null && guild.getTag() != null && guild.getTag().startsWith("#")) {
            lore.add(GuildText.translate("&aCurrent: " + "&7[" + guild.getTag() + guild.getName() + "&7]&r"));
        } else {
            lore.add(GuildText.translate("&eClick to set a custom hex colour."));
        }
        return new ItemBuilder(Material.GLASS)
                .name(GuildText.translate("&b&lCustom Hex Colour"))
                .lore(lore)
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (guild == null) {
            playFail(player);
            return;
        }
        if (!guild.isLeader(player.getUniqueId())) {
            playFail(player);
            player.sendMessage(GuildText.translate("&cOnly the guild leader can change the tag."));
            return;
        }

        // Defer to next tick: the inventory close must happen AFTER this click
        // event finishes, otherwise Bukkit serialises a re-open on top of it.
        Bukkit.getScheduler().runTask(SoupPvP.getInstance(), () -> {
            player.closeInventory();
            GuildHexChatHandler.beginInput(player, guild);
        });
    }

    /**
     * Tell the Menu framework NOT to refresh / re-open the menu after this
     * click. Without this, MenuListener would immediately re-render the colour
     * picker on top of our closeInventory() call.
     */
    @Override
    public boolean shouldUpdate(Player player, ClickType clickType) {
        return false;
    }
}