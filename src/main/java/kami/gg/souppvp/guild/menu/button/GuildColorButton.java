package kami.gg.souppvp.guild.menu.button;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.Guild;
import kami.gg.souppvp.guild.GuildText;
import kami.gg.souppvp.listener.LunarClientListener;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import xyz.refinedev.phoenix.Phoenix;

import java.util.ArrayList;
import java.util.List;

public class GuildColorButton extends Button {

    private final String code;
    private final Material wool;
    private final Guild guild;

    public GuildColorButton(String code, Material wool, Guild guild) {
        this.code = code; this.wool = wool; this.guild = guild;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        boolean selected = guild != null && code.equals(guild.getTag());
        List<String> lore = new ArrayList<>();
        Phoenix api2 = Phoenix.getInstance();

        xyz.refinedev.phoenix.profile.IProfile killerPhoenixProfile =
                api2.getProfileHandler().getProfile(player.getUniqueId() );

        lore.add(GuildText.translate("&7Preview:"));
        lore.add(GuildText.translate(killerPhoenixProfile.getHighestRank().getColor() + player.getName() +"&r "  + "&7[" + code + (guild == null ? "Guild" : guild.getName()) + "&7]&r"));
        lore.add("");
        lore.add(GuildText.translate(selected ? "&aCurrently selected." : "&eClick to select this colour."));

        ItemBuilder builder = new ItemBuilder(wool)
                .name(GuildText.translate(code + "&lColour &7(" + code + ")"))
                .lore(lore);
        if (selected) builder.enchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 1);
        return builder.build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (guild == null) { playFail(player); return; }
        if (!guild.isLeader(player.getUniqueId())) {
            playFail(player);
            player.sendMessage(GuildText.translate("&cOnly the guild leader can change the tag."));
            return;
        }
        guild.setTag(code);
        SoupPvP.getInstance().getGuildsHandler().save(guild);
        player.sendMessage(GuildText.translate("&aGuild tag colour set to "  + "&7[&r" + code + guild.getName() + "&7]&a."));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1F, 1.6F);
        LunarClientListener.updateNametag(player);
    }
}