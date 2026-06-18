package kami.gg.souppvp.util.assemble;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

public interface AssembleAdapter {

	Component getTitle(Player player);

	List<Component> getLines(Player player);

}