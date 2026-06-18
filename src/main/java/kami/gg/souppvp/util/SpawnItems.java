package kami.gg.souppvp.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collections;

public class SpawnItems {

    public static ItemStack KITS_SELECTOR = new ItemBuilder(Material.ENCHANTED_BOOK)
            .name(CC.translate("&bKits Selector &7(Right Click)"))
            .build();

    public static ItemStack HOST_EVENTS = new ItemBuilder(Material.DIAMOND)
            .name(CC.translate("&bHost Events &7(Right Click)"))
            .build();

    public static ItemStack GAME_PERKS = new ItemBuilder(Material.CHEST)
            .name(CC.translate("&bGame Perks &7(Right Click)"))
            .build();

    public static ItemStack YOUR_STATISTICS =  new ItemBuilder(Material.PLAYER_HEAD)
            .name(CC.translate("&bYour Statistics &7(Right Click)"))
            .durability((short) 3)
            .build();

    public static ItemStack PREVIOUS_KIT = new ItemBuilder(Material.EMERALD)
            .name(CC.translate("&bSelect Previous Kit &7(Right Click)"))
            .build();

    public static ItemStack YOUR_OPTIONS = new ItemBuilder(Material.NETHER_STAR)
            .name(CC.translate("&bConfigure Options &7(Right Click)"))
            .build();


    public static ItemStack FFA = new ItemBuilder(Material.CLOCK)
            .name(CC.translate("&bFFA &7(Right Click)"))
            .build();

    public static ItemStack getYourStatistics(Player player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(CC.translate("&bYour Statistics &7(Right Click)"));

        item.setItemMeta(meta);
        return item;
    }

}


