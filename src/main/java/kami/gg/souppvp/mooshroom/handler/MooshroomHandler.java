package kami.gg.souppvp.mooshroom.handler;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MushroomCow;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Stores, spawns, removes, and persists the named Mooshrooms created with
 * /mooshroom create <name>.
 *
 * Saved to plugins/SoupPvP/mooshrooms.yml.
 */
public class MooshroomHandler {

    public static final String METADATA_KEY = "souppvp_soup_mooshroom";
    public static final String METADATA_NAME = "souppvp_soup_mooshroom_name";

    private static final String FILE_NAME = "mooshrooms.yml";

    private final Map<String, Location> locationByName = new LinkedHashMap<>();
    private final Map<String, UUID>     entityByName   = new HashMap<>();

    private final File file;
    private YamlConfiguration config;

    public MooshroomHandler() {
        SoupPvP plugin = SoupPvP.getInstance();
        plugin.getDataFolder().mkdirs();
        file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException ignored) {}
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadLocations();

        // onEnable() purges all non-player entities at the end, so we wait one
        // tick before respawning our saved mooshrooms.
        Bukkit.getScheduler().runTaskLater(plugin, this::respawnAll, 5L);
    }

    /* ---------- persistence ---------- */

    private void loadLocations() {
        ConfigurationSection section = config.getConfigurationSection("mooshrooms");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            ConfigurationSection s = section.getConfigurationSection(key);
            if (s == null) continue;
            World w = Bukkit.getWorld(s.getString("world", ""));
            if (w == null) continue;
            Location loc = new Location(w,
                    s.getDouble("x"),
                    s.getDouble("y"),
                    s.getDouble("z"),
                    (float) s.getDouble("yaw"),
                    (float) s.getDouble("pitch"));
            locationByName.put(key.toLowerCase(), loc);
        }
    }

    private void save() {
        config.set("mooshrooms", null);
        for (Map.Entry<String, Location> e : locationByName.entrySet()) {
            String path = "mooshrooms." + e.getKey();
            Location loc = e.getValue();
            config.set(path + ".world", loc.getWorld().getName());
            config.set(path + ".x",     loc.getX());
            config.set(path + ".y",     loc.getY());
            config.set(path + ".z",     loc.getZ());
            config.set(path + ".yaw",   loc.getYaw());
            config.set(path + ".pitch", loc.getPitch());
        }
        try { config.save(file); } catch (IOException ignored) {}
    }

    /* ---------- spawning ---------- */

    private MushroomCow spawnEntity(String name, Location loc) {
        MushroomCow cow = (MushroomCow) loc.getWorld()
                .spawnEntity(loc, EntityType.MOOSHROOM);

        cow.setAI(false);
        cow.setSilent(true);
        cow.setRemoveWhenFarAway(false);
        cow.setCanPickupItems(false);
        cow.setInvulnerable(true);   // can be hit, can't be killed
        cow.setCollidable(true);

        cow.setCustomName(CC.translate("&d&l" + name + " &7(Right-Click for Soup)"));
        cow.setCustomNameVisible(true);

        cow.setMetadata(METADATA_KEY,
                new FixedMetadataValue(SoupPvP.getInstance(), true));
        cow.setMetadata(METADATA_NAME,
                new FixedMetadataValue(SoupPvP.getInstance(), name.toLowerCase()));

        entityByName.put(name.toLowerCase(), cow.getUniqueId());
        return cow;
    }

    private void respawnAll() {
        for (Map.Entry<String, Location> e : locationByName.entrySet()) {
            spawnEntity(e.getKey(), e.getValue());
        }
    }

    /* ---------- public API ---------- */

    /** @return true on success, false if the name was already taken. */
    public boolean create(String name, Location loc) {
        String key = name.toLowerCase();
        if (locationByName.containsKey(key)) return false;
        locationByName.put(key, loc);
        spawnEntity(name, loc);
        save();
        return true;
    }

    /** @return true on success, false if no such Mooshroom exists. */
    public boolean remove(String name) {
        String key = name.toLowerCase();
        if (!locationByName.containsKey(key)) return false;

        UUID uuid = entityByName.remove(key);
        if (uuid != null) {
            Entity e = Bukkit.getEntity(uuid);
            if (e != null) e.remove();
        }
        // Belt-and-braces: also remove any tagged entity with this name in the
        // world (in case the UUID got lost across reloads).
        for (World w : Bukkit.getWorlds()) {
            for (Entity ent : w.getEntities()) {
                if (ent.hasMetadata(METADATA_NAME)
                        && key.equals(ent.getMetadata(METADATA_NAME).get(0).asString())) {
                    ent.remove();
                }
            }
        }

        locationByName.remove(key);
        save();
        return true;
    }

    public Set<String> getNames() {
        return Collections.unmodifiableSet(locationByName.keySet());
    }

    public Location getLocation(String name) {
        return locationByName.get(name.toLowerCase());
    }
}