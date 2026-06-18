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
import kami.gg.souppvp.util.PlayerUtil;
import kami.gg.souppvp.util.XPBarTimer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class EchoKit extends Kit implements Listener {

    // ── Tuning constants ──────────────────────────────────────────
    private static final int    BEAM_COOLDOWN_SECONDS = 20;
    private static final int    BEAM_DURATION_TICKS   = 60;   // 3 seconds of channelling
    private static final double BEAM_RANGE            = 6.0;
    private static final double BEAM_NORMAL_DAMAGE    = 3.0;  // per tick (every 10 ticks)
    private static final double BEAM_EXECUTE_DAMAGE   = 8.0;  // per tick when target < 50 % HP
    private static final int    DAMAGE_INTERVAL_TICKS = 10;   // damage applied every 10 ticks
    private static final int    IDLE_TICK_LIMIT       = 30;

    // ── Per-player state ──────────────────────────────────────────
    private final Map<UUID, BukkitRunnable> aimTasks    = new HashMap<>(); // passive beam preview
    private final Map<UUID, BukkitRunnable> beamTasks   = new HashMap<>(); // active channel
    private final Map<UUID, Long>           lastClick   = new HashMap<>();
    private final Map<UUID, Location>       lastLookLoc = new HashMap<>();
    private final Map<UUID, Integer>        idleTicks   = new HashMap<>();

    // ── Kit metadata ──────────────────────────────────────────────
    @Override public String      getName()       { return "Echo"; }
    @Override public KitRarity   getRarityType() { return KitRarity.ULTIMATE; }
    @Override public Integer     getPrice()      { return getRarityType().getPrice(); }
    @Override public KitCategory getCategory()   { return KitCategory.OVERWATCH; }

    // ── Cosmetics ─────────────────────────────────────────────────

    private static final CosmeticSkin DEFAULT = new CosmeticSkin(
            "default",
            "&bDefault Echo",
            "197dd5c426f86f8f663a8efbf9619f6513602e98a7c810a46ea95df4eb23b712"
    );


    @Override public CosmeticSkin       getDefaultCosmetic()    { return DEFAULT; }

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
        desc.add("&7Hold &eBlaze Rod &7to preview your &bFocusing Beam&7.");
        desc.add("&7A &bbeam &7locks onto the nearest enemy within &e" + (int) BEAM_RANGE + " blocks&7.");
        desc.add("&7Right-click to channel for &e3s&7.");
        desc.add("&7If the target is &cbelow 50%% HP&7, damage is &cdoubled&7.");
        desc.add("&7Cooldown: &e" + BEAM_COOLDOWN_SECONDS + "s");
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
        items.add(buildBlazeRod());
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

        // Light armour value — Echo is a damage hero, not a tank
        meta.addAttributeModifier(
                Attribute.ARMOR,
                new AttributeModifier(
                        new NamespacedKey(SoupPvP.getInstance(), "echo_helmet_armor"),
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
                // Boots — white/cyan to match Echo's colour palette
                new ItemBuilder(Material.LEATHER_BOOTS)
                        .color(Color.fromRGB(180, 230, 255))
                        .enchantment(Enchantment.PROTECTION, 1)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                new ItemBuilder(Material.LEATHER_LEGGINGS)
                        .color(Color.WHITE)
                        .enchantment(Enchantment.PROTECTION, 1)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE)
                        .color(Color.fromRGB(140, 210, 255))
                        .enchantment(Enchantment.PROTECTION, 1)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                helmet
        };
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        // Echo is nimble but not as beefy as Reaper
        List<PotionEffect> effects = new ArrayList<>();
        effects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
        return effects;
    }

    // ── Kit select ────────────────────────────────────────────────
    @Override
    public void onSelect(Player player) {
        startAimTicker(player);
    }

    // ═══════════════════════════════════════════════════════════════
    //  EVENTS
    // ═══════════════════════════════════════════════════════════════

    @EventHandler
    public void onHoldBlazeRod(PlayerItemHeldEvent event) {
        Player    player  = event.getPlayer();
        if (!isEcho(player)) return;

        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());

        if (newItem != null && newItem.getType() == Material.BLAZE_ROD) startAimTicker(player);
        if (oldItem != null && oldItem.getType() == Material.BLAZE_ROD) stopAimTicker(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFocusingBeam(PlayerInteractEvent event) {
        Action    action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player    player = event.getPlayer();
        ItemStack held   = player.getInventory().getItemInMainHand();
        if (held.getType() != Material.BLAZE_ROD) return;

        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setCancelled(true);

        if (!isEcho(player)) return;

        Profile profile = getProfile(player);
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;

        UUID uuid = player.getUniqueId();

        // Debounce
        long now = System.currentTimeMillis();
        if (now - lastClick.getOrDefault(uuid, 0L) < 200) return;
        lastClick.put(uuid, now);

        // Already channelling
        if (beamTasks.containsKey(uuid)) return;

        // Cooldown check
        if (SoupPvP.getInstance().getTimersHandler().getRemaining(uuid, "Focusing Beam", true) > 0) {
            player.sendMessage(CC.translate("&bFocusing Beam &7is on cooldown for another &e"
                    + DurationFormatter.getRemaining(
                    SoupPvP.getInstance().getTimersHandler().getRemaining(uuid, "Focusing Beam", true), true)
                    + "&7."));
            return;
        }

        Player target = findBeamTarget(player);
        if (target == null) {
            player.sendMessage(CC.translate("&bFocusing Beam&7: &cNo enemy in range!"));
            return;
        }

        startBeamChannel(player, target);
    }

    // ═══════════════════════════════════════════════════════════════
    //  AIM TICKER  (passive — shows beam preview while holding rod)
    // ═══════════════════════════════════════════════════════════════

    private void startAimTicker(Player player) {
        UUID uuid = player.getUniqueId();
        stopAimTicker(uuid);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isEcho(player)) { stopAimTicker(uuid); cancel(); return; }

                ItemStack held = player.getInventory().getItemInMainHand();
                if (held.getType() != Material.BLAZE_ROD) { stopAimTicker(uuid); cancel(); return; }

                Profile profile = getProfile(player);
                if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;

                // Don't render while channelling or on cooldown
                if (beamTasks.containsKey(uuid)) return;
                if (SoupPvP.getInstance().getTimersHandler().getRemaining(uuid, "Focusing Beam", true) > 0) return;
                if (isIdle(player)) return;

                // Find nearest target and draw a preview particle line toward them
                Player target = findBeamTarget(player);
                if (target == null) return;

                spawnBeamPreview(player, target);
            }
        };

        aimTasks.put(uuid, task);
        task.runTaskTimer(SoupPvP.getInstance(), 0L, 2L);
    }

    private void stopAimTicker(UUID uuid) {
        BukkitRunnable existing = aimTasks.remove(uuid);
        if (existing != null) try { existing.cancel(); } catch (IllegalStateException ignored) {}
        lastLookLoc.remove(uuid);
        idleTicks.remove(uuid);
    }

    // ═══════════════════════════════════════════════════════════════
    //  BEAM CHANNEL  (active — damages target over 3 seconds)
    // ═══════════════════════════════════════════════════════════════

    private void startBeamChannel(Player caster, Player target) {
        UUID uuid = caster.getUniqueId();

        caster.sendMessage(CC.translate("&bFocusing Beam &7— channelling!"));
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.6f);

        // Slow caster slightly while channelling (flavour)
        caster.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, BEAM_DURATION_TICKS, 0, false, false));

        final int[] ticksElapsed = {0};

        Player currentTarget = findBeamTarget(caster);


        BukkitRunnable beam = new BukkitRunnable() {
            @Override
            public void run() {
                // Abort if either player disconnects / kit changes
                if (!caster.isOnline() || !target.isOnline() || !isEcho(caster)) {
                    finishBeam(uuid, caster, false);
                    cancel();
                    return;
                }

                // Abort if target moves out of range or line-of-sight is blocked
                if (caster.getLocation().distance(target.getLocation()) > BEAM_RANGE + 2) {
                    caster.sendMessage(CC.translate("&bFocusing Beam&7: &ctarget moved out of range!"));
                    finishBeam(uuid, caster, false);
                    cancel();
                    return;
                }

                if (currentTarget == null || !currentTarget.getUniqueId().equals(target.getUniqueId())) {
                    caster.sendMessage(CC.translate("&bFocusing Beam&7: &cTarget lost!"));
                    finishBeam(uuid, caster, false);
                    cancel();
                    return;
                }

                ticksElapsed[0]++;

                // Deal damage every DAMAGE_INTERVAL_TICKS
                if (ticksElapsed[0] % DAMAGE_INTERVAL_TICKS == 0) {
                    boolean executeRange = target.getHealth() < (target.getAttribute(Attribute.MAX_HEALTH).getValue() / 2.0);
                    double  dmg         = executeRange ? BEAM_EXECUTE_DAMAGE : BEAM_NORMAL_DAMAGE;
                    target.damage(dmg, caster);

                    if (executeRange) {
                        // Flash a red particle burst on the target to signal execute mode
                        target.getWorld().spawnParticle(
                                Particle.DAMAGE_INDICATOR,
                                target.getLocation().clone().add(0, 1, 0),
                                6, 0.2, 0.3, 0.2, 0.05);
                    }
                }

                // Beam particle line every tick
                spawnActiveBeam(caster, target, ticksElapsed[0]);

                // End after full duration
                if (ticksElapsed[0] >= BEAM_DURATION_TICKS) {
                    finishBeam(uuid, caster, true);
                    cancel();
                }
            }
        };

        beamTasks.put(uuid, beam);
        beam.runTaskTimer(SoupPvP.getInstance(), 0L, 1L);
    }

    private void finishBeam(UUID uuid, Player caster, boolean applyFullCooldown) {
        BukkitRunnable existing = beamTasks.remove(uuid);
        if (existing != null) try { existing.cancel(); } catch (IllegalStateException ignored) {}

        if (!caster.isOnline()) return;

        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.7f, 1.6f);

        if (applyFullCooldown) {
            caster.sendMessage(CC.translate("&bFocusing Beam &7— complete!"));
        }

        // Always apply cooldown regardless of early cancellation (punish interruption)
        SoupPvP.getInstance().getTimersHandler().addPlayerTimer(
                uuid,
                new Timer("Focusing Beam", TimeUnit.SECONDS.toMillis(BEAM_COOLDOWN_SECONDS)),
                true);
        XPBarTimer.runXpBar(caster, BEAM_COOLDOWN_SECONDS);
    }

    // ═══════════════════════════════════════════════════════════════
    //  PARTICLES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Preview: a gentle dotted cyan line from Echo's hand toward the target.
     * Mirrors Reaper's ring concept — runs on the aim ticker (every 2 ticks).
     */
    private void spawnBeamPreview(Player caster, Player target) {
        Location from   = caster.getEyeLocation().clone().add(caster.getLocation().getDirection().multiply(0.5));
        Location to     = target.getLocation().clone().add(0, 1, 0);
        Vector   dir    = to.toVector().subtract(from.toVector());
        double   length = dir.length();
        dir.normalize();

        int dots = (int) (length / 0.6);
        for (int i = 0; i < dots; i++) {
            Location point = from.clone().add(dir.clone().multiply(i * 0.6));
            // Alternating WITCH (purple-ish) and END_ROD (white) for a sci-fi look
            Particle p = (i % 2 == 0) ? Particle.WITCH : Particle.END_ROD;
            caster.getWorld().spawnParticle(p, point, 1, 0, 0, 0, 0);
        }

        // Small rotating ring around the target (like Reaper's destination ring)
        spawnTargetRing(target.getLocation().clone().add(0, 0.05, 0), caster.getUniqueId());
    }

    /**
     * Active beam: solid, bright cyan line with END_ROD particles.
     * Pulses with ELECTRIC_SPARK at the point of impact.
     */
    private void spawnActiveBeam(Player caster, Player target, int tick) {
        Location from = caster.getEyeLocation().clone().add(caster.getLocation().getDirection().multiply(0.5));
        Location to   = target.getLocation().clone().add(0, 1, 0);
        Vector   dir  = to.toVector().subtract(from.toVector());
        double   length = dir.length();
        dir.normalize();

        int dots = (int) (length / 0.35); // tighter spacing for the active beam
        for (int i = 0; i < dots; i++) {
            Location point = from.clone().add(dir.clone().multiply(i * 0.35));
            caster.getWorld().spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0);
        }

        // Impact sparks on the target
        caster.getWorld().spawnParticle(
                Particle.ELECTRIC_SPARK,
                target.getLocation().clone().add(0, 1, 0),
                3, 0.15, 0.3, 0.15, 0.02);

        // Pulse sound every 20 ticks
        if (tick % 20 == 0) {
            caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 0.5f, 1.8f);
        }
    }

    /**
     * Small spinning ring around the locked-on target — same technique as Reaper's destination ring.
     */
    private static final int    RING_PARTICLES = 20;
    private static final double RING_RADIUS    = 0.7;
    private final Map<UUID, Double> ringOffsets = new HashMap<>();

    private void spawnTargetRing(Location center, UUID casterUUID) {
        World  world  = center.getWorld();
        if (world == null) return;

        double offset = ringOffsets.getOrDefault(casterUUID, 0.0);
        double step   = (2 * Math.PI) / RING_PARTICLES;

        for (int i = 0; i < RING_PARTICLES; i++) {
            double angle = step * i + offset;
            double x = RING_RADIUS * Math.cos(angle);
            double z = RING_RADIUS * Math.sin(angle);
            world.spawnParticle(Particle.END_ROD, center.clone().add(x, 0.05, z), 1, 0, 0, 0, 0);
        }

        ringOffsets.put(casterUUID, offset + 0.25);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Finds the nearest enemy player within BEAM_RANGE using a ray-trace first,
     * then falls back to proximity scan — so aiming roughly at someone locks them.
     */
    private Player findBeamTarget(Player caster) {
        RayTraceResult ray = caster.getWorld().rayTraceEntities(
                caster.getEyeLocation(),
                caster.getEyeLocation().getDirection(),
                BEAM_RANGE,
                entity -> entity instanceof Player
                        && !entity.equals(caster));

        if (ray != null && ray.getHitEntity() instanceof Player) {
            Player hit = (Player) ray.getHitEntity();

            if (isValidTarget(caster, hit)) {
                return hit;
            }
        }

        return null;
    }

    private boolean isValidTarget(Player caster, Player target) {
        Profile tp = getProfile(target);
        return !tp.isInEvent()
                && tp.getProfileState() != ProfileState.SPAWN
                && target.isOnline()
                && !target.equals(caster);
    }

    // Idle detection — same logic as Reaper
    private boolean isIdle(Player player) {
        UUID     uuid    = player.getUniqueId();
        Location current = player.getLocation();
        Location last    = lastLookLoc.get(uuid);

        if (last == null) {
            lastLookLoc.put(uuid, current.clone());
            idleTicks.put(uuid, 0);
            return false;
        }

        boolean moved = last.getX() != current.getX()
                || last.getY() != current.getY()
                || last.getZ() != current.getZ()
                || Math.abs(last.getYaw()   - current.getYaw())   > 0.5f
                || Math.abs(last.getPitch() - current.getPitch()) > 0.5f;

        if (moved) {
            lastLookLoc.put(uuid, current.clone());
            idleTicks.put(uuid, 0);
            return false;
        }

        int ticks = idleTicks.getOrDefault(uuid, 0) + 2;
        idleTicks.put(uuid, ticks);
        return ticks >= IDLE_TICK_LIMIT;
    }

    private ItemStack buildBlazeRod() {
        return new ItemBuilder(Material.BLAZE_ROD)
                .name(CC.translate("&bFocusing Beam &7[Right-Click]"))
                .build();
    }

    private boolean isEcho(Player player) {
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Echo");
        return getProfile(player).getCurrentKit().equals(kit);
    }

    private Profile getProfile(Player player) {
        return SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
    }
}