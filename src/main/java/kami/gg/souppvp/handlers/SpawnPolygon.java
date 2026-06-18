package kami.gg.souppvp.handlers;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An XZ polygon that defines the spawn border.
 * Points are stored as flat [x, z] pairs; the polygon is infinite in Y.
 */
public class SpawnPolygon {

    private final List<double[]> points; // each entry: {x, z}
    private final World world;

    public SpawnPolygon(World world, List<double[]> points) {
        this.world = world;
        this.points = Collections.unmodifiableList(new ArrayList<>(points));
    }

    public World getWorld() { return world; }
    public List<double[]> getPoints() { return this.points; }
    public int size() { return points.size(); }
    public boolean isValid() { return points.size() >= 3; }

    /**
     * Point-in-polygon (ray casting) — ignores Y.
     */
    public boolean contains(double x, double z) {
        int n = points.size();
        boolean inside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = points.get(i)[0], zi = points.get(i)[1];
            double xj = points.get(j)[0], zj = points.get(j)[1];
            if (((zi > z) != (zj > z)) && (x < (xj - xi) * (z - zi) / (zj - zi) + xi)) {
                inside = !inside;
            }
        }
        return inside;
    }

    /**
     * Walks every edge of the polygon and calls the consumer for each
     * block-sized step along the perimeter.
     */
    public void forEachEdgePoint(EdgePointConsumer consumer) {
        int n = points.size();
        for (int i = 0; i < n; i++) {
            double[] a = points.get(i);
            double[] b = points.get((i + 1) % n);
            double dx = b[0] - a[0];
            double dz = b[1] - a[1];
            double length = Math.sqrt(dx * dx + dz * dz);
            int steps = (int) Math.ceil(length);
            for (int s = 0; s < steps; s++) {
                double t = (double) s / steps;
                double x = a[0] + dx * t;
                double z = a[1] + dz * t;
                consumer.accept((int) Math.floor(x), (int) Math.floor(z));
            }
        }
    }

    @FunctionalInterface
    public interface EdgePointConsumer {
        void accept(int x, int z);
    }

    // ── Config serialisation ──────────────────────────────────────────────────

    public void save(ConfigurationSection section) {
        section.set("world", world.getName());
        List<String> encoded = new ArrayList<>();
        for (double[] p : points) {
            encoded.add(p[0] + "," + p[1]);
        }
        section.set("points", encoded);
    }

    public static SpawnPolygon load(ConfigurationSection section, org.bukkit.Bukkit bukkit) {
        String worldName = section.getString("world");
        World w = org.bukkit.Bukkit.getWorld(worldName);
        if (w == null) throw new IllegalStateException("World '" + worldName + "' not found");

        List<String> encoded = section.getStringList("points");
        List<double[]> pts = new ArrayList<>();
        for (String s : encoded) {
            String[] parts = s.split(",");
            pts.add(new double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])});
        }
        return new SpawnPolygon(w, pts);
    }
}