package kami.gg.souppvp.kit.valorant;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.KitCategory;
import kami.gg.souppvp.kit.KitRarity;
import kami.gg.souppvp.kit.cosmetic.CosmeticSkin;
import kami.gg.souppvp.kit.cosmetic.SkinApplier;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
import java.util.concurrent.ConcurrentHashMap;

public class ReynaKit extends Kit implements Listener {

    // ── Tuning constants ──────────────────────────────────────────

    /** How long the Soul Orb lingers before disappearing (ticks). ~6 seconds. */
    private static final int    ORB_LIFETIME_TICKS     = 120;

    /** How long Devour heals Reyna (ticks). 3 seconds of rapid regeneration. */
    private static final int    DEVOUR_DURATION_TICKS  = 60;
    /** Regen strength during Devour (Regeneration II = 1 HP/tick roughly). */
    private static final int    DEVOUR_REGEN_AMPLIFIER = 2;

    /** How long Dismiss invisibility lasts (ticks). 3 seconds. */
    private static final int    DISMISS_DURATION_TICKS = 60;

    /** Radius within which Reyna must be to interact with a Soul Orb. */
    private static final double ORB_INTERACT_RADIUS    = 5.0;

    /** Click debounce (ms). */
    private static final long   CLICK_DEBOUNCE_MS      = 250;
    /** How often the "no orb nearby" message can fire (ms). Prevents chat spam. */
    private static final long   NO_ORB_MSG_COOLDOWN_MS = 1_500;

    // ── Soul Orb state ────────────────────────────────────────────

    /**
     * Represents a single Soul Orb left by a killed player.
     */
    private static class SoulOrb {
        final UUID       killerUUID;   // which Reyna player owns this orb
        final Location   location;     // orb world position
        final ArmorStand stand;        // invisible stand used as the particle anchor
        BukkitRunnable   lifetimeTask; // ticks down and despawns the orb
        BukkitRunnable   ambientTask;  // particle loop
        boolean          consumed = false;

        SoulOrb(UUID killerUUID, Location location, ArmorStand stand) {
            this.killerUUID = killerUUID;
            this.location   = location;
            this.stand      = stand;
        }
    }

    /**
     * One Reyna player may have multiple orbs up (from combo kills).
     * UUID → list of active orbs belonging to that player.
     */
    private final Map<UUID, List<SoulOrb>> playerOrbs = new ConcurrentHashMap<>();

    /** Last click timestamp per player for debounce. */
    private final Map<UUID, Long> lastClick     = new HashMap<>();
    /** Last "no orb" message timestamp — suppresses chat spam. */
    private final Map<UUID, Long> lastNoOrbMsg  = new HashMap<>();

    // ── Kit metadata ──────────────────────────────────────────────

    @Override public String      getName()       { return "Reyna"; }
    @Override public KitRarity   getRarityType() { return KitRarity.ULTIMATE; }
    @Override public Integer     getPrice()      { return getRarityType().getPrice(); }
    @Override public KitCategory getCategory()   { return KitCategory.VALORANT; }

    // ── Cosmetics ─────────────────────────────────────────────────

    private static final CosmeticSkin DEFAULT = new CosmeticSkin(
            "default",
            "&5Default Reyna",
            "ee7333e315c6d97c96eaa5b2c382fdf4e3e27df865e37f685b219e94811728f",
            Color.fromRGB(43, 48, 82),     // midnight blue
            Color.fromRGB(188, 164, 108),  // antique gold
            Color.fromRGB(180, 70, 170)    // magenta sash
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
        desc.add("&7When you kill a player, a &5Soul Orb &7rises above their body.");
        desc.add("&7Approach the orb and use &5Soul Consumer &7to claim it:");
        desc.add("  &a&lLeft-Click &r&7— &5Dismiss&7: Gain &finvisibility &7for &e"
                + (DISMISS_DURATION_TICKS / 20) + "s &7+ a speed burst.");
        desc.add("  &d&lRight-Click &r&7— &5Devour&7: Rapidly &aregain health &7for &e"
                + (DEVOUR_DURATION_TICKS / 20) + "s&7.");
        desc.add("&7Orbs expire after &e" + (ORB_LIFETIME_TICKS / 20) + "s &7— use them fast.");
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
        items.add(buildSoulConsumerItem());
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
                        new NamespacedKey(SoupPvP.getInstance(), "reyna_helmet_armor"),
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
                        .color(Color.fromRGB(60, 0, 90))
                        .enchantment(Enchantment.PROTECTION, 1)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                new ItemBuilder(Material.LEATHER_LEGGINGS)
                        .color(Color.fromRGB(40, 10, 70))
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE)
                        .color(Color.fromRGB(80, 20, 120))
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

    @Override
    public void onSelect(Player player) {
        // No passive trail — Reyna's passive is entirely kill-triggered
    }

    // ═══════════════════════════════════════════════════════════════
    //  EVENTS
    // ═══════════════════════════════════════════════════════════════

    /**
     * When a Reyna kills someone, spawn a Soul Orb at the victim's location.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) return;
        if (!isReyna(killer)) return;

        Profile killerProfile = getProfile(killer);
        if (killerProfile.isInEvent() || killerProfile.getProfileState() == ProfileState.SPAWN) return;

        // Small delay so the death location is fully settled
        Location deathLoc = victim.getLocation().clone();
        Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(),
                () -> spawnSoulOrb(killer, deathLoc), 3L);
    }

    /**
     * Handle left-click (Dismiss) and right-click (Devour) on the Soul Consumer item.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onSoulConsumer(PlayerInteractEvent event) {
        Action action = event.getAction();
        boolean isLeft  = (action == Action.LEFT_CLICK_AIR  || action == Action.LEFT_CLICK_BLOCK);
        boolean isRight = (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK);
        if (!isLeft && !isRight) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player    player = event.getPlayer();
        ItemStack held   = player.getInventory().getItemInMainHand();
        if (held.getType() != Material.PURPLE_DYE) return;

        event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
        event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
        event.setCancelled(true);

        if (!isReyna(player)) return;

        Profile profile = getProfile(player);
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;

        // Debounce
        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();
        if (now - lastClick.getOrDefault(uuid, 0L) < CLICK_DEBOUNCE_MS) return;
        lastClick.put(uuid, now);

        // Find nearest orb within range
        SoulOrb nearest = findNearestOrb(player);
        if (nearest == null) {
            long nowMsg = System.currentTimeMillis();
            if (nowMsg - lastNoOrbMsg.getOrDefault(uuid, 0L) >= NO_ORB_MSG_COOLDOWN_MS) {
                player.sendMessage(CC.translate("&5Soul Consumer &7— &cNo Soul Orb nearby."));
                lastNoOrbMsg.put(uuid, nowMsg);
            }
            return;
        }

        if (isLeft) {
            activateDismiss(player, nearest);
        } else {
            activateDevour(player, nearest);
        }
    }

    /**
     * Clean up all orbs when a Reyna player disconnects.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        removeAllOrbs(uuid);
        lastClick.remove(uuid);
        lastNoOrbMsg.remove(uuid);
    }

    // ═══════════════════════════════════════════════════════════════
    //  SOUL ORB LIFECYCLE
    // ═══════════════════════════════════════════════════════════════

    private void spawnSoulOrb(Player killer, Location deathLoc) {
        if (!killer.isOnline()) return;
        World world = deathLoc.getWorld();
        if (world == null) return;

        // Position the orb 2.4 blocks above the death location so it floats
        // clearly above the body/dropped items regardless of terrain
        Location orbLoc = deathLoc.clone().add(0, 2.4, 0);

        // Spawn an invisible, invulnerable, no-gravity armor stand as an anchor
        // so we can attach the orb location cleanly to a UUID-tracked entity
        ArmorStand stand = (ArmorStand) world.spawnEntity(orbLoc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setSmall(true);
        stand.setMarker(true);  // no hitbox — purely cosmetic anchor

        SoulOrb orb = new SoulOrb(killer.getUniqueId(), orbLoc, stand);

        // Register
        playerOrbs.computeIfAbsent(killer.getUniqueId(), k -> new ArrayList<>()).add(orb);

        // Notification
        killer.sendMessage(CC.translate("&5Soul Orb &7available — &aRight-Click &7to &dDevour &7or &aLeft-Click &7to &5Dismiss&7."));
        world.playSound(orbLoc, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 0.7f, 0.6f);
        world.playSound(orbLoc, Sound.ENTITY_ENDERMAN_AMBIENT,       0.4f, 1.6f);

        // Spawn burst
        spawnOrbSpawnBurst(orbLoc);

        // ── Ambient particle loop ──
        BukkitRunnable ambientTask = new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (orb.consumed || !stand.isValid()) { cancel(); return; }
                spawnOrbAmbient(orbLoc, tick++);
            }
        };
        orb.ambientTask = ambientTask;
        ambientTask.runTaskTimer(SoupPvP.getInstance(), 0L, 1L);

        // ── Lifetime countdown ──
        BukkitRunnable lifetimeTask = new BukkitRunnable() {
            @Override public void run() {
                if (!orb.consumed) expireOrb(orb, killer);
            }
        };
        orb.lifetimeTask = lifetimeTask;
        lifetimeTask.runTaskLater(SoupPvP.getInstance(), ORB_LIFETIME_TICKS);
    }

    /**
     * Orb timed out without being consumed.
     */
    private void expireOrb(SoulOrb orb, Player killer) {
        if (orb.consumed) return;
        consumeOrb(orb);
        if (killer.isOnline()) {
            killer.sendMessage(CC.translate("&8Your &5Soul Orb &8faded."));
        }
        spawnOrbFadeBurst(orb.location);
        orb.location.getWorld().playSound(orb.location, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 0.5f, 0.7f);
    }

    /**
     * Marks the orb consumed, cancels its tasks, removes the stand.
     */
    private void consumeOrb(SoulOrb orb) {
        if (orb.consumed) return;
        orb.consumed = true;

        if (orb.ambientTask  != null) { try { orb.ambientTask.cancel();  } catch (IllegalStateException ignored) {} }
        if (orb.lifetimeTask != null) { try { orb.lifetimeTask.cancel(); } catch (IllegalStateException ignored) {} }

        if (orb.stand.isValid()) orb.stand.remove();

        List<SoulOrb> list = playerOrbs.get(orb.killerUUID);
        if (list != null) list.remove(orb);
    }

    /**
     * Finds the nearest valid, unconsumed Soul Orb within interact range.
     */
    private SoulOrb findNearestOrb(Player player) {
        List<SoulOrb> orbs = playerOrbs.get(player.getUniqueId());
        if (orbs == null || orbs.isEmpty()) return null;

        SoulOrb nearest  = null;
        double  bestDist = Double.MAX_VALUE;

        for (SoulOrb orb : orbs) {
            if (orb.consumed) continue;
            if (!orb.location.getWorld().equals(player.getWorld())) continue;
            double dist = orb.location.distanceSquared(player.getLocation());
            if (dist <= ORB_INTERACT_RADIUS * ORB_INTERACT_RADIUS && dist < bestDist) {
                bestDist = dist;
                nearest  = orb;
            }
        }
        return nearest;
    }

    private void removeAllOrbs(UUID uuid) {
        List<SoulOrb> orbs = playerOrbs.remove(uuid);
        if (orbs == null) return;
        for (SoulOrb orb : orbs) consumeOrb(orb);
    }

    // ═══════════════════════════════════════════════════════════════
    //  DEVOUR  (Right-Click) — Rapid heal
    // ═══════════════════════════════════════════════════════════════

    private void activateDevour(Player player, SoulOrb orb) {
        Location orbLoc = orb.location.clone();
        World world = orbLoc.getWorld();

        spawnOrbConsumeBurst(orbLoc, true);
        consumeOrb(orb);

        world.playSound(orbLoc, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.8f, 0.7f);
        world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.8f);

        player.sendMessage(CC.translate("&dDevour &7— &aRapidly healing!"));

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.REGENERATION,
                DEVOUR_DURATION_TICKS,
                DEVOUR_REGEN_AMPLIFIER,
                false, false, true));

        // Soul stream from orb -> Reyna
        new BukkitRunnable() {
            double progress = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                Location target = player.getLocation().clone().add(0, 1.0, 0);

                Vector direction = target.toVector().subtract(orbLoc.toVector());
                double distance = direction.length();

                if (distance < 0.1) {
                    cancel();
                    return;
                }

                direction.normalize();

                // Move the stream towards the player
                progress += 0.15;

                if (progress >= distance) {
                    cancel();
                    return;
                }

                Location point = orbLoc.clone().add(direction.clone().multiply(progress));

                // Subtle purple soul
                world.spawnParticle(
                        Particle.DUST,
                        point,
                        1,
                        0, 0, 0,
                        0,
                        new Particle.DustOptions(Color.fromRGB(180, 60, 255), 1.0f)
                );

                // Occasional red accent
                if (Math.random() < 0.35) {
                    world.spawnParticle(
                            Particle.DUST,
                            point,
                            1,
                            0, 0, 0,
                            0,
                            new Particle.DustOptions(Color.fromRGB(255, 40, 80), 0.8f)
                    );
                }
            }
        }.runTaskTimer(SoupPvP.getInstance(), 0L, 1L);

        // Optional subtle healing aura on Reyna
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (!player.isOnline() || tick >= DEVOUR_DURATION_TICKS) {
                    cancel();
                    return;
                }

                Location loc = player.getLocation().add(0, 1, 0);

                world.spawnParticle(
                        Particle.DUST,
                        loc,
                        2,
                        0.15, 0.25, 0.15,
                        0,
                        new Particle.DustOptions(Color.fromRGB(180, 60, 255), 0.8f)
                );

                tick++;
            }
        }.runTaskTimer(SoupPvP.getInstance(), 0L, 2L);
    }

    // ═══════════════════════════════════════════════════════════════
    //  DISMISS  (Left-Click) — Invisibility + speed dash
    // ═══════════════════════════════════════════════════════════════

    private void activateDismiss(Player player, SoulOrb orb) {
        Location orbLoc = orb.location;
        World    world  = orbLoc.getWorld();

        spawnOrbConsumeBurst(orbLoc, false);
        consumeOrb(orb);

        world.playSound(orbLoc,           Sound.ENTITY_ENDERMAN_TELEPORT, 0.9f, 1.2f);
        world.playSound(player.getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 0.5f, 0.8f);

        player.sendMessage(CC.translate("&5Dismiss &7— &fPhasing out!"));

        // Invisibility + Speed I for the duration
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY,
                DISMISS_DURATION_TICKS,
                200, false, false, false));  // no icon — authentic stealth feel
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                DISMISS_DURATION_TICKS,
                1, false, false, true));

        // Brief dismiss smoke-out on Reyna herself
        spawnDismissBurst(player.getLocation().clone().add(0, 1, 0));

        // Trail of faint purple wisps while invisible
        BukkitRunnable dismissTrail = new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (!player.isOnline() || tick >= DISMISS_DURATION_TICKS) { cancel(); return; }
                if (tick % 4 == 0) spawnDismissTrail(player.getLocation().clone().add(0, 1, 0));
                tick++;
            }
        };
        dismissTrail.runTaskTimer(SoupPvP.getInstance(), 0L, 1L);

        // Re-appear burst when Dismiss ends
        Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
            if (!player.isOnline()) return;
            spawnReappearBurst(player.getLocation().clone().add(0, 1, 0));
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.5f);
            player.sendMessage(CC.translate("&5Dismiss &7faded."));
        }, DISMISS_DURATION_TICKS);
    }

    // ═══════════════════════════════════════════════════════════════
    //  PARTICLES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Pulsing, rotating Soul Orb ambient particles.
     * Uses ENDER_EYE-style purple dust with a rotating ring + upward wisps.
     */
    private void spawnOrbAmbient(Location centre, int tick) {
        World world = centre.getWorld();
        if (world == null) return;

        // ── Rotating purple ring ──
        double offset = tick * 0.12;
        int    steps  = 12;
        double step   = (2 * Math.PI) / steps;

        Particle.DustOptions outer = new Particle.DustOptions(Color.fromRGB(140, 30, 200), 1.0f);
        Particle.DustOptions mid   = new Particle.DustOptions(Color.fromRGB(180, 80, 230), 0.85f);
        Particle.DustOptions core  = new Particle.DustOptions(Color.fromRGB(220, 160, 255), 0.7f);

        double radius = 0.35;

        for (int i = 0; i < steps; i++) {
            double angle = step * i + offset;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            world.spawnParticle(Particle.DUST, centre.clone().add(x, 0, z), 1, 0, 0, 0, 0, outer);

            // Counter-rotating inner ring
            double a2 = step * i - offset * 0.8;
            double x2 = (radius * 0.5) * Math.cos(a2);
            double z2 = (radius * 0.5) * Math.sin(a2);
            world.spawnParticle(Particle.DUST, centre.clone().add(x2, 0, z2), 1, 0, 0, 0, 0, mid);
        }

        // ── Vertical wisp rising from orb every 3 ticks ──
        if (tick % 3 == 0) {
            world.spawnParticle(Particle.DUST, centre.clone().add(0, 0.05, 0),
                    1, 0.05, 0.1, 0.05, 0, core);
            world.spawnParticle(Particle.WITCH, centre, 1, 0.1, 0.15, 0.1, 0.02);
        }

        // ── Ender-eye flicker every 10 ticks ──
        if (tick % 10 == 0) {
            world.spawnParticle(Particle.PORTAL, centre, 4, 0.1, 0.2, 0.1, 0.05);
        }

        // ── Slow vertical bob: offset the stand slightly ──
        // (purely cosmetic — the stand is a marker so this has no hitbox impact)
        double bob = Math.sin(tick * 0.08) * 0.04;
        if (tick % 2 == 0 && centre.getWorld() != null) {
            world.spawnParticle(Particle.DUST,
                    centre.clone().add(0, bob, 0), 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.fromRGB(200, 100, 255), 1.1f));
        }
    }

    /**
     * Big burst when a Soul Orb spawns above the killed player.
     */
    private void spawnOrbSpawnBurst(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Particle.DustOptions burst  = new Particle.DustOptions(Color.fromRGB(150, 20, 220), 1.5f);
        Particle.DustOptions bright = new Particle.DustOptions(Color.fromRGB(220, 140, 255), 1.1f);

        world.spawnParticle(Particle.DUST,   loc, 18, 0.5, 0.4, 0.5, 0, burst);
        world.spawnParticle(Particle.DUST,   loc,  8, 0.2, 0.2, 0.2, 0, bright);
        world.spawnParticle(Particle.PORTAL, loc, 20, 0.3, 0.3, 0.3, 0.2);
        world.spawnParticle(Particle.WITCH,  loc,  6, 0.3, 0.3, 0.3, 0.05);
        world.spawnParticle(Particle.FLASH,  loc,  1, 0,   0,   0,   0);
    }

    /**
     * Burst when the orb is consumed by Devour or Dismiss.
     * @param devour true = warm purple/red (Devour); false = cold pale (Dismiss)
     */
    private void spawnOrbConsumeBurst(Location loc, boolean devour) {
        World world = loc.getWorld();
        if (world == null) return;

        Color c1 = devour ? Color.fromRGB(180, 20, 200) : Color.fromRGB(200, 160, 255);
        Color c2 = devour ? Color.fromRGB(255, 60, 255) : Color.fromRGB(230, 230, 255);

        Particle.DustOptions d1 = new Particle.DustOptions(c1, 1.4f);
        Particle.DustOptions d2 = new Particle.DustOptions(c2, 1.0f);

        world.spawnParticle(Particle.DUST,   loc, 16, 0.4, 0.4, 0.4, 0, d1);
        world.spawnParticle(Particle.DUST,   loc,  8, 0.2, 0.2, 0.2, 0, d2);
        world.spawnParticle(Particle.PORTAL, loc, 25, 0.3, 0.3, 0.3, 0.3);
        world.spawnParticle(Particle.FLASH,  loc,  1, 0,   0,   0,   0);
    }

    /**
     * Fades the orb when it expires naturally.
     */
    private void spawnOrbFadeBurst(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Particle.DustOptions fade = new Particle.DustOptions(Color.fromRGB(80, 10, 120), 0.9f);
        world.spawnParticle(Particle.DUST,    loc, 8, 0.3, 0.2, 0.3, 0, fade);
        world.spawnParticle(Particle.END_ROD, loc, 3, 0.2, 0.2, 0.2, 0.03);
    }

    /**
     * Purple heart/healing wisps rising during Devour.
     */
    private void spawnDevourHealTick(Location loc, int tick) {
        World world = loc.getWorld();
        if (world == null) return;

        Particle.DustOptions heal = new Particle.DustOptions(Color.fromRGB(200, 80, 255), 1.0f);
        Particle.DustOptions rose = new Particle.DustOptions(Color.fromRGB(255, 120, 200), 0.85f);

        world.spawnParticle(Particle.DUST,  loc, 2, 0.2, 0.3, 0.2, 0, heal);
        world.spawnParticle(Particle.WITCH, loc, 1, 0.1, 0.2, 0.1, 0.01);

        if (tick % 8 == 0) {
            world.spawnParticle(Particle.DUST, loc.clone().add(0, 0.3, 0), 3, 0.15, 0.1, 0.15, 0, rose);
        }
    }

    /**
     * Cold purple smoke on activation of Dismiss — Reyna phases out.
     */
    private void spawnDismissBurst(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Particle.DustOptions cold   = new Particle.DustOptions(Color.fromRGB(190, 180, 255), 1.2f);
        Particle.DustOptions shadow = new Particle.DustOptions(Color.fromRGB(60,  40, 120),  1.0f);

        world.spawnParticle(Particle.DUST,           loc, 20, 0.4, 0.5, 0.4, 0, cold);
        world.spawnParticle(Particle.DUST,           loc,  8, 0.2, 0.3, 0.2, 0, shadow);
        world.spawnParticle(Particle.REVERSE_PORTAL, loc, 15, 0.3, 0.5, 0.3, 0.05);
        world.spawnParticle(Particle.FLASH,          loc,  1, 0,   0,   0,   0);
    }

    /**
     * Faint trail particles during Dismiss invisibility.
     */
    private void spawnDismissTrail(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Particle.DustOptions ghost = new Particle.DustOptions(Color.fromRGB(170, 150, 255), 0.7f);
        world.spawnParticle(Particle.DUST,           loc, 2, 0.1, 0.15, 0.1, 0, ghost);
        world.spawnParticle(Particle.REVERSE_PORTAL, loc, 1, 0.1, 0.1,  0.1, 0.01);
    }

    /**
     * Re-materialise burst when Dismiss expires.
     */
    private void spawnReappearBurst(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Particle.DustOptions flash = new Particle.DustOptions(Color.fromRGB(220, 200, 255), 1.3f);
        Particle.DustOptions deep  = new Particle.DustOptions(Color.fromRGB(120, 40, 200),  1.0f);

        world.spawnParticle(Particle.DUST,   loc, 16, 0.4, 0.5, 0.4, 0, flash);
        world.spawnParticle(Particle.DUST,   loc,  6, 0.2, 0.2, 0.2, 0, deep);
        world.spawnParticle(Particle.PORTAL, loc, 20, 0.3, 0.4, 0.3, 0.15);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════

    private ItemStack buildSoulConsumerItem() {
        return new ItemBuilder(Material.PURPLE_DYE)
                .name(CC.translate("&5Soul Consumer &7[L-Click: &5Dismiss&7 | R-Click: &dDevour&7]"))
                .build();
    }

    private boolean isReyna(Player player) {
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Reyna");
        return getProfile(player).getCurrentKit().equals(kit);
    }

    private Profile getProfile(Player player) {
        return SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
    }
}