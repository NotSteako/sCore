package kami.gg.souppvp.kit.valorant;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.KitCategory;
import kami.gg.souppvp.kit.KitRarity;
import kami.gg.souppvp.kit.cosmetic.CosmeticSkin;
import kami.gg.souppvp.kit.cosmetic.SkinApplier;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.timer.Timer;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.DurationFormatter;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.XPBarTimer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PhoenixKit extends Kit implements Listener {

    // ── Tuning constants ──────────────────────────────────────────
    /** How long the fire zone burns (ticks). */
    private static final int    ZONE_DURATION_TICKS   = 100; // 5 seconds @ 20 tps
    /** How often the zone ticks damage/heal (ticks). Every 0.5s like dragon's breath. */
    private static final int    ZONE_TICK_INTERVAL    = 10;
    /** Damage dealt to enemies per tick inside the zone. */
    private static final double ZONE_DAMAGE_PER_TICK  = 1.0; // half a heart — subtle
    /** Health restored to Phoenix per tick while inside his own zone. */
    private static final double ZONE_HEAL_PER_TICK    = 1.0; // half a heart
    /** Radius of the fire zone (blocks). */
    private static final double ZONE_RADIUS           = 2.5;
    /** Cooldown after throwing (seconds). */
    private static final int    ABILITY_COOLDOWN_SECS = 12;
    /** Passive trail interval (ticks). */
    private static final int    TRAIL_INTERVAL_TICKS  = 3;

    // ── Per-player state ──────────────────────────────────────────
    /** Active zone tasks keyed by thrower UUID. */
    private final Map<UUID, BukkitRunnable> zoneTasks  = new HashMap<>();
    /** Passive flame trail tasks. */
    private final Map<UUID, BukkitRunnable> trailTasks = new HashMap<>();
    /** Last-click debounce. */
    private final Map<UUID, Long>           lastClick  = new HashMap<>();

    // ── Kit metadata ──────────────────────────────────────────────
    @Override public String      getName()       { return "Phoenix"; }
    @Override public KitRarity   getRarityType() { return KitRarity.ULTIMATE; }
    @Override public Integer     getPrice()      { return getRarityType().getPrice(); }
    @Override public KitCategory getCategory()   { return KitCategory.VALORANT; }

    // ── Cosmetics ─────────────────────────────────────────────────
    private static final CosmeticSkin DEFAULT = new CosmeticSkin(
            "default",
            "&cDefault Phoenix",
            "213f546310fd4d4e2f978fe49532533da9612de57e302eccc3005679f83db4b",
            Color.fromRGB(220, 60, 10),   // flame orange jacket
            Color.fromRGB(255, 180, 0),   // gold fire trim
            Color.fromRGB(240, 240, 230)  // ash white accent
    );

    @Override public CosmeticSkin getDefaultCosmetic() { return DEFAULT; }

    @Override
    public ItemStack getIcon() {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta  = (SkullMeta) skull.getItemMeta();
        SkinApplier.apply(meta, DEFAULT);
        skull.setItemMeta(meta);
        return skull;
    }

    // ── Description ───────────────────────────────────────────────
    @Override
    public List<String> getDescription() {
        List<String> desc = new ArrayList<>();
        desc.add("&7Right-click &6Blaze Powder &7to throw &cHot Hands&7.");
        desc.add("&7Creates a &cfire zone &7at your feet that burns for &e"
                + (ZONE_DURATION_TICKS / 20) + "s&7.");
        desc.add("&7Enemies inside take &c" + (int)(ZONE_DAMAGE_PER_TICK / 2 * 10) / 10.0
                + " &7damage every &e0.5s&7.");
        desc.add("&7Phoenix standing inside is &ahealed &7instead.");
        desc.add("&7Cooldown: &e" + ABILITY_COOLDOWN_SECS + "s");
        return desc;
    }

    // ── Equipment ─────────────────────────────────────────────────
    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> items = new ArrayList<>();
        items.add(new ItemBuilder(Material.IRON_SWORD)
                .enchantment(Enchantment.SHARPNESS, 1)
                .enchantment(Enchantment.UNBREAKING, 3)
                .build());
        items.add(buildHotHandsItem());
        return items;
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        ItemStack helmet = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta   = (SkullMeta) helmet.getItemMeta();

        CosmeticSkin skin = getDefaultCosmetic();
        if (player != null) {
            Profile profile    = getProfile(player);
            String  selectedId = profile.getSelectedCosmetic(getName());
            for (CosmeticSkin cosmetic : getAvailableCosmetics()) {
                if (cosmetic.getId().equalsIgnoreCase(selectedId)) {
                    skin = cosmetic;
                    break;
                }
            }
        }
        SkinApplier.apply(meta, skin);

        meta.addAttributeModifier(
                Attribute.ARMOR,
                new AttributeModifier(
                        new NamespacedKey(SoupPvP.getInstance(), "phoenix_helmet_armor"),
                        2.0,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HEAD.getGroup()
                )
        );
        helmet.setItemMeta(meta);

        ItemMeta flagMeta = helmet.getItemMeta();
        flagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        helmet.setItemMeta(flagMeta);

        return new ItemStack[]{
                new ItemBuilder(Material.LEATHER_BOOTS)
                        .color(Color.fromRGB(240, 235, 220))
                        .enchantment(Enchantment.PROTECTION, 1)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                new ItemBuilder(Material.LEATHER_LEGGINGS)
                        .color(Color.fromRGB(40, 30, 25))
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE)
                        .color(Color.fromRGB(220, 60, 10))
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                helmet
        };
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        return Collections.emptyList();
    }

    // ── Kit select ────────────────────────────────────────────────
    @Override
    public void onSelect(Player player) {
        startTrailTicker(player);
    }

    // ═══════════════════════════════════════════════════════════════
    //  EVENTS
    // ═══════════════════════════════════════════════════════════════

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
            if (player.isOnline() && isPhoenix(player)) startTrailTicker(player);
        }, 1L);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
            if (player.isOnline() && isPhoenix(player)) startTrailTicker(player);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHotHands(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player    player = event.getPlayer();
        ItemStack held   = player.getInventory().getItemInMainHand();
        if (held.getType() != Material.BLAZE_POWDER) return;

        event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
        event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
        event.setCancelled(true);

        if (!isPhoenix(player)) return;

        Profile profile = getProfile(player);
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;

        UUID uuid = player.getUniqueId();

        // Debounce
        long now = System.currentTimeMillis();
        if (now - lastClick.getOrDefault(uuid, 0L) < 200) return;
        lastClick.put(uuid, now);

        // Cooldown check
        long remaining = SoupPvP.getInstance().getTimersHandler().getRemaining(uuid, "HotHands", true);
        if (remaining > 0) {
            player.sendMessage(CC.translate("&cHot Hands &7is on cooldown for another &c"
                    + DurationFormatter.getRemaining(remaining, true) + "&7."));
            return;
        }

        // Already have an active zone — don't stack
        if (zoneTasks.containsKey(uuid)) {
            player.sendMessage(CC.translate("&cHot Hands &7is already burning!"));
            return;
        }

        throwHotHands(player);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HOT HANDS ZONE
    // ═══════════════════════════════════════════════════════════════

    private void throwHotHands(Player thrower) {
        UUID     uuid   = thrower.getUniqueId();
        // Zone drops at the thrower's feet (like Valorant's ground placement)
        Location centre = thrower.getLocation().clone();
        World    world  = centre.getWorld();
        if (world == null) return;

        // Throw sound + small activation burst
        world.playSound(centre, Sound.ITEM_FIRECHARGE_USE,  0.8f, 1.1f);
        world.playSound(centre, Sound.ENTITY_BLAZE_SHOOT,   0.5f, 1.4f);
        spawnThrowBurst(centre);

        thrower.sendMessage(CC.translate("&cHot Hands &7— &6Fire zone ignited!"));

        final int[] ticks = {0};

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                ticks[0]++;

                // ── Ambient zone visuals every tick ──
                spawnZoneAmbient(centre, ticks[0]);

                // ── Damage / heal every ZONE_TICK_INTERVAL ticks ──
                if (ticks[0] % ZONE_TICK_INTERVAL == 0) {
                    tickZoneEffects(thrower, centre, uuid);
                }

                // ── Zone expired ──
                if (ticks[0] >= ZONE_DURATION_TICKS) {
                    spawnExtinguishBurst(centre);
                    world.playSound(centre, Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 1.0f);
                    zoneTasks.remove(uuid);
                    cancel();
                }
            }
        };

        zoneTasks.put(uuid, task);
        task.runTaskTimer(SoupPvP.getInstance(), 0L, 1L);

        // Set cooldown
        SoupPvP.getInstance().getTimersHandler().addPlayerTimer(
                uuid,
                new Timer("HotHands", TimeUnit.SECONDS.toMillis(ABILITY_COOLDOWN_SECS)),
                true);
        XPBarTimer.runXpBar(thrower, ABILITY_COOLDOWN_SECS);
    }

    /**
     * Called every ZONE_TICK_INTERVAL ticks — scans players inside the radius.
     * Enemies get damaged; Phoenix himself gets healed.
     */
    private void tickZoneEffects(Player thrower, Location centre, UUID throwerUUID) {
        if (!thrower.isOnline()) return;

        for (Player nearby : centre.getWorld().getPlayers()) {
            if (nearby.isDead()) continue;
            if (nearby.getLocation().distanceSquared(centre) > ZONE_RADIUS * ZONE_RADIUS) continue;

            Profile nearbyProfile = getProfile(nearby);
            if (nearbyProfile.getProfileState() == ProfileState.SPAWN) continue;
            if (nearbyProfile.isInEvent()) continue;

            if (nearby.getUniqueId().equals(throwerUUID)) {
                // ── Heal Phoenix ──
                double newHp = Math.min(nearby.getHealth() + ZONE_HEAL_PER_TICK, nearby.getMaxHealth());
                nearby.setHealth(newHp);
                spawnHealTick(nearby.getLocation().clone().add(0, 1, 0));

            } else {
                // ── Damage enemies ──
                nearby.damage(ZONE_DAMAGE_PER_TICK, thrower);
                nearby.setFireTicks(60);
                spawnDamageTick(nearby.getLocation().clone().add(0, 1, 0));
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  PASSIVE TRAIL TICKER
    // ═══════════════════════════════════════════════════════════════

    private void startTrailTicker(Player player) {
        UUID uuid = player.getUniqueId();
        stopTrailTicker(uuid);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isPhoenix(player)) {
                    stopTrailTicker(uuid);
                    cancel();
                    return;
                }
                Profile profile = getProfile(player);
                if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;
                spawnPassiveTrail(player);
            }
        };

        trailTasks.put(uuid, task);
        task.runTaskTimer(SoupPvP.getInstance(), 0L, TRAIL_INTERVAL_TICKS);
    }

    private void stopTrailTicker(UUID uuid) {
        BukkitRunnable existing = trailTasks.remove(uuid);
        if (existing != null) { try { existing.cancel(); } catch (IllegalStateException ignored) {} }
    }

    // ═══════════════════════════════════════════════════════════════
    //  PARTICLES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Passive idle ember trail at foot level — two small red dust dots
     * behind the player with occasional bright flicker.
     */
    private void spawnPassiveTrail(Player player) {
        Location base  = player.getLocation().clone().add(0, 0.1, 0);
        World    world = base.getWorld();
        if (world == null) return;

        double yaw = Math.toRadians(base.getYaw() + 90);
        double ox  = Math.cos(yaw) * 0.15;
        double oz  = Math.sin(yaw) * 0.15;

        // Deep red embers
        Particle.DustOptions ember  = new Particle.DustOptions(Color.fromRGB(200, 20, 10),  0.85f);
        Particle.DustOptions flick  = new Particle.DustOptions(Color.fromRGB(255, 80, 20),  0.7f);

        world.spawnParticle(Particle.DUST, base.clone().add( ox, 0,  oz), 1, 0, 0, 0, 0, ember);
        world.spawnParticle(Particle.DUST, base.clone().add(-ox, 0, -oz), 1, 0, 0, 0, 0, ember);
        world.spawnParticle(Particle.FLAME, base, 1, 0.04, 0.0, 0.04, 0.01);

        if (Math.random() < 0.3) {
            world.spawnParticle(Particle.DUST, base.clone().add(0, 0.45, 0), 1, 0.04, 0.04, 0.04, 0, flick);
        }
    }

    /**
     * Ambient zone visuals — called every tick while the fire zone burns.
     * Fills the radius with swirling red dust + rising FLAME particles
     * so the zone boundary is visible like dragon's breath cloud.
     */
    private void spawnZoneAmbient(Location centre, int tick) {
        World world = centre.getWorld();
        if (world == null) return;

        // ── Perimeter ring of red dust (spins over time) ──
        double offset = tick * 0.15;
        int    steps  = 16;
        double step   = (2 * Math.PI) / steps;

        Particle.DustOptions rim    = new Particle.DustOptions(Color.fromRGB(210, 15, 10),  0.9f);
        Particle.DustOptions inner  = new Particle.DustOptions(Color.fromRGB(255, 50, 20),  0.75f);
        Particle.DustOptions core   = new Particle.DustOptions(Color.fromRGB(255, 100, 30), 0.65f);

        for (int i = 0; i < steps; i++) {
            double angle = step * i + offset;

            // Outer perimeter ring — deep red
            double x = ZONE_RADIUS * Math.cos(angle);
            double z = ZONE_RADIUS * Math.sin(angle);
            world.spawnParticle(Particle.DUST,
                    centre.clone().add(x, 0.05, z), 1, 0, 0, 0, 0, rim);

            // Counter-rotating inner ring — brighter orange-red
            double angle2 = step * i - offset * 1.3;
            double x2     = (ZONE_RADIUS * 0.55) * Math.cos(angle2);
            double z2     = (ZONE_RADIUS * 0.55) * Math.sin(angle2);
            world.spawnParticle(Particle.DUST,
                    centre.clone().add(x2, 0.05, z2), 1, 0, 0, 0, 0, inner);
        }

        // ── Random FLAME particles rising inside the zone ──
        // Fires on every other tick to avoid overkill
        if (tick % 2 == 0) {
            for (int i = 0; i < 3; i++) {
                double rx = (Math.random() * 2 - 1) * ZONE_RADIUS * 0.85;
                double rz = (Math.random() * 2 - 1) * ZONE_RADIUS * 0.85;
                // Keep within circle
                if (rx * rx + rz * rz <= ZONE_RADIUS * ZONE_RADIUS) {
                    world.spawnParticle(Particle.FLAME,
                            centre.clone().add(rx, 0.1, rz), 1, 0.05, 0.0, 0.05, 0.02);
                }
            }
        }

        // ── Central core glow — bright red dust cluster ──
        if (tick % 5 == 0) {
            world.spawnParticle(Particle.DUST, centre.clone().add(0, 0.15, 0),
                    3, 0.2, 0.05, 0.2, 0, core);
        }
    }

    /**
     * Small red burst on the player when healed by their own zone.
     */
    private void spawnHealTick(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Particle.DustOptions healRed = new Particle.DustOptions(Color.fromRGB(255, 60, 20), 1.0f);
        world.spawnParticle(Particle.DUST,    loc, 4, 0.2, 0.2, 0.2, 0, healRed);
        world.spawnParticle(Particle.FLAME,   loc, 2, 0.1, 0.1, 0.1, 0.03);
    }

    /**
     * Small dark-red burst on an enemy when they take zone damage.
     * Subtle — like dragon's breath not an explosion.
     */
    private void spawnDamageTick(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Particle.DustOptions dmgRed = new Particle.DustOptions(Color.fromRGB(180, 10, 5), 0.9f);
        world.spawnParticle(Particle.DUST,  loc, 3, 0.15, 0.15, 0.15, 0, dmgRed);
        world.spawnParticle(Particle.FLAME, loc, 1, 0.08, 0.08, 0.08, 0.02);
    }

    /**
     * Activation burst when the blaze powder hits the ground.
     */
    private void spawnThrowBurst(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Location centre = loc.clone().add(0, 0.5, 0);
        Particle.DustOptions burst  = new Particle.DustOptions(Color.fromRGB(220, 25, 10),  1.4f);
        Particle.DustOptions bright = new Particle.DustOptions(Color.fromRGB(255, 80, 30),  1.1f);

        world.spawnParticle(Particle.DUST,  centre, 14, 0.4, 0.3, 0.4, 0, burst);
        world.spawnParticle(Particle.DUST,  centre,  8, 0.2, 0.2, 0.2, 0, bright);
        world.spawnParticle(Particle.FLAME, centre,  8, 0.4, 0.3, 0.4, 0.08);
        world.spawnParticle(Particle.FLASH, centre,  1, 0, 0, 0, 0);
    }

    /**
     * Gentle extinguish burst when the zone expires.
     */
    private void spawnExtinguishBurst(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Location centre = loc.clone().add(0, 0.3, 0);
        Particle.DustOptions fade = new Particle.DustOptions(Color.fromRGB(150, 15, 5), 1.0f);

        world.spawnParticle(Particle.DUST,    centre,  8, 0.5, 0.2, 0.5, 0, fade);
        world.spawnParticle(Particle.END_ROD, centre,  3, 0.3, 0.2, 0.3, 0.04);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════

    private ItemStack buildHotHandsItem() {
        return new ItemBuilder(Material.BLAZE_POWDER)
                .name(CC.translate("&cHot Hands &7[Right-Click]"))
                .build();
    }

    private boolean isPhoenix(Player player) {
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Phoenix");
        return getProfile(player).getCurrentKit().equals(kit);
    }

    private Profile getProfile(Player player) {
        return SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
    }
}