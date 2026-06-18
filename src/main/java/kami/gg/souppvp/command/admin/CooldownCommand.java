package kami.gg.souppvp.command.admin;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class CooldownCommand {

    private static volatile boolean globalBypass = false;

    /** Checked by TimersHandler#addPlayerTimer to skip registering cooldown timers. */
    public static boolean isBypassed() {
        return globalBypass;
    }

    /** UUID overload for compatibility with any per-player call sites. */
    public static boolean isBypassed(UUID uuid) {
        return globalBypass;
    }

    @Command(name = "", desc = "Toggle or reset ability cooldowns server-wide.", usage = "<off|on|reset>")
    @Require("op")
    public void root(@Sender CommandSender sender) {
        sender.sendMessage(CC.translate("&6/cooldown off &7- disable cooldowns server-wide"));
        sender.sendMessage(CC.translate("&6/cooldown on &7- re-enable cooldowns server-wide"));
        sender.sendMessage(CC.translate("&6/cooldown reset &7- clear all active cooldown timers"));
        sender.sendMessage(CC.translate("&7Status: " + (globalBypass ? "&aOFF (bypassed)" : "&cON (active)")));
    }

    @Command(name = "off", desc = "Disable ability cooldowns for all players.", usage = "")
    @Require("op")
    public void off(@Sender CommandSender sender) {
        globalBypass = true;
        SoupPvP.getInstance().getTimersHandler().getPrimaryAbilitiesHashMap().clear();
        SoupPvP.getInstance().getTimersHandler().getSecondaryAbilitiesHashMap().clear();
        Bukkit.broadcastMessage(CC.translate("&a[SoupPvP] &lAbility cooldowns DISABLED &aserver-wide by &f" + sender.getName() + "&a."));
    }

    @Command(name = "on", desc = "Re-enable ability cooldowns for all players.", usage = "")
    @Require("op")
    public void on(@Sender CommandSender sender) {
        globalBypass = false;
        Bukkit.broadcastMessage(CC.translate("&e[SoupPvP] &lAbility cooldowns ENABLED &eserver-wide by &f" + sender.getName() + "&e."));
    }

    @Command(name = "reset", desc = "Instantly clear all active ability cooldown timers.", usage = "")
    @Require("op")
    public void reset(@Sender CommandSender sender) {
        SoupPvP.getInstance().getTimersHandler().getPrimaryAbilitiesHashMap().clear();
        SoupPvP.getInstance().getTimersHandler().getSecondaryAbilitiesHashMap().clear();
        Bukkit.broadcastMessage(CC.translate("&b[SoupPvP] &lAll ability cooldowns RESET &bby &f" + sender.getName() + "&b."));
    }
}