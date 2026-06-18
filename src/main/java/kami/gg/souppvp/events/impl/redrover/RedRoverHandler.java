package kami.gg.souppvp.events.impl.redrover;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.events.impl.redrover.task.RedRoverStartTask;
import kami.gg.souppvp.util.LocationUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

public class RedRoverHandler {

    @Getter private RedRover activeRedRover;
    @Getter @Setter private Location spectatorSpawn;
    @Getter @Setter private Location spawnA;
    @Getter @Setter private Location spawnB;

    public RedRoverHandler() { load(); }

    public void setActiveRedRover(RedRover redRover) {
        if (activeRedRover != null) activeRedRover.setEventTask(null);
        if (redRover == null) { activeRedRover = null; return; }
        activeRedRover = redRover;
        activeRedRover.setEventTask(new RedRoverStartTask(redRover));
    }

    public void load() {
        String spec = SoupPvP.getInstance().getConfig().getString("EVENTS.RED-ROVER.SPECTATOR-SPAWN",
                SoupPvP.getInstance().getConfig().getString("EVENTS.SUMO.SPECTATOR-SPAWN"));
        String a = SoupPvP.getInstance().getConfig().getString("EVENTS.RED-ROVER.SPAWN-A",
                SoupPvP.getInstance().getConfig().getString("EVENTS.SUMO.SPAWN-A"));
        String b = SoupPvP.getInstance().getConfig().getString("EVENTS.RED-ROVER.SPAWN-B",
                SoupPvP.getInstance().getConfig().getString("EVENTS.SUMO.SPAWN-B"));

        if (spec != null) spectatorSpawn = LocationUtil.deserialize(spec);
        if (a != null)    spawnA = LocationUtil.deserialize(a);
        if (b != null)    spawnB = LocationUtil.deserialize(b);
    }

    public void save() {
        SoupPvP.getInstance().getConfig().set("EVENTS.RED-ROVER.SPECTATOR-SPAWN", LocationUtil.serialize(spectatorSpawn));
        SoupPvP.getInstance().getConfig().set("EVENTS.RED-ROVER.SPAWN-A", LocationUtil.serialize(spawnA));
        SoupPvP.getInstance().getConfig().set("EVENTS.RED-ROVER.SPAWN-B", LocationUtil.serialize(spawnB));
        SoupPvP.getInstance().saveConfig();
        SoupPvP.getInstance().reloadConfig();
    }

}