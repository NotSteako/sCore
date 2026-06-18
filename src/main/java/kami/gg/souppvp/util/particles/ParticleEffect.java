package kami.gg.souppvp.util.particles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 1.21.8 port of the original 1.8 ParticleEffect helper.
 * Original was full-NMS reflection. This version maps the legacy names to
 * modern {@link org.bukkit.Particle} values and uses the public Bukkit API.
 */
public enum ParticleEffect {
    HUGE_EXPLOSION,
    LARGE_EXPLODE,
    BUBBLE,
    SUSPEND,
    DEPTH_SUSPEND,
    MAGIC_CRIT,
    MOB_SPELL,
    MOB_SPELL_AMBIENT,
    INSTANT_SPELL,
    WITCH_MAGIC,
    EXPLODE,
    SPLASH,
    LARGE_SMOKE,
    RED_DUST,
    SNOWBALL_POOF,
    ANGRY_VILLAGER,
    HAPPY_VILLAGER,
    EXPLOSION_NORMAL,
    EXPLOSION_LARGE,
    EXPLOSION_HUGE,
    FIREWORKS_SPARK,
    WATER_BUBBLE,
    WATER_SPLASH,
    WATER_WAKE,
    SUSPENDED,
    SUSPENDED_DEPTH,
    CRIT,
    CRIT_MAGIC,
    SMOKE_NORMAL,
    SMOKE_LARGE,
    SPELL,
    SPELL_INSTANT,
    SPELL_MOB,
    SPELL_MOB_AMBIENT,
    SPELL_WITCH,
    DRIP_WATER,
    DRIP_LAVA,
    VILLAGER_ANGRY,
    VILLAGER_HAPPY,
    TOWN_AURA,
    NOTE,
    PORTAL,
    ENCHANTMENT_TABLE,
    FLAME,
    LAVA,
    FOOTSTEP,
    CLOUD,
    REDSTONE,
    SNOWBALL,
    SNOW_SHOVEL,
    SLIME,
    HEART,
    BARRIER,
    ITEM_CRACK,
    BLOCK_CRACK,
    BLOCK_DUST,
    WATER_DROP,
    ITEM_TAKE,
    MOB_APPEARANCE;

    private static int particleRange = 25;
    private static final Map<ParticleEffect, Particle> MAPPING = new HashMap<>();

    static {
        put(EXPLODE,          "POOF",            "EXPLOSION_NORMAL", "EXPLOSION");
        put(EXPLOSION_NORMAL, "POOF",            "EXPLOSION_NORMAL", "EXPLOSION");
        put(LARGE_EXPLODE,    "EXPLOSION",       "EXPLOSION_LARGE");
        put(EXPLOSION_LARGE,  "EXPLOSION",       "EXPLOSION_LARGE");
        put(HUGE_EXPLOSION,   "EXPLOSION_EMITTER","EXPLOSION_HUGE");
        put(EXPLOSION_HUGE,   "EXPLOSION_EMITTER","EXPLOSION_HUGE");
        put(BUBBLE,           "BUBBLE",          "WATER_BUBBLE");
        put(WATER_BUBBLE,     "BUBBLE",          "WATER_BUBBLE");
        put(SPLASH,           "SPLASH",          "WATER_SPLASH");
        put(WATER_SPLASH,     "SPLASH",          "WATER_SPLASH");
        put(WATER_WAKE,       "FISHING",         "WATER_WAKE");
        put(SUSPEND,          "UNDERWATER",      "SUSPENDED");
        put(SUSPENDED,        "UNDERWATER",      "SUSPENDED");
        put(DEPTH_SUSPEND,    "UNDERWATER",      "SUSPENDED_DEPTH");
        put(SUSPENDED_DEPTH,  "UNDERWATER",      "SUSPENDED_DEPTH");
        put(CRIT,             "CRIT");
        put(MAGIC_CRIT,       "ENCHANTED_HIT",   "CRIT_MAGIC");
        put(CRIT_MAGIC,       "ENCHANTED_HIT",   "CRIT_MAGIC");
        put(SMOKE_NORMAL,     "SMOKE",           "SMOKE_NORMAL");
        put(SMOKE_LARGE,      "LARGE_SMOKE",     "SMOKE_LARGE");
        put(LARGE_SMOKE,      "LARGE_SMOKE",     "SMOKE_LARGE");
        put(SPELL,            "EFFECT",          "SPELL");
        put(SPELL_INSTANT,    "INSTANT_EFFECT",  "SPELL_INSTANT");
        put(INSTANT_SPELL,    "INSTANT_EFFECT",  "SPELL_INSTANT");
        put(SPELL_MOB,        "ENTITY_EFFECT",   "SPELL_MOB");
        put(MOB_SPELL,        "ENTITY_EFFECT",   "SPELL_MOB");
        put(SPELL_MOB_AMBIENT,"ENTITY_EFFECT",   "SPELL_MOB_AMBIENT");
        put(MOB_SPELL_AMBIENT,"ENTITY_EFFECT",   "SPELL_MOB_AMBIENT");
        put(SPELL_WITCH,      "WITCH",           "SPELL_WITCH");
        put(WITCH_MAGIC,      "WITCH",           "SPELL_WITCH");
        put(DRIP_WATER,       "DRIPPING_WATER",  "DRIP_WATER");
        put(DRIP_LAVA,        "DRIPPING_LAVA",   "DRIP_LAVA");
        put(VILLAGER_ANGRY,   "ANGRY_VILLAGER",  "VILLAGER_ANGRY");
        put(ANGRY_VILLAGER,   "ANGRY_VILLAGER",  "VILLAGER_ANGRY");
        put(VILLAGER_HAPPY,   "HAPPY_VILLAGER",  "VILLAGER_HAPPY");
        put(HAPPY_VILLAGER,   "HAPPY_VILLAGER",  "VILLAGER_HAPPY");
        put(TOWN_AURA,        "MYCELIUM",        "TOWN_AURA");
        put(NOTE,             "NOTE");
        put(PORTAL,           "PORTAL");
        put(ENCHANTMENT_TABLE,"ENCHANT",         "ENCHANTMENT_TABLE");
        put(FLAME,            "FLAME");
        put(LAVA,             "LAVA");
        put(CLOUD,            "CLOUD");
        put(REDSTONE,         "DUST",            "REDSTONE");
        put(RED_DUST,         "DUST",            "REDSTONE");
        put(SNOWBALL,         "ITEM_SNOWBALL",   "SNOWBALL_POOF", "SNOWBALL");
        put(SNOWBALL_POOF,    "ITEM_SNOWBALL",   "SNOWBALL_POOF", "SNOWBALL");
        put(SNOW_SHOVEL,      "ITEM_SNOWBALL",   "SNOW_SHOVEL");
        put(SLIME,            "ITEM_SLIME",      "SLIME");
        put(HEART,             "HEART");
        put(FIREWORKS_SPARK,  "FIREWORK",        "FIREWORKS_SPARK");
        put(BARRIER,          "BLOCK_MARKER",    "BARRIER");
        put(ITEM_CRACK,       "ITEM",            "ITEM_CRACK");
        put(BLOCK_CRACK,      "BLOCK",           "BLOCK_CRACK");
        put(BLOCK_DUST,       "BLOCK",           "BLOCK_DUST");
        put(WATER_DROP,       "FALLING_WATER",   "WATER_DROP", "RAIN");
        put(ITEM_TAKE,        "POOF",            "ITEM_TAKE");
        put(MOB_APPEARANCE,   "ELDER_GUARDIAN",  "MOB_APPEARANCE");
        put(FOOTSTEP,         "SCRAPE",          "FOOTSTEP");
    }

    private static void put(ParticleEffect eff, String... candidates) {
        for (String name : candidates) {
            try {
                Particle p = Particle.valueOf(name);
                MAPPING.put(eff, p);
                return;
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public String getName() {
        return this.name();
    }

    public static void setRange(int range) {
        if (range < 0) throw new IllegalArgumentException("Range must be positive!");
        particleRange = range;
    }

    public static int getRange() {
        return particleRange;
    }

    public void sendToPlayer(Player player, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count) {
        if (!isPlayerInRange(player, location)) return;
        Particle particle = MAPPING.get(this);
        if (particle == null) return;
        try {
            player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
        } catch (Throwable ignored) {
            // Some particles require specific data types (BLOCK, ITEM, DUST). Silently ignore.
        }
    }

    public void sendToPlayers(Collection<? extends Player> collection, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count) {
        for (Player p : collection) sendToPlayer(p, location, offsetX, offsetY, offsetZ, speed, count);
    }

    public void sendToPlayers(Player[] players, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count) {
        sendToPlayers(Arrays.asList(players), location, offsetX, offsetY, offsetZ, speed, count);
    }

    private static boolean isPlayerInRange(Player p, Location center) {
        if (!p.getWorld().equals(center.getWorld())) return false;
        return p.getLocation().distance(center) < particleRange;
    }
}
