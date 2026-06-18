package kami.gg.souppvp.kit.menu;

import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.button.view.CombatEquipmentButton;
import kami.gg.souppvp.kit.button.view.PotionEffectsButton;
import kami.gg.souppvp.kit.button.view.amor.BootsButton;
import kami.gg.souppvp.kit.button.view.amor.ChestplateButton;
import kami.gg.souppvp.kit.button.view.amor.HelmetButton;
import kami.gg.souppvp.kit.button.view.amor.LeggingsButton;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.menu.Button;
import kami.gg.souppvp.util.menu.Menu;
import kami.gg.souppvp.util.menu.button.BackButton;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class KitViewMenu extends Menu {

    private Kit kit;

    public KitViewMenu(Kit kit){
        this.kit = kit;
    }

    @Override
    public String getTitle(Player player) {
        return CC.translate("Viewing the " + kit.getName() + " kit");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttonMap = new HashMap<>();
        int j = 0;
        int position = 27;
        for (ItemStack itemStack : kit.getCombatEquipments()){
            buttonMap.put(position, new CombatEquipmentButton(kit, j));
            j++;
            position++;
        }
        List<ItemStack> list;
        list = Arrays.asList(kit.getArmor(player));
        if (list.get(0) == null){
            buttonMap.put(36, Button.placeholder(Material.RED_STAINED_GLASS_PANE, (byte) 14, " "));
        } else {
            buttonMap.put(36, new HelmetButton(kit));
        }
        if (list.get(1) == null){
            buttonMap.put(37, Button.placeholder(Material.RED_STAINED_GLASS_PANE, (byte) 14, " "));
        } else {
            buttonMap.put(37, new ChestplateButton(kit));
        }
        if (list.get(2) == null){
            buttonMap.put(38, Button.placeholder(Material.RED_STAINED_GLASS_PANE, (byte) 14, " "));
        } else {
            buttonMap.put(38, new LeggingsButton(kit));
        }
        if (list.get(3) == null){
            buttonMap.put(39, Button.placeholder(Material.RED_STAINED_GLASS_PANE, (byte) 14, " "));
        } else {
            buttonMap.put(39, new BootsButton(kit));
        }
        buttonMap.put(40, new PotionEffectsButton(kit));
        buttonMap.put(44, new BackButton(new KitsSelectMenu()));
        for (int i=0; i<36; i++){
            buttonMap.putIfAbsent(i, Button.placeholder(Material.MUSHROOM_STEW, (byte) 0, ""));
        }
        for (int i=41; i<45; i++){
            buttonMap.putIfAbsent(i, Button.placeholder(Material.RED_STAINED_GLASS_PANE, (byte) 15, " "));
        }
        return buttonMap;
    }


}
