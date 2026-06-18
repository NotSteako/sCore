package kami.gg.souppvp.events;

import kami.gg.souppvp.SoupPvP;
import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum Events {

    SUMO(SoupPvP.getInstance().getSumoHandler().getActiveSumo(), "Sumo", Material.LEAD),
    FOUR_CORNERS(SoupPvP.getInstance().getFourCornersHandler().getActiveEvent(), "4Corners", Material.WHITE_WOOL);


    Events(Object object, String title, Material material){
        this.object = object;
        this.title = title;
        this.material = material;
    }

    private Object object;
    private String title;
    private Material material;

}
