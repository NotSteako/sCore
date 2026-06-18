package kami.gg.souppvp.kit.button;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.cosmetics.PreviewSession;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.cosmetic.CosmeticSkin;
import kami.gg.souppvp.kit.cosmetic.SkinApplier;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.menu.Button;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class CosmeticSelectButton extends Button {

    private final Kit kit;
    private final CosmeticSkin skin;

    public CosmeticSelectButton(
            Kit kit,
            CosmeticSkin skin
    ) {
        this.kit = kit;
        this.skin = skin;
    }

    @Override
    public ItemStack getButtonItem(Player player) {

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);

        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        SkinApplier.apply(meta, skin);

        meta.setDisplayName(CC.translate(skin.getDisplayName()));

        skull.setItemMeta(meta);

        return skull;
    }

    @Override
    public void clicked(Player player, ClickType clickType) {

        if (clickType == ClickType.LEFT) {
            Profile profile = SoupPvP.getInstance()
                    .getProfilesHandler()
                    .getProfileByUUID(player.getUniqueId());

            profile.setSelectedCosmetic(
                    kit.getName(),
                    skin.getId()
            );

            player.sendMessage(CC.translate(
                    "&aSelected " + skin.getDisplayName()
            ));
            Button.playSuccess(player);

        } else if (clickType == ClickType.RIGHT) {
            player.closeInventory();
            // Small delay so inventory close doesn't interfere with gamemode switch
            Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
                PreviewSession.startPreview(player, kit, skin);
            }, 2L);
        }

    }
}