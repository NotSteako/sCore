package kami.gg.souppvp.kit.marvel;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.KitCategory;
import kami.gg.souppvp.kit.KitRarity;
import kami.gg.souppvp.kit.cosmetic.CosmeticSkin;
import kami.gg.souppvp.kit.cosmetic.SkinApplier;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.timer.Timer;
import kami.gg.souppvp.util.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class StarlordKit extends Kit implements Listener {

    // ── Tuning constants ──────────────────────────────────────────
    /** How long Galactic Legend lasts (ticks). Matches Rivals' 8s duration. */
    private static final int    ULTIMATE_DURATION_TICKS  = 160; // 8 seconds @ 20 tps
    /** Cooldown after the ultimate expires. */
    private static final int    ULTIMATE_COOLDOWN_SECONDS = 25;
    /** How many shots fire during the ultimate (spread over the duration). */
    private static final int    SHOT_COUNT               = 12;
    /** Ticks between each auto-shot burst during the ultimate. */
    private static final int    SHOT_INTERVAL_TICKS      = ULTIMATE_DURATION_TICKS / SHOT_COUNT;
    /** Damage per projectile hit (simulated via direct damage). */
    private static final double SHOT_DAMAGE              = 4.0;  // ~6.5 per round in Rivals, tuned for soup
    /** Radius to scan for nearby enemies to lock onto. */
    private static final double LOCK_ON_RADIUS           = 8.0;
    /** Flight speed boost strength during the ultimate (Speed effect level). */
    private static final int    FLIGHT_SPEED_LEVEL       = 3;
    /** Passive trail interval ticks. */
    private static final int    TRAIL_INTERVAL_TICKS     = 3;

    // ── Per-player state ──────────────────────────────────────────
    /** Players currently inside their Galactic Legend window. */
    private final Set<UUID>                     ultimateActive = new HashSet<>();
    /** Set just before programmatic damage so the melee listener knows not to cancel it. */
    private final Set<UUID>                     firingShot     = new HashSet<>();
    /** The main ultimate task per player. */
    private final Map<UUID, BukkitRunnable>     ultimateTasks  = new HashMap<>();
    /** Passive trail task. */
    private final Map<UUID, BukkitRunnable>     trailTasks     = new HashMap<>();
    /** Last-click debounce. */
    private final Map<UUID, Long>               lastClick      = new HashMap<>();

    // ── Kit metadata ──────────────────────────────────────────────
    @Override public String      getName()       { return "Star-Lord"; }
    @Override public KitRarity   getRarityType() { return KitRarity.ULTIMATE; }
    @Override public Integer     getPrice()      { return getRarityType().getPrice(); }
    @Override public KitCategory getCategory()   { return KitCategory.MARVEL; }

    // ── Cosmetics ─────────────────────────────────────────────────

    private static final CosmeticSkin DEFAULT = new CosmeticSkin(
            "default",
            "&6Default Star-Lord",
            // Star-Lord / Peter Quill skin texture hash
            "38663a77ff88165dde37f351225baf4f7e9d43e84d3d1d6f9cede71141331d",
            Color.fromRGB(110, 55, 30),   // dark russet jacket
            Color.fromRGB(190, 140, 60),  // gold visor trim
            Color.fromRGB(210, 85, 30)    // burnt-orange accent
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
        desc.add("&7Right-click &5Blaze Rod &7to activate &eGalactic Legend&7.");
        desc.add("&7Engage in free-flight and &clock onto &7nearby enemies,");
        desc.add("&7firing rapid &6Element Gun &7bursts at them automatically.");
        desc.add("&7Duration: &e" + (ULTIMATE_DURATION_TICKS / 20) + "s");
        desc.add("&7Cooldown: &e" + ULTIMATE_COOLDOWN_SECONDS + "s");
        return desc;
    }

    // ── Equipment ─────────────────────────────────────────────────
    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> items = new ArrayList<>();
        // Primary melee — Star-Lord punches when out of ammo
        items.add(new ItemBuilder(Material.IRON_SWORD)
                .enchantment(Enchantment.SHARPNESS, 1)
                .enchantment(Enchantment.UNBREAKING, 3)
                .build());
        // Galactic Legend activator
        items.add(buildGalacticLegendItem());
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

        // Star-Lord wears medium armour — not a tank, not glass-cannon
        meta.addAttributeModifier(
                Attribute.ARMOR,
                new AttributeModifier(
                        new NamespacedKey(SoupPvP.getInstance(), "starlord_helmet_armor"),
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
                // Boots — Star-Lord's brown leather boots
                new ItemBuilder(Material.LEATHER_BOOTS)
                        .color(Color.fromRGB(90, 50, 20))
                        .enchantment(Enchantment.PROTECTION, 1)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                // Leggings — dark trousers
                new ItemBuilder(Material.LEATHER_LEGGINGS)
                        .color(Color.fromRGB(55, 35, 20))
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                // Chestplate — iconic red-brown Ravager jacket
                new ItemBuilder(Material.LEATHER_CHESTPLATE)
                        .color(Color.fromRGB(140, 50, 25))
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                helmet
        };
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        // Standard mobility — no passive speed, the ultimate provides the burst
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
            if (player.isOnline() && isStarLord(player)) {
                startTrailTicker(player);
            }
        }, 1L);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
            if (player.isOnline() && isStarLord(player)) {
                startTrailTicker(player);
            }
        }, 1L);
    }

    /**
     * Prevents normal melee damage while ultimate is active — Star-Lord is
     * airborne firing projectiles, not punching people.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMeleeDuringUltimate(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        // Allow our programmed lock-on shots
        if (firingShot.contains(attacker.getUniqueId())) {
            return;
        }

        // Block actual melee attacks while Galactic Legend is active
        if (ultimateActive.contains(attacker.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onGalacticLegend(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player    player = event.getPlayer();
        ItemStack held   = player.getInventory().getItemInMainHand();
        if (held.getType() != Material.BLAZE_ROD) return;

        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setCancelled(true);

        if (!isStarLord(player)) return;

        Profile profile = getProfile(player);
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;

        UUID uuid = player.getUniqueId();

        // Debounce
        long now = System.currentTimeMillis();
        if (now - lastClick.getOrDefault(uuid, 0L) < 200) return;
        lastClick.put(uuid, now);

        // Already active
        if (ultimateActive.contains(uuid)) {
            player.sendMessage(CC.translate("&eGalactic Legend &7is already active!"));
            return;
        }

        // Cooldown check
        if (SoupPvP.getInstance().getTimersHandler().getRemaining(uuid, "GalacticLegend", true) > 0) {
            player.sendMessage(CC.translate("&eGalactic Legend &7is on cooldown for another &e"
                    + DurationFormatter.getRemaining(
                    SoupPvP.getInstance().getTimersHandler().getRemaining(uuid, "GalacticLegend", true), true)
                    + "&7."));
            return;
        }

        activateGalacticLegend(player);
    }

    // ═══════════════════════════════════════════════════════════════
    //  GALACTIC LEGEND ACTIVATION
    // ═══════════════════════════════════════════════════════════════

    private void activateGalacticLegend(Player player) {
        UUID uuid = player.getUniqueId();

        ultimateActive.add(uuid);

        // ── Visual + sound: activation burst ──
        spawnActivationBurst(player.getLocation());
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER,   0.3f, 2.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,   0.7f, 1.2f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE,   0.5f, 0.5f);

        // ── Flight boost: Speed + Jump ──

        player.sendMessage(CC.translate("&eGalactic Legend &7— &bFree-flight engaged! Locking on..."));
        player.setAllowFlight(true);
        player.setFlying(true);
        TasksUtility.runTaskLater(() -> {
            player.setAllowFlight(false);
            player.setFlying(false);
        }, 5 * 20);

        final int[] ticks     = {0};
        final int[] shotsFired = {0};

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isStarLord(player)) {
                    endUltimate(player, uuid);
                    cancel();
                    return;
                }

                ticks[0]++;

                // ── Continuous ambient thruster trail while airborne ──
                spawnThrusterTrail(player);

                // ── Spinning lock-on ring every tick to signal active state ──
                spawnLockOnRing(player.getLocation().clone().add(0, 1.0, 0), ticks[0]);

                // ── Lock-on shots at SHOT_INTERVAL_TICKS ──
                if (ticks[0] % SHOT_INTERVAL_TICKS == 0 && shotsFired[0] < SHOT_COUNT) {
                    fireLockedShot(player);
                    shotsFired[0]++;
                }

                // ── Duration expired ──
                if (ticks[0] >= ULTIMATE_DURATION_TICKS) {
                    endUltimate(player, uuid);
                    cancel();
                }
            }
        };

        ultimateTasks.put(uuid, task);
        task.runTaskTimer(SoupPvP.getInstance(), 0L, 1L);

        // ── Cooldown starts immediately (can't re-activate mid-flight) ──
        SoupPvP.getInstance().getTimersHandler().addPlayerTimer(
                uuid,
                new Timer("GalacticLegend", TimeUnit.SECONDS.toMillis(ULTIMATE_COOLDOWN_SECONDS)),
                true);
        XPBarTimer.runXpBar(player, ULTIMATE_COOLDOWN_SECONDS);
    }

    /**
     * Finds the nearest enemy within LOCK_ON_RADIUS and fires a simulated
     * Element Gun projectile at them — direct damage + visual tracer.
     */
    private void fireLockedShot(Player shooter) {
        Location origin = shooter.getLocation().clone().add(0, 1.0, 0);
        World    world  = origin.getWorld();
        if (world == null) return;

        // ── Scan for nearest enemy ──
        Player target = findNearestEnemy(shooter);

        if (target != null) {

            shooter.getWorld().playSound(
                    shooter.getLocation(),
                    Sound.ENTITY_ARROW_HIT_PLAYER,
                    0.4f,
                    1.6f
            );

            spawnProjectileTracer(
                    origin,
                    target.getLocation().clone().add(0, 1.0, 0)
            );

            // Damage that bypasses the melee cancellation
            firingShot.add(shooter.getUniqueId());

            try {
                target.damage(SHOT_DAMAGE, shooter);
            } finally {
                firingShot.remove(shooter.getUniqueId());
            }

            spawnHitBurst(
                    target.getLocation().clone().add(0, 1.0, 0)
            );
        } else {
            // No target — fire a wild shot forward with a "missed" sound
            shooter.getWorld().playSound(shooter.getLocation(),
                    Sound.ENTITY_ARROW_SHOOT, 0.3f, 1.4f);
            spawnWildShot(origin, shooter.getLocation().getDirection());
        }
    }

    /**
     * Returns the nearest online, living, non-self player within LOCK_ON_RADIUS
     * who is in a fightable state (not in spawn / event).
     */
    private Player findNearestEnemy(Player shooter) {
        Player nearest     = null;
        double nearestDist = Double.MAX_VALUE;

        for (Player other : shooter.getWorld().getPlayers()) {
            if (other.equals(shooter)) continue;
            if (!other.isOnline() || other.isDead()) continue;

            Profile otherProfile = getProfile(other);
            if (otherProfile.getProfileState() == ProfileState.SPAWN) continue;
            if (otherProfile.isInEvent()) continue;

            double dist = shooter.getLocation().distanceSquared(other.getLocation());
            if (dist <= LOCK_ON_RADIUS * LOCK_ON_RADIUS && dist < nearestDist) {
                nearestDist = dist;
                nearest     = other;
            }
        }

        return nearest;
    }

    private void endUltimate(Player player, UUID uuid) {
        ultimateActive.remove(uuid);
        BukkitRunnable existing = ultimateTasks.remove(uuid);
        if (existing != null) try { existing.cancel(); } catch (IllegalStateException ignored) {}

        if (!player.isOnline()) return;

        // Remove flight potions
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);

        // Expiry burst + sound
        spawnExpiryBurst(player.getLocation());
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.6f, 0.8f);

        player.sendMessage(CC.translate("&eGalactic Legend &7has ended."));
    }

    // ═══════════════════════════════════════════════════════════════
    //  PASSIVE TRAIL TICKER
    // ═══════════════════════════════════════════════════════════════

    /**
     * Soft cosmic dust trail — idle passive effect while NOT in ultimate.
     * Distinguishes Star-Lord from other kits at a glance.
     */
    private void startTrailTicker(Player player) {
        UUID uuid = player.getUniqueId();
        stopTrailTicker(uuid);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isStarLord(player)) {
                    stopTrailTicker(uuid);
                    cancel();
                    return;
                }
                Profile profile = getProfile(player);
                if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;

                // During ultimate the thruster trail handles visuals — skip passive here
                if (ultimateActive.contains(uuid)) return;

                spawnPassiveTrail(player);
            }
        };

        trailTasks.put(uuid, task);
        task.runTaskTimer(SoupPvP.getInstance(), 0L, TRAIL_INTERVAL_TICKS);
    }

    private void stopTrailTicker(UUID uuid) {
        BukkitRunnable existing = trailTasks.remove(uuid);
        if (existing != null) try { existing.cancel(); } catch (IllegalStateException ignored) {}
    }

    // ═══════════════════════════════════════════════════════════════
    //  PARTICLES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Passive idle trail — subtle cosmic red-orange dust at foot level,
     * a nod to Star-Lord's Quad Blasters' exhaust vents.
     */
    private void spawnPassiveTrail(Player player) {
        Location base  = player.getLocation().clone().add(0, 0.1, 0);
        World    world = base.getWorld();
        if (world == null) return;

        double yaw = Math.toRadians(base.getYaw() + 90);
        double ox  = Math.cos(yaw) * 0.18;
        double oz  = Math.sin(yaw) * 0.18;

        Particle.DustOptions rustRed = new Particle.DustOptions(Color.fromRGB(210, 70, 20), 0.9f);

        world.spawnParticle(Particle.DUST, base.clone().add( ox, 0,  oz), 1, 0, 0, 0, 0, rustRed);
        world.spawnParticle(Particle.DUST, base.clone().add(-ox, 0, -oz), 1, 0, 0, 0, 0, rustRed);

        // Occasional gold cosmic sparkle
        if (Math.random() < 0.25) {
            Particle.DustOptions gold = new Particle.DustOptions(Color.fromRGB(255, 190, 30), 0.7f);
            world.spawnParticle(Particle.DUST, base.clone().add(0, 0.4, 0), 1, 0.05, 0.05, 0.05, 0, gold);
        }
    }

    /**
     * Thruster trail during Galactic Legend — dual rocket exhaust jets
     * pointing opposite to the player's movement direction.
     */
    private void spawnThrusterTrail(Player player) {
        Location base  = player.getLocation().clone().add(0, 0.3, 0);
        World    world = base.getWorld();
        if (world == null) return;

        double yaw = Math.toRadians(base.getYaw() + 90);
        double ox  = Math.cos(yaw) * 0.25;
        double oz  = Math.sin(yaw) * 0.25;

        // Bright orange-white thrust
        Particle.DustOptions thrust  = new Particle.DustOptions(Color.fromRGB(255, 160, 40), 1.2f);
        Particle.DustOptions exhaust = new Particle.DustOptions(Color.fromRGB(255, 240, 180), 0.8f);

        world.spawnParticle(Particle.DUST, base.clone().add( ox, 0,  oz), 2, 0.02, 0.05, 0.02, 0, thrust);
        world.spawnParticle(Particle.DUST, base.clone().add(-ox, 0, -oz), 2, 0.02, 0.05, 0.02, 0, thrust);
        world.spawnParticle(Particle.DUST, base.clone().add( ox, 0,  oz), 1, 0, 0, 0, 0, exhaust);
        world.spawnParticle(Particle.DUST, base.clone().add(-ox, 0, -oz), 1, 0, 0, 0, 0, exhaust);

        // END_ROD for rocket spark effect
        if (Math.random() < 0.4) {
            world.spawnParticle(Particle.END_ROD, base.clone().add(0, 0.2, 0), 1, 0.08, 0.08, 0.08, 0.01);
        }
    }

    /**
     * Spinning gold ring around the player during Galactic Legend to show
     * the lock-on targeting system is active — mirrors Marvel Rivals' UI targeting reticle.
     */
    private static final int    RING_PARTICLES = 20;
    private static final double RING_RADIUS    = 0.7;

    private void spawnLockOnRing(Location centre, int tick) {
        World world = centre.getWorld();
        if (world == null) return;

        double offset = tick * 0.25;
        double step   = (2 * Math.PI) / RING_PARTICLES;

        // Outer ring: gold
        Particle.DustOptions gold   = new Particle.DustOptions(Color.fromRGB(255, 200, 30), 0.75f);
        // Inner ring (counter-rotating): bright orange
        Particle.DustOptions orange = new Particle.DustOptions(Color.fromRGB(255, 100, 20), 0.6f);

        for (int i = 0; i < RING_PARTICLES; i++) {
            double angle = step * i + offset;
            double x     = RING_RADIUS * Math.cos(angle);
            double z     = RING_RADIUS * Math.sin(angle);
            world.spawnParticle(Particle.DUST, centre.clone().add(x, 0, z), 1, 0, 0, 0, 0, gold);

            // Counter-rotate a smaller inner ring
            double angle2 = step * i - offset * 1.5;
            double x2     = (RING_RADIUS * 0.55) * Math.cos(angle2);
            double z2     = (RING_RADIUS * 0.55) * Math.sin(angle2);
            world.spawnParticle(Particle.DUST, centre.clone().add(x2, 0, z2), 1, 0, 0, 0, 0, orange);
        }
    }

    /**
     * Line of gold particles from shooter to target simulating the rapid-fire
     * Element Gun projectile tracer.
     */
    private void spawnProjectileTracer(Location from, Location to) {
        World world = from.getWorld();
        if (world == null) return;

        Vector dir    = to.toVector().subtract(from.toVector());
        double length = dir.length();
        if (length == 0) return;

        dir.normalize();
        Particle.DustOptions shot = new Particle.DustOptions(Color.fromRGB(255, 230, 80), 0.7f);

        // Step along the path placing particles every 0.5 blocks
        for (double d = 0; d < length; d += 0.45) {
            Location point = from.clone().add(dir.clone().multiply(d));
            world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, shot);
        }
    }

    /**
     * Burst on a lock-on hit — small white flash + orange scatter, like a blaster impact.
     */
    private void spawnHitBurst(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Particle.DustOptions impact = new Particle.DustOptions(Color.fromRGB(255, 220, 60), 1.0f);
        world.spawnParticle(Particle.DUST,    loc, 6, 0.2, 0.2, 0.2, 0, impact);
        world.spawnParticle(Particle.FLASH,   loc, 1, 0, 0, 0, 0);
        world.spawnParticle(Particle.END_ROD, loc, 2, 0.1, 0.1, 0.1, 0.04);
    }

    /**
     * Wild shot when no target is in range — END_RODs in the player's look direction.
     */
    private void spawnWildShot(Location origin, Vector direction) {
        World world = origin.getWorld();
        if (world == null) return;

        Vector dir = direction.clone().normalize();
        for (double d = 0; d < 5; d += 0.5) {
            Location point = origin.clone().add(dir.clone().multiply(d));
            world.spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Activation burst — wide sphere of gold + orange particles + a FLASH.
     */
    private void spawnActivationBurst(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Location centre = loc.clone().add(0, 1, 0);
        Particle.DustOptions gold   = new Particle.DustOptions(Color.fromRGB(255, 200, 30), 1.5f);
        Particle.DustOptions orange = new Particle.DustOptions(Color.fromRGB(230, 80, 20),  1.2f);

        world.spawnParticle(Particle.DUST,    centre, 20, 0.5, 0.5, 0.5, 0, gold);
        world.spawnParticle(Particle.DUST,    centre, 12, 0.3, 0.3, 0.3, 0, orange);
        world.spawnParticle(Particle.END_ROD, centre,  6, 0.4, 0.4, 0.4, 0.1);
        world.spawnParticle(Particle.FLASH,   centre,  1, 0, 0, 0, 0);
    }

    /**
     * Expiry burst — gentle fade effect as the ultimate winds down.
     */
    private void spawnExpiryBurst(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Location centre = loc.clone().add(0, 1, 0);
        Particle.DustOptions fade = new Particle.DustOptions(Color.fromRGB(180, 120, 30), 1.0f);

        world.spawnParticle(Particle.DUST,    centre, 10, 0.4, 0.4, 0.4, 0, fade);
        world.spawnParticle(Particle.END_ROD, centre,  3, 0.2, 0.2, 0.2, 0.05);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════

    private ItemStack buildGalacticLegendItem() {
        return new ItemBuilder(Material.BLAZE_ROD)
                .name(CC.translate("&eGalactic Legend &7[Right-Click]"))
                .build();
    }

    private boolean isStarLord(Player player) {
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Star-Lord");
        return getProfile(player).getCurrentKit().equals(kit);
    }

    private Profile getProfile(Player player) {
        return SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
    }
}