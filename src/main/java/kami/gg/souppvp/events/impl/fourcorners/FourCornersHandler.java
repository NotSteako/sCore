package kami.gg.souppvp.events.impl.fourcorners;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.fourcorners.task.FourCornersStartTask;
import kami.gg.souppvp.util.LocationUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.EnumMap;
import java.util.Map;

public class FourCornersHandler {

    @Getter private FourCorners activeEvent;
    @Getter @Setter private Location spectatorSpawn;
    @Getter private final Map<FourCornersCorner, Location> cornerLocations = new EnumMap<>(FourCornersCorner.class);
    @Getter @Setter private double cornerRadius;

    public FourCornersHandler() { load(); }

    public void setActiveEvent(FourCorners event) {
        if (activeEvent != null) activeEvent.setEventTask(null);
        if (event == null) { activeEvent = null; return; }
        activeEvent = event;
        activeEvent.setEventTask(new FourCornersStartTask(event));
    }

    public Location getCornerLocation(FourCornersCorner corner) { return cornerLocations.get(corner); }
    public void setCornerLocation(FourCornersCorner corner, Location loc) { cornerLocations.put(corner, loc); }

    public void load() {
        String specRaw = SoupPvP.getInstance().getConfig().getString("EVENTS.FOURCORNERS.SPECTATOR-SPAWN");
        if (specRaw != null) spectatorSpawn = LocationUtil.deserialize(specRaw);
        for (FourCornersCorner corner : FourCornersCorner.values()) {
            String raw = SoupPvP.getInstance().getConfig().getString("EVENTS.FOURCORNERS.CORNERS." + corner.name());
            if (raw != null && !raw.isEmpty()) cornerLocations.put(corner, LocationUtil.deserialize(raw));
        }
        cornerRadius = SoupPvP.getInstance().getConfig().getDouble("EVENTS.FOURCORNERS.CORNER-RADIUS", 4.0);
    }

    public void save() {
        if (spectatorSpawn != null)
            SoupPvP.getInstance().getConfig().set("EVENTS.FOURCORNERS.SPECTATOR-SPAWN", LocationUtil.serialize(spectatorSpawn));
        for (FourCornersCorner corner : FourCornersCorner.values()) {
            Location loc = cornerLocations.get(corner);
            if (loc != null)
                SoupPvP.getInstance().getConfig().set("EVENTS.FOURCORNERS.CORNERS." + corner.name(), LocationUtil.serialize(loc));
        }
        SoupPvP.getInstance().getConfig().set("EVENTS.FOURCORNERS.CORNER-RADIUS", cornerRadius);
        SoupPvP.getInstance().saveConfig();
        SoupPvP.getInstance().reloadConfig();
    }
}