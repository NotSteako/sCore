package kami.gg.souppvp.command.guild;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.guild.Guild;
import kami.gg.souppvp.guild.GuildNameValidator;
import kami.gg.souppvp.handlers.GuildsHandler;
import kami.gg.souppvp.listener.LunarClientListener;
import kami.gg.souppvp.util.CC;
import org.bukkit.entity.Player;

public class GuildCreateCommand {

    @Command(name = "create", desc = "Create a new guild", usage = "<name>")
    public void execute(@Sender Player player, String name) {
        GuildsHandler handler = SoupPvP.getInstance().getGuildsHandler();
        if (handler.getByPlayer(player.getUniqueId()) != null) {
            player.sendMessage(CC.translate("&cYou are already in a guild. Leave it before creating a new one."));
            return;
        }
        GuildNameValidator.Result result = GuildNameValidator.validateFormat(name);
        if (result != GuildNameValidator.Result.OK) {
            player.sendMessage(CC.translate("&c" + GuildNameValidator.describe(result)));
            return;
        }
        if (handler.isBlocked(name)) {
            player.sendMessage(CC.translate("&c" + GuildNameValidator.describe(GuildNameValidator.Result.BLOCKED)));
            return;
        }
        if (handler.isTaken(name)) {
            player.sendMessage(CC.translate("&c" + GuildNameValidator.describe(GuildNameValidator.Result.TAKEN)));
            return;
        }
        Guild guild = handler.create(name, player.getUniqueId());
        handler.setProfileGuild(player.getUniqueId(), guild.getName());

        player.sendMessage(CC.translate("&aGuild " + guild.getColoredTag()
                + " &acreated! Use &e/guild tag &ato pick a colour."));
        LunarClientListener.updateNametag(player);
    }
}