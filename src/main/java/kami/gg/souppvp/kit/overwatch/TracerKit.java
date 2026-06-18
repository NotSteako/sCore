package kami.gg.souppvp.kit.overwatch;

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
import org.bukkit.event.Event;
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

public class TracerKit extends Kit implements Listener {

    // ── Tuning constants ──────────────────────────────────────────
    private static final int    RECALL_COOLDOWN_SECONDS  = 12;
    private static final int    RECALL_HISTORY_TICKS     = 60;  // 3 seconds of history @ 20 tps
    private static final int    RECALL_INVINCIBILITY_TICKS = 15; // ~0.75 s invulnerability on activate
    private static final int    TRAIL_INTERVAL_TICKS     = 2;   // passive movement trail frequency

    // ── Per-player state ──────────────────────────────────────────

    /** Circular history of (location, health) snapshots, one per tick. */
    private final Map<UUID, Deque<RecallSnapshot>> recallHistory = new HashMap<>();
    /** Invincibility task handle — cancelled after the recall window expires. */
    private final Map<UUID, BukkitRunnable>        invincTasks   = new HashMap<>();
    /** Passive movement trail task. */
    private final Map<UUID, BukkitRunnable>        trailTasks    = new HashMap<>();
    /** Last-click debounce. */
    private final Map<UUID, Long>                  lastClick     = new HashMap<>();
    /** Debounce for the "no history yet" message — don't spam it. */
    private final Map<UUID, Long>                  lastNoHistory = new HashMap<>();
    /** Players currently inside their recall invincibility window. */
    private final Set<UUID>                        invincible    = new HashSet<>();

    // ── Snapshot record ───────────────────────────────────────────
    private static class RecallSnapshot {
        final Location location;
        final double   health;
        RecallSnapshot(Location location, double health) {
            this.location = location.clone();
            this.health   = health;
        }
    }

    // ── Kit metadata ──────────────────────────────────────────────
    @Override public String      getName()       { return "Tracer"; }
    @Override public KitRarity   getRarityType() { return KitRarity.ULTIMATE; }
    @Override public Integer     getPrice()      { return getRarityType().getPrice(); }
    @Override public KitCategory getCategory()   { return KitCategory.OVERWATCH; }

    // ── Cosmetics ─────────────────────────────────────────────────

    private static final CosmeticSkin DEFAULT = new CosmeticSkin(
            "default",
            "&6Default Tracer",
            // Tracer's orange visor / chronal accelerator skin texture hash
            "2e887b062c66b3195419b38aa31703b9c8d86f72e8adb8c9dad798bc0df54ce0",
            Color.fromRGB(235, 90, 42),
            Color.fromRGB(241, 174, 4),
            Color.WHITE
    );

    private static final CosmeticSkin GRAFFITI = new CosmeticSkin(
            "graffiti_tracer",
            "&bGraffiti Tracer",
            // Tracer's orange visor / chronal accelerator skin texture hash
            "aac4432c8248bb737212e962e1c7b5d9882930a24fef68e612455e7f8c464f56",
            Color.fromRGB(41, 107, 165),
            Color.fromRGB(53, 126, 191),
            Color.fromRGB(172, 121, 0)
    );

    @Override
    public List<CosmeticSkin> getAvailableCosmetics() {
        return Arrays.asList(
                DEFAULT,
                GRAFFITI
        );
    }

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
        desc.add("&7Right-click &6Ender Pearl &7to activate &eRecall&7.");
        desc.add("&7Rewinds your position and health to");
        desc.add("&7where you were &e3 seconds ago&7.");
        desc.add("&7Brief &binvincibility &7plays during the rewind.");
        desc.add("&7Cooldown: &e" + RECALL_COOLDOWN_SECONDS + "s");
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
        items.add(buildEnderPearl());
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

        // Tracer is glass-cannon — very light armour
        meta.addAttributeModifier(
                Attribute.ARMOR,
                new AttributeModifier(
                        new NamespacedKey(SoupPvP.getInstance(), "tracer_helmet_armor"),
                        1.0,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HEAD.getGroup()
                )
        );
        helmet.setItemMeta(meta);

        ItemMeta flagMeta = helmet.getItemMeta();
        flagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        helmet.setItemMeta(flagMeta);

        Color cpColor    = skin.hasOutfitColors() ? skin.getChestplateColor() : Color.BLUE;
        Color legColor   = skin.hasOutfitColors() ? skin.getLeggingsColor()   : Color.RED;
        Color bootColor  = skin.hasOutfitColors() ? skin.getBootsColor()      : Color.BLUE;

        return new ItemStack[]{
                // Boots — Tracer's signature orange-and-brown colour palette
                new ItemBuilder(Material.LEATHER_BOOTS)
                        .color(bootColor)
                        .enchantment(Enchantment.PROTECTION, 1)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                new ItemBuilder(Material.LEATHER_LEGGINGS)
                        .color(legColor)
                        .enchantment(Enchantment.PROTECTION, 1)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE)
                        .color(cpColor)
                        .enchantment(Enchantment.PROTECTION, 1)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                helmet
        };
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        // Tracer is the fastest hero in Overwatch — Speed II to reflect that
        List<PotionEffect> effects = new ArrayList<>();
        effects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        return effects;
    }

    // ── Kit select ────────────────────────────────────────────────
    @Override
    public void onSelect(Player player) {
        startHistoryTicker(player);
        startTrailTicker(player);
    }

    // ═══════════════════════════════════════════════════════════════
    //  EVENTS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Re-start tickers on join in case the player already had Tracer selected.
     * Without this, logging back in after a crash/disconnect means the history
     * never starts (onSelect only fires on menu selection, not on load).
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Schedule 1-tick delay so the profile is fully loaded before we check
        Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
            if (player.isOnline() && isTracer(player)) {
                startHistoryTicker(player);
                startTrailTicker(player);
            }
        }, 1L);
    }

    /**
     * Re-start history after respawn so recall is ready immediately.
     */
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
            if (player.isOnline() && isTracer(player)) {
                startHistoryTicker(player);
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRecall(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player    player = event.getPlayer();
        ItemStack held   = player.getInventory().getItemInMainHand();
        if (held.getType() != Material.ENDER_PEARL) return;

        // Prevent vanilla pearl throw
        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setCancelled(true);

        if (!isTracer(player)) return;

        Profile profile = getProfile(player);
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;

        UUID uuid = player.getUniqueId();

        // Debounce
        long now = System.currentTimeMillis();
        if (now - lastClick.getOrDefault(uuid, 0L) < 200) return;
        lastClick.put(uuid, now);

        // Already invincible (mid-recall)
        if (invincible.contains(uuid)) return;

        // Cooldown check
        if (SoupPvP.getInstance().getTimersHandler().getRemaining(uuid, "Recall", true) > 0) {
            player.sendMessage(CC.translate("&eRecall &7is on cooldown for another &e"
                    + DurationFormatter.getRemaining(
                    SoupPvP.getInstance().getTimersHandler().getRemaining(uuid, "Recall", true), true)
                    + "&7."));
            return;
        }

        activateRecall(player);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HISTORY TICKER  (stores a location+health snapshot every tick)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Runs every tick while the player is alive and playing as Tracer.
     * Maintains a rolling 3-second window of (location, health) snapshots.
     */
    private void startHistoryTicker(Player player) {
        UUID uuid = player.getUniqueId();
        stopHistoryTicker(uuid);
        recallHistory.put(uuid, new ArrayDeque<>());

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isTracer(player)) {
                    stopHistoryTicker(uuid);
                    cancel();
                    return;
                }
                Profile profile = getProfile(player);
                if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;

                Deque<RecallSnapshot> history = recallHistory.get(uuid);
                if (history == null) { cancel(); return; }

                // Push new snapshot
                history.addLast(new RecallSnapshot(player.getLocation(), player.getHealth()));

                // Trim to keep only the last RECALL_HISTORY_TICKS entries (3 seconds)
                while (history.size() > RECALL_HISTORY_TICKS) {
                    history.removeFirst();
                }
            }
        };
        task.runTaskTimer(SoupPvP.getInstance(), 0L, 1L);
    }

    private void stopHistoryTicker(UUID uuid) {
        recallHistory.remove(uuid);
    }

    // ═══════════════════════════════════════════════════════════════
    //  PASSIVE TRAIL TICKER  (orange particle trail following Tracer)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Spawns a warm amber/orange dust trail behind Tracer as she moves —
     * a nod to the chronal accelerator energy streaks in Overwatch.
     * Only renders while out of combat zone and not on cooldown/inactive.
     */
    private void startTrailTicker(Player player) {
        UUID uuid = player.getUniqueId();
        stopTrailTicker(uuid);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isTracer(player)) {
                    stopTrailTicker(uuid);
                    cancel();
                    return;
                }
                Profile profile = getProfile(player);
                if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;

                spawnMovementTrail(player);
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
    //  RECALL ACTIVATION
    // ═══════════════════════════════════════════════════════════════

    private void activateRecall(Player player) {
        UUID uuid = player.getUniqueId();

        Deque<RecallSnapshot> history = recallHistory.get(uuid);
        if (history == null || history.isEmpty()) {
            // Only show the "no history" message once every 3 seconds to prevent spam
            long now2 = System.currentTimeMillis();
            if (now2 - lastNoHistory.getOrDefault(uuid, 0L) >= 3000) {
                player.sendMessage(CC.translate("&eRecall &7— &cno history yet, keep moving!"));
                lastNoHistory.put(uuid, now2);
            }
            // Also kick off the ticker now in case it never started
            if (history == null) startHistoryTicker(player);
            return;
        }

        // Target snapshot = oldest in the window (the furthest 3 seconds back)
        RecallSnapshot target = history.peekFirst();

        // ── Burst of outgoing orange particles at current position ──
        spawnRecallBurst(player.getLocation(), false);

        // ── Teleport + health restore ──
        player.teleport(target.location);
        double restoredHealth = Math.min(
                target.health,
                player.getAttribute(Attribute.MAX_HEALTH).getValue()
        );
        // Only restore if it's actually better than current health
        if (restoredHealth > player.getHealth()) {
            player.setHealth(restoredHealth);
        }

        // Clear the history so she can't double-recall to the same window
        history.clear();

        // ── Arrival burst at recall destination ──
        spawnRecallBurst(player.getLocation(), true);

        // ── Sound: Tracer's iconic chronal-rewind "whoosh" ──
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.8f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.5f, 0.7f);

        // ── Message ──
        player.sendMessage(CC.translate("&eRecall &7— &bRewound!"));

        // ── Brief invincibility window ──
        beginInvincibility(player);

        // ── Cooldown ──
        SoupPvP.getInstance().getTimersHandler().addPlayerTimer(
                uuid,
                new Timer("Recall", TimeUnit.SECONDS.toMillis(RECALL_COOLDOWN_SECONDS)),
                true);
        XPBarTimer.runXpBar(player, RECALL_COOLDOWN_SECONDS);
    }

    // ═══════════════════════════════════════════════════════════════
    //  INVINCIBILITY WINDOW
    // ═══════════════════════════════════════════════════════════════

    /**
     * Grants a short invincibility window via max health + RESISTANCE V,
     * then removes it once the window expires.
     * Also spawns a tight looping ring around the player during this window
     * (same visual language as Echo's target ring) to signal the active state.
     */
    private void beginInvincibility(Player player) {
        UUID uuid = player.getUniqueId();

        // Cancel any existing window
        BukkitRunnable existing = invincTasks.remove(uuid);
        if (existing != null) try { existing.cancel(); } catch (IllegalStateException ignored) {}

        invincible.add(uuid);
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE, RECALL_INVINCIBILITY_TICKS, 255, false, false));

        final int[] elapsed = {0};

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) { invincible.remove(uuid); cancel(); return; }

                elapsed[0] += 2;

                // Spin a tight amber ring around the player while invincible
                spawnInvincibilityRing(player.getLocation().clone().add(0, 0.05, 0), elapsed[0]);

                if (elapsed[0] >= RECALL_INVINCIBILITY_TICKS) {
                    invincible.remove(uuid);
                    invincTasks.remove(uuid);
                    cancel();
                }
            }
        };

        invincTasks.put(uuid, task);
        task.runTaskTimer(SoupPvP.getInstance(), 0L, 2L);
    }

    // ═══════════════════════════════════════════════════════════════
    //  PARTICLES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Passive movement trail — warm amber Dust particles (Tracer's chronal energy).
     * Two offset traces at foot level give the illusion of twin thruster exhaust.
     */
    private void spawnMovementTrail(Player player) {
        Location base = player.getLocation().clone().add(0, 0.15, 0);
        World    world = base.getWorld();
        if (world == null) return;

        // Left and right foot offsets relative to player facing
        double yaw = Math.toRadians(base.getYaw() + 90);
        double ox  = Math.cos(yaw) * 0.2;
        double oz  = Math.sin(yaw) * 0.2;

        Particle.DustOptions amber = new Particle.DustOptions(
                Color.fromRGB(255, 140, 20), 1.0f);

        world.spawnParticle(Particle.DUST, base.clone().add( ox, 0,  oz), 1, 0, 0, 0, 0, amber);
        world.spawnParticle(Particle.DUST, base.clone().add(-ox, 0, -oz), 1, 0, 0, 0, 0, amber);

        // Occasional bright white FLASH spark — chronal accelerator "flicker"
        if (Math.random() < 0.3) {
            world.spawnParticle(Particle.END_ROD, base.clone().add(0, 0.3, 0), 1, 0.05, 0.05, 0.05, 0);
        }
    }

    /**
     * Burst of orange/yellow particles at recall departure or arrival.
     * {@code arrival} true = tighter, brighter arrival effect; false = spread departure.
     */
    private void spawnRecallBurst(Location loc, boolean arrival) {
        World world = loc.getWorld();
        if (world == null) return;

        Particle.DustOptions bright = new Particle.DustOptions(Color.fromRGB(255, 200, 40), 1.5f);
        Particle.DustOptions amber  = new Particle.DustOptions(Color.fromRGB(255, 100, 10), 1.2f);

        Location centre = loc.clone().add(0, 1, 0);

        if (arrival) {
            // Arrival: tight vertical column + ring on the ground
            world.spawnParticle(Particle.DUST, centre,               12, 0.2, 0.5, 0.2, 0, bright);
            world.spawnParticle(Particle.DUST, centre.clone().add(0,-0.9,0),  8, 0.4, 0.05, 0.4, 0, amber);
            world.spawnParticle(Particle.FLASH, centre,               1, 0, 0, 0, 0);
        } else {
            // Departure: wide outward spray
            world.spawnParticle(Particle.DUST, centre,               10, 0.4, 0.4, 0.4, 0, amber);
            world.spawnParticle(Particle.END_ROD, centre,             4, 0.3, 0.3, 0.3, 0.08);
        }
    }

    /**
     * Tight spinning ring around the player during the invincibility window.
     * Warm gold — distinct from Echo's cool white ring so players can read the state.
     */
    private static final int    INV_RING_PARTICLES = 16;
    private static final double INV_RING_RADIUS    = 0.55;

    private void spawnInvincibilityRing(Location centre, int tick) {
        World world = centre.getWorld();
        if (world == null) return;

        double offset = tick * 0.35; // rotation over time
        double step   = (2 * Math.PI) / INV_RING_PARTICLES;

        Particle.DustOptions gold = new Particle.DustOptions(Color.fromRGB(255, 190, 30), 0.85f);

        for (int i = 0; i < INV_RING_PARTICLES; i++) {
            double angle = step * i + offset;
            double x     = INV_RING_RADIUS * Math.cos(angle);
            double z     = INV_RING_RADIUS * Math.sin(angle);
            world.spawnParticle(Particle.DUST, centre.clone().add(x, 0.05, z), 1, 0, 0, 0, 0, gold);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════

    private ItemStack buildEnderPearl() {
        return new ItemBuilder(Material.ENDER_PEARL)
                .name(CC.translate("&eRecall &7[Right-Click]"))
                .build();
    }

    private boolean isTracer(Player player) {
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Tracer");
        return getProfile(player).getCurrentKit().equals(kit);
    }

    private Profile getProfile(Player player) {
        return SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
    }
}