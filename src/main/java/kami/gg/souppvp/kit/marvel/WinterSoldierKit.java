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
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class WinterSoldierKit extends Kit implements Listener {

    // ── Tuning constants ──────────────────────────────────────────
    /** Maximum range of the Bionic Hook pull (blocks). */
    private static final double HOOK_RANGE            = 12.0;
    /** How hard the target is yanked toward the caster. Higher = snappier pull. */
    private static final double HOOK_PULL_STRENGTH    = 1.6;
    /** Small upward arc added to the pull so targets don't clip the floor. */
    private static final double HOOK_LIFT             = 0.25;
    /** Cooldown after a successful hook (seconds). */
    private static final int    HOOK_COOLDOWN_SECONDS = 12;
    /** Passive trail interval (ticks). */
    private static final int    TRAIL_INTERVAL_TICKS  = 4;
    /** Ticks to run the chain-link particle animation per cast (rope visual). */
    private static final int    CHAIN_ANIM_TICKS      = 6;

    // ── Per-player state ──────────────────────────────────────────
    /** Players currently inside a hook animation frame window. */
    private final Set<UUID>                 hookAnimating = new HashSet<>();
    /** Last-click debounce timestamps. */
    private final Map<UUID, Long>           lastClick     = new HashMap<>();
    /** Passive trail tasks. */
    private final Map<UUID, BukkitRunnable> trailTasks    = new HashMap<>();

    // ── Kit metadata ──────────────────────────────────────────────
    @Override public String      getName()       { return "Winter Soldier"; }
    @Override public KitRarity   getRarityType() { return KitRarity.LEGENDARY; }
    @Override public Integer     getPrice()      { return getRarityType().getPrice(); }
    @Override public KitCategory getCategory()   { return KitCategory.MARVEL; }

    // ── Cosmetics ─────────────────────────────────────────────────

    private static final CosmeticSkin DEFAULT = new CosmeticSkin(
            "default",
            "&7Default Winter Soldier",
            // James Buchanan Barnes / Bucky skin texture hash
            "df3ed9713ddfe9cebfd1005a8060a4205081458f990c81a230eec6bdfbb36a6d",
            Color.fromRGB(25, 25, 30),    // near-black tactical outfit
            Color.fromRGB(60, 60, 70),    // dark steel armour plate
            Color.fromRGB(180, 30, 30)    // red star on metal arm
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
        desc.add("&7Right-click &8Hook &7to launch your &bBionic Hook&7.");
        desc.add("&7Snare the nearest enemy within &f" + (int) HOOK_RANGE + " blocks");
        desc.add("&7and yank them directly toward you.");
        desc.add("&7Cooldown: &e" + HOOK_COOLDOWN_SECONDS + "s");
        return desc;
    }

    // ── Equipment ─────────────────────────────────────────────────
    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> items = new ArrayList<>();
        // Roterstern — Winter Soldier's handgun represented as a sword
        items.add(new ItemBuilder(Material.IRON_SWORD)
                .enchantment(Enchantment.SHARPNESS, 2)
                .enchantment(Enchantment.UNBREAKING, 3)
                .build());
        // Bionic Hook activator
        items.add(buildHookItem());
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

        // Slightly tankier than Star-Lord — Winter Soldier is a super-soldier
        meta.addAttributeModifier(
                Attribute.ARMOR,
                new AttributeModifier(
                        new NamespacedKey(SoupPvP.getInstance(), "wintersoldier_helmet_armor"),
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
                // Dark tactical boots
                new ItemBuilder(Material.LEATHER_BOOTS)
                        .color(Color.fromRGB(20, 20, 25))
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                // Dark combat trousers
                new ItemBuilder(Material.LEATHER_LEGGINGS)
                        .color(Color.fromRGB(25, 25, 35))
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                // Tactical chestplate — dark with muted steel tone
                new ItemBuilder(Material.LEATHER_CHESTPLATE)
                        .color(Color.fromRGB(30, 30, 40))
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                helmet
        };
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        // Ceaseless Charge passive — slight speed boost, super-soldier agility
        List<PotionEffect> effects = new ArrayList<>();
        effects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false));
        return effects;
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
            if (player.isOnline() && isWinterSoldier(player)) {
                startTrailTicker(player);
            }
        }, 1L);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
            if (player.isOnline() && isWinterSoldier(player)) {
                startTrailTicker(player);
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBionicHook(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player    player = event.getPlayer();
        ItemStack held   = player.getInventory().getItemInMainHand();
        if (held.getType() != Material.CHAIN) return;

        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setCancelled(true);

        if (!isWinterSoldier(player)) return;

        Profile profile = getProfile(player);
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;

        UUID uuid = player.getUniqueId();

        // Debounce — prevent double-fire
        long now = System.currentTimeMillis();
        if (now - lastClick.getOrDefault(uuid, 0L) < 200) return;
        lastClick.put(uuid, now);

        // Animation lock — prevent spam during chain anim
        if (hookAnimating.contains(uuid)) {
            player.sendMessage(CC.translate("&bBionic Hook &7is already in flight!"));
            return;
        }

        // Cooldown check
        if (SoupPvP.getInstance().getTimersHandler().getRemaining(uuid, "BionicHook", true) > 0) {
            player.sendMessage(CC.translate("&bBionic Hook &7is on cooldown for another &e"
                    + DurationFormatter.getRemaining(
                    SoupPvP.getInstance().getTimersHandler().getRemaining(uuid, "BionicHook", true), true)
                    + "&7."));
            return;
        }

        fireBionicHook(player);
    }

    // ═══════════════════════════════════════════════════════════════
    //  BIONIC HOOK LOGIC
    // ═══════════════════════════════════════════════════════════════

    private void fireBionicHook(Player caster) {
        UUID uuid = caster.getUniqueId();

        // Find nearest enemy in range
        Player target = findNearestEnemy(caster);

        if (target == null) {
            // Missed cast — fire a "whiff" chain animation in look direction
            caster.sendMessage(CC.translate("&bBionic Hook &7— &cNo target in range!"));
            caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_FISHING_BOBBER_THROW, 0.8f, 1.3f);
            spawnWhiffChain(caster.getLocation().clone().add(0, 1.2, 0),
                    caster.getLocation().getDirection());
            return;
        }

        // Lock the caster so they can't spam during the pull animation
        hookAnimating.add(uuid);

        Location from = caster.getLocation().clone().add(0, 1.2, 0);
        Location to   = target.getLocation().clone().add(0, 1.0, 0);

        // ── Sounds: chain launch + impact ──
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_FISHING_BOBBER_THROW, 1.0f, 0.8f);
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_CHAIN_HIT,             0.9f, 1.2f);

        // ── Animate the chain travelling to the target over CHAIN_ANIM_TICKS ──
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (!caster.isOnline() || !target.isOnline()) {
                    hookAnimating.remove(uuid);
                    cancel();
                    return;
                }

                // Interpolate chain tip toward target
                double progress = (double) tick / CHAIN_ANIM_TICKS;
                Location tip = interpolate(from, to, progress);

                // Draw chain particles from caster to current tip
                spawnChainLine(from, tip);

                // Spawn rotating steel ring around the tip (targeting reticle)
                spawnHookReticle(tip, tick);

                tick++;

                if (tick > CHAIN_ANIM_TICKS) {
                    // ── Hook lands — yank the target ──
                    applyPull(caster, target);

                    // Impact effects at target location
                    spawnHookImpact(target.getLocation().clone().add(0, 1.0, 0));
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT,   0.7f, 1.4f);
                    target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND,         0.4f, 1.8f);

                    caster.sendMessage(CC.translate("&bBionic Hook &7— &aGot them!"));

                    // Brief residual chain display then clear
                    Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
                        hookAnimating.remove(uuid);
                    }, 3L);

                    // Apply cooldown
                    SoupPvP.getInstance().getTimersHandler().addPlayerTimer(
                            uuid,
                            new Timer("BionicHook",
                                    TimeUnit.SECONDS.toMillis(HOOK_COOLDOWN_SECONDS)),
                            true);
                    XPBarTimer.runXpBar(caster, HOOK_COOLDOWN_SECONDS);

                    cancel();
                }
            }
        }.runTaskTimer(SoupPvP.getInstance(), 0L, 1L);
    }

    /**
     * Calculates and applies the Bionic Hook pull velocity to the target,
     * sending them flying toward the caster.
     */
    private void applyPull(Player caster, Player target) {
        Vector pull = caster.getLocation().toVector()
                .subtract(target.getLocation().toVector())
                .normalize()
                .multiply(HOOK_PULL_STRENGTH);

        pull.setY(Math.abs(pull.getY()) + HOOK_LIFT); // always lift slightly

        target.setVelocity(pull);
    }

    /**
     * Finds the nearest living, non-spawn enemy within HOOK_RANGE.
     */
    private Player findNearestEnemy(Player caster) {
        Player nearest     = null;
        double nearestDist = Double.MAX_VALUE;

        for (Player other : caster.getWorld().getPlayers()) {
            if (other.equals(caster)) continue;
            if (!other.isOnline() || other.isDead()) continue;

            Profile p = getProfile(other);
            if (p.getProfileState() == ProfileState.SPAWN) continue;
            if (p.isInEvent()) continue;

            double dist = caster.getLocation().distanceSquared(other.getLocation());
            if (dist <= HOOK_RANGE * HOOK_RANGE && dist < nearestDist) {
                nearestDist = dist;
                nearest     = other;
            }
        }

        return nearest;
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
                if (!player.isOnline() || !isWinterSoldier(player)) {
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
        if (existing != null) try { existing.cancel(); } catch (IllegalStateException ignored) {}
    }

    // ═══════════════════════════════════════════════════════════════
    //  PARTICLES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Passive trail — cold steel-blue sparks at foot level.
     * Reflects the Winter Soldier's cold, mechanical aesthetic.
     */
    private void spawnPassiveTrail(Player player) {
        Location base  = player.getLocation().clone().add(0, 0.1, 0);
        World    world = base.getWorld();
        if (world == null) return;

        // Dark steel particles along the player's side (bionic arm side)
        double yaw = Math.toRadians(base.getYaw() + 90);
        double ox  = Math.cos(yaw) * 0.15;
        double oz  = Math.sin(yaw) * 0.15;

        Particle.DustOptions steel = new Particle.DustOptions(Color.fromRGB(80, 100, 130), 0.8f);
        world.spawnParticle(Particle.DUST, base.clone().add(ox, 0, oz), 1, 0, 0, 0, 0, steel);

        // Red star flicker on the bionic arm side — occasional red spark
        if (Math.random() < 0.2) {
            Particle.DustOptions redStar = new Particle.DustOptions(Color.fromRGB(200, 20, 20), 0.9f);
            world.spawnParticle(Particle.DUST, base.clone().add(-ox, 0.3, -oz), 1, 0, 0, 0, 0, redStar);
        }
    }

    /**
     * Chain line from caster origin to the current hook tip.
     * Uses alternating steel-grey and dark particles to mimic a chain link texture.
     */
    private void spawnChainLine(Location from, Location to) {
        World world = from.getWorld();
        if (world == null) return;

        Vector dir    = to.toVector().subtract(from.toVector());
        double length = dir.length();
        if (length == 0) return;

        dir.normalize();

        Particle.DustOptions linkA = new Particle.DustOptions(Color.fromRGB(160, 170, 180), 0.8f); // bright link
        Particle.DustOptions linkB = new Particle.DustOptions(Color.fromRGB(50,  55,  65),  0.7f); // dark gap

        double step = 0.4;
        int    i    = 0;
        for (double d = 0; d < length; d += step) {
            Location point = from.clone().add(dir.clone().multiply(d));
            // Alternate bright/dark to simulate chain links
            world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, (i % 2 == 0) ? linkA : linkB);
            i++;
        }
    }

    /**
     * Spinning steel reticle at the hook tip during the travel animation —
     * mirrors the Star-Lord lock-on ring concept but styled for Winter Soldier
     * (tighter, colder, red-star accent).
     */
    private static final int    RETICLE_PARTICLES = 12;
    private static final double RETICLE_RADIUS    = 0.4;

    private void spawnHookReticle(Location centre, int tick) {
        World world = centre.getWorld();
        if (world == null) return;

        double offset = tick * 0.4;
        double step   = (2 * Math.PI) / RETICLE_PARTICLES;

        Particle.DustOptions steel  = new Particle.DustOptions(Color.fromRGB(150, 160, 175), 0.65f);
        Particle.DustOptions redAcc = new Particle.DustOptions(Color.fromRGB(200, 20,  20),  0.7f);

        for (int i = 0; i < RETICLE_PARTICLES; i++) {
            double angle = step * i + offset;
            double x     = RETICLE_RADIUS * Math.cos(angle);
            double z     = RETICLE_RADIUS * Math.sin(angle);

            // Every 4th particle is a red star accent
            Particle.DustOptions color = (i % 4 == 0) ? redAcc : steel;
            world.spawnParticle(Particle.DUST, centre.clone().add(x, 0, z), 1, 0, 0, 0, 0, color);
        }

        // END_ROD flash at the very tip
        if (tick % 2 == 0) {
            world.spawnParticle(Particle.END_ROD, centre, 1, 0.02, 0.02, 0.02, 0.01);
        }
    }

    /**
     * Impact burst when the hook lands — shrapnel scatter with a red-star core flash.
     */
    private void spawnHookImpact(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Particle.DustOptions steelBurst = new Particle.DustOptions(Color.fromRGB(180, 190, 200), 1.2f);
        Particle.DustOptions redCore    = new Particle.DustOptions(Color.fromRGB(220, 30,  30),  1.0f);

        world.spawnParticle(Particle.DUST,    loc, 10, 0.3, 0.3, 0.3, 0, steelBurst);
        world.spawnParticle(Particle.DUST,    loc,  4, 0.1, 0.1, 0.1, 0, redCore);
        world.spawnParticle(Particle.END_ROD, loc,  4, 0.2, 0.2, 0.2, 0.06);
        world.spawnParticle(Particle.FLASH,   loc,  1, 0,   0,   0,   0);
    }

    /**
     * Whiff animation — END_RODs fired in the cast direction when no target is found.
     */
    private void spawnWhiffChain(Location origin, Vector direction) {
        World world = origin.getWorld();
        if (world == null) return;

        Vector dir = direction.clone().normalize();
        Particle.DustOptions steel = new Particle.DustOptions(Color.fromRGB(140, 150, 160), 0.7f);

        // Chain travels 6 blocks then vanishes
        for (double d = 0; d < 6.0; d += 0.4) {
            Location point = origin.clone().add(dir.clone().multiply(d));
            world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, steel);
        }
        // END_ROD fizzle at max range
        Location end = origin.clone().add(dir.clone().multiply(6.0));
        world.spawnParticle(Particle.END_ROD, end, 3, 0.1, 0.1, 0.1, 0.03);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════

    /** Linear interpolation between two Locations. */
    private Location interpolate(Location a, Location b, double t) {
        double x = a.getX() + (b.getX() - a.getX()) * t;
        double y = a.getY() + (b.getY() - a.getY()) * t;
        double z = a.getZ() + (b.getZ() - a.getZ()) * t;
        return new Location(a.getWorld(), x, y, z);
    }

    private ItemStack buildHookItem() {
        return new ItemBuilder(Material.CHAIN)
                .name(CC.translate("&bBionic Hook &7[Right-Click]"))
                .build();
    }

    private boolean isWinterSoldier(Player player) {
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Winter Soldier");
        return getProfile(player).getCurrentKit().equals(kit);
    }

    private Profile getProfile(Player player) {
        return SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
    }
}