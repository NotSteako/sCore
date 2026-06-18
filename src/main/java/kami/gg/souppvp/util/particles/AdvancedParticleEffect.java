package kami.gg.souppvp.util.particles;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 1.21.8 port of the legacy AdvancedParticleEffect (originally NMS reflection).
 * This rewrite preserves the public API but emits particles through the modern
 * Bukkit {@link org.bukkit.Particle} API. Colour/data variants delegate to safe
 * defaults if unsupported on the running server.
 */
public enum AdvancedParticleEffect {
    EXPLOSION_NORMAL("POOF",            "EXPLOSION_NORMAL", "EXPLOSION"),
    EXPLOSION_LARGE ("EXPLOSION",       "EXPLOSION_LARGE"),
    EXPLOSION_HUGE  ("EXPLOSION_EMITTER","EXPLOSION_HUGE"),
    FIREWORKS_SPARK ("FIREWORK",        "FIREWORKS_SPARK"),
    WATER_BUBBLE    ("BUBBLE",          "WATER_BUBBLE"),
    WATER_SPLASH    ("SPLASH",          "WATER_SPLASH"),
    WATER_WAKE      ("FISHING",         "WATER_WAKE"),
    SUSPENDED       ("UNDERWATER",      "SUSPENDED"),
    SUSPENDED_DEPTH ("UNDERWATER",      "SUSPENDED_DEPTH"),
    CRIT            ("CRIT"),
    CRIT_MAGIC      ("ENCHANTED_HIT",   "CRIT_MAGIC"),
    SMOKE_NORMAL    ("SMOKE",           "SMOKE_NORMAL"),
    SMOKE_LARGE     ("LARGE_SMOKE",     "SMOKE_LARGE"),
    SPELL           ("EFFECT",          "SPELL"),
    SPELL_INSTANT   ("INSTANT_EFFECT",  "SPELL_INSTANT"),
    SPELL_MOB       ("ENTITY_EFFECT",   "SPELL_MOB"),
    SPELL_MOB_AMBIENT("ENTITY_EFFECT",  "SPELL_MOB_AMBIENT"),
    SPELL_WITCH     ("WITCH",           "SPELL_WITCH"),
    DRIP_WATER      ("DRIPPING_WATER",  "DRIP_WATER"),
    DRIP_LAVA       ("DRIPPING_LAVA",   "DRIP_LAVA"),
    VILLAGER_ANGRY  ("ANGRY_VILLAGER",  "VILLAGER_ANGRY"),
    VILLAGER_HAPPY  ("HAPPY_VILLAGER",  "VILLAGER_HAPPY"),
    TOWN_AURA       ("MYCELIUM",        "TOWN_AURA"),
    NOTE            ("NOTE"),
    PORTAL          ("PORTAL"),
    ENCHANTMENT_TABLE("ENCHANT",        "ENCHANTMENT_TABLE"),
    FLAME           ("FLAME"),
    LAVA            ("LAVA"),
    FOOTSTEP        ("SCRAPE",          "FOOTSTEP"),
    CLOUD           ("CLOUD"),
    REDSTONE        ("DUST",            "REDSTONE"),
    SNOWBALL        ("ITEM_SNOWBALL",   "SNOWBALL_POOF", "SNOWBALL"),
    SNOW_SHOVEL     ("ITEM_SNOWBALL",   "SNOW_SHOVEL"),
    SLIME           ("ITEM_SLIME",      "SLIME"),
    HEART           ("HEART"),
    BARRIER         ("BLOCK_MARKER",    "BARRIER"),
    ITEM_CRACK      ("ITEM",            "ITEM_CRACK"),
    BLOCK_CRACK     ("BLOCK",           "BLOCK_CRACK"),
    BLOCK_DUST      ("BLOCK",           "BLOCK_DUST"),
    WATER_DROP      ("FALLING_WATER",   "WATER_DROP", "RAIN"),
    ITEM_TAKE       ("POOF",            "ITEM_TAKE"),
    MOB_APPEARANCE  ("ELDER_GUARDIAN",  "MOB_APPEARANCE"),
    DRAGONBREATH    ("DRAGON_BREATH",   "DRAGONBREATH"),
    ENDROD          ("END_ROD",         "ENDROD"),
    DAMAGEINDICATOR ("DAMAGE_INDICATOR","DAMAGEINDICATOR"),
    SWEEPATTACK     ("SWEEP_ATTACK",    "SWEEPATTACK"),
    FALLINGDUST     ("FALLING_DUST",    "FALLINGDUST");

    private final Particle particle;
    private static final Map<String, AdvancedParticleEffect> NAME_MAP = new HashMap<>();
    private static final Map<Integer, AdvancedParticleEffect> ID_MAP = new HashMap<>();

    AdvancedParticleEffect(String... candidates) {
        Particle resolved = null;
        for (String n : candidates) {
            try { resolved = Particle.valueOf(n); break; } catch (IllegalArgumentException ignored) {}
        }
        this.particle = resolved;
    }

    public String getName() { return name().toLowerCase(); }
    public int getId() { return ordinal(); }
    public int getRequiredVersion() { return -1; }
    public boolean hasProperty(ParticleProperty property) {
        // Best-effort property report
        return property == ParticleProperty.DIRECTIONAL;
    }
    public boolean isSupported() { return particle != null; }

    public Particle getParticle() { return particle; }

    public static AdvancedParticleEffect fromName(String name) { return NAME_MAP.get(name == null ? null : name.toLowerCase()); }
    public static AdvancedParticleEffect fromId(int id) { return ID_MAP.get(id); }

    private void emit(Location center, float ox, float oy, float oz, float speed, int amount) {
        if (particle == null || center == null || center.getWorld() == null) return;
        try {
            center.getWorld().spawnParticle(particle, center, amount, ox, oy, oz, speed);
        } catch (Throwable ignored) {}
    }

    // Public display API: kept signatures broadly compatible with the original.
    public void display(float ox, float oy, float oz, float speed, int amount, Location center, double range) { emit(center, ox, oy, oz, speed, amount); }
    public void display(float ox, float oy, float oz, float speed, int amount, Location center, List<Player> players) { emit(center, ox, oy, oz, speed, amount); }
    public void display(float ox, float oy, float oz, float speed, int amount, Location center, Player... players) { display(ox, oy, oz, speed, amount, center, Arrays.asList(players)); }
    public void display(Vector direction, float speed, Location center, double range) { emit(center, (float) direction.getX(), (float) direction.getY(), (float) direction.getZ(), speed, 0); }
    public void display(Vector direction, float speed, Location center, List<Player> players) { emit(center, (float) direction.getX(), (float) direction.getY(), (float) direction.getZ(), speed, 0); }
    public void display(Vector direction, float speed, Location center, Player... players) { display(direction, speed, center, Arrays.asList(players)); }
    public void display(ParticleColor color, Location center, double range) { emit(center, color.getValueX(), color.getValueY(), color.getValueZ(), 1.0f, 0); }
    public void display(ParticleColor color, Location center, List<Player> players) { emit(center, color.getValueX(), color.getValueY(), color.getValueZ(), 1.0f, 0); }
    public void display(ParticleColor color, Location center, Player... players) { display(color, center, Arrays.asList(players)); }
    public void display(ParticleData data, float ox, float oy, float oz, float speed, int amount, Location center, double range) { emit(center, ox, oy, oz, speed, amount); }
    public void display(ParticleData data, float ox, float oy, float oz, float speed, int amount, Location center, List<Player> players) { emit(center, ox, oy, oz, speed, amount); }
    public void display(ParticleData data, float ox, float oy, float oz, float speed, int amount, Location center, Player... players) { display(data, ox, oy, oz, speed, amount, center, Arrays.asList(players)); }
    public void display(ParticleData data, Vector direction, float speed, Location center, double range) { emit(center, (float) direction.getX(), (float) direction.getY(), (float) direction.getZ(), speed, 0); }
    public void display(ParticleData data, Vector direction, float speed, Location center, List<Player> players) { emit(center, (float) direction.getX(), (float) direction.getY(), (float) direction.getZ(), speed, 0); }
    public void display(ParticleData data, Vector direction, float speed, Location center, Player... players) { display(data, direction, speed, center, Arrays.asList(players)); }

    static {
        for (AdvancedParticleEffect e : values()) {
            NAME_MAP.put(e.name().toLowerCase(), e);
            ID_MAP.put(e.ordinal(), e);
        }
    }

    public enum ParticleProperty { REQUIRES_WATER, REQUIRES_DATA, DIRECTIONAL, COLORABLE }

    public abstract static class ParticleData {
        private final Material material;
        private final byte data;
        public ParticleData(Material material, byte data) { this.material = material; this.data = data; }
        public Material getMaterial() { return material; }
        public byte getData() { return data; }
        public int[] getPacketData() { return new int[]{0, data}; }
        public String getPacketDataString() { return "_" + data; }
    }

    public static final class ItemData extends ParticleData {
        public ItemData(Material material, byte data) { super(material, data); }
    }

    public static final class BlockData extends ParticleData {
        public BlockData(Material material, byte data) { super(material, data); }
    }

    public abstract static class ParticleColor {
        public abstract float getValueX();
        public abstract float getValueY();
        public abstract float getValueZ();
    }

    public static final class OrdinaryColor extends ParticleColor {
        private final int red, green, blue;
        public OrdinaryColor(int red, int green, int blue) {
            this.red   = Math.max(0, Math.min(255, red));
            this.green = Math.max(0, Math.min(255, green));
            this.blue  = Math.max(0, Math.min(255, blue));
        }
        public int getRed() { return red; }
        public int getGreen() { return green; }
        public int getBlue() { return blue; }
        @Override public float getValueX() { return red / 255.0f; }
        @Override public float getValueY() { return green / 255.0f; }
        @Override public float getValueZ() { return blue / 255.0f; }
    }

    public static final class NoteColor extends ParticleColor {
        private final int note;
        public NoteColor(int note) { this.note = Math.max(0, Math.min(24, note)); }
        @Override public float getValueX() { return note / 24.0f; }
        @Override public float getValueY() { return 0f; }
        @Override public float getValueZ() { return 0f; }
    }
}
