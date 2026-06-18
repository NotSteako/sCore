package kami.gg.souppvp.kit;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.cosmetic.CosmeticSkin;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.PlayerUtil;
import kami.gg.souppvp.util.XPBarTimer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collections;
import java.util.List;

@Getter @Setter
public abstract class Kit implements Listener {

    public abstract String getName();
    public abstract KitRarity getRarityType();
    public abstract Integer getPrice();
    public abstract ItemStack getIcon();
    public abstract List<String> getDescription();
    public abstract List<ItemStack> getCombatEquipments();
    public abstract ItemStack[] getArmor(Player player);
    public abstract List<PotionEffect> getPotionEffects();


    public abstract KitCategory getCategory();


    public abstract void onSelect(Player player);

    public void onDeselect(Player player) {
        // no-op by default
    }

    public ItemStack[] getArmorForPreview() {
        return getArmor(null); // most kits won't need the player for armor items
    }

    public void setup() {

    }


    public void equipKit(Player player) {
        XPBarTimer.remove(player.getPlayer());

        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());

        // Notify the currently active kit that it's being replaced
        Kit previousKit = profile.getCurrentKit();
        if (previousKit != null) {
            previousKit.onDeselect(player);
        }

        profile.setProfileState(ProfileState.COMBAT);
        player.getInventory().clear();
        player.getInventory().setArmorContents(this.getArmor(player));


        List<ItemStack> equipments = this.getCombatEquipments();
        List<Integer> layout = profile.getKitLayouts() == null ? null : profile.getKitLayouts().get(this.getName());
        boolean[] used = new boolean[9];
        for (int i = 0; i < equipments.size(); i++) {
            int slot = -1;
            if (layout != null && i < layout.size() && layout.get(i) != null) {
                int desired = layout.get(i);
                if (desired >= 0 && desired <= 8 && !used[desired]) slot = desired;
            }
            if (slot == -1) {
                if (i <= 8 && !used[i]) slot = i;
                else for (int s = 0; s < 9; s++) if (!used[s]) { slot = s; break; }
            }
            if (slot == -1) slot = i;
            used[slot] = true;
            player.getInventory().setItem(slot, equipments.get(i));
        }

        PlayerUtil.giveSoup(player);
        for (PotionEffect potionEffect : this.getPotionEffects()) {
            player.addPotionEffect(potionEffect);
        }
        onSelect(player);
        setup();
        player.sendMessage(CC.translate("&aSuccessfully given you the kit &r" + this.getRarityType().getColor() + this.getName() + "&a."));
    }

    public List<CosmeticSkin> getAvailableCosmetics() {
        return Collections.emptyList();
    }




    public CosmeticSkin getSelectedCosmetic(Profile profile) {

        String cosmeticId =
                profile.getSelectedCosmetic(getName());

        for (CosmeticSkin skin : getAvailableCosmetics()) {
            if (skin.getId().equalsIgnoreCase(cosmeticId)) {
                return skin;
            }
        }

        return getDefaultCosmetic();
    }

    public CosmeticSkin getDefaultCosmetic() {
        return null;
    }

}