package kami.gg.souppvp.kit.editor;

import kami.gg.souppvp.kit.Kit;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@Getter
public class KitEditorHolder implements InventoryHolder {

    private final Player player;
    private final Kit kit;
    private Inventory inventory;

    public KitEditorHolder(Player player, Kit kit) {
        this.player = player;
        this.kit = kit;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}