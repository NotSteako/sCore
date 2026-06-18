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
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
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

public class ReaperKit extends Kit implements Listener {

    private static final int    SHADOW_STEP_COOLDOWN = 40;
    private static final double SHADOW_STEP_RANGE    = 20.0;
    private static final int    RING_PARTICLES       = 28;
    private static final double RING_RADIUS          = 1.1;
    private static final int    IDLE_TICK_LIMIT      = 30;

    // Players with the active aiming ticker running
    private final Map<UUID, BukkitRunnable> aimTasks    = new HashMap<>();
    private final Map<UUID, Double>         ringOffsets = new HashMap<>();
    // Debounce right-click
    private final Map<UUID, Long>           lastClick   = new HashMap<>();
    // Idle detection for the aim ring
    private final Map<UUID, Location>       lastLookLoc = new HashMap<>();
    private final Map<UUID, Integer>        idleTicks   = new HashMap<>();

    @Override public String getName()          { return "Reaper"; }
    @Override public KitRarity getRarityType() { return KitRarity.ULTIMATE; }
    @Override public Integer getPrice()        { return getRarityType().getPrice(); }

    @Override
    public KitCategory getCategory() {
        return KitCategory.OVERWATCH;
    }

    // ── Cosmetic registration ───────────────────────────────────────

    private static final CosmeticSkin DEFAULT = new CosmeticSkin(
            "default",
            "&aDefault Reaper",
            "9998a226568d45faf77785890564a897aa58d151a5303989732d9f5ea0aae6c3"
    );

    private static final CosmeticSkin RAT_KING = new CosmeticSkin(
            "ratking",
            "&6Rat King Reaper",
            "3f25c66697649ec86eeebdd8b264613ffda00708810f1b2d06e8e404c7362eb6"
    );

    @Override
    public List<CosmeticSkin> getAvailableCosmetics() {
        return Arrays.asList(
                DEFAULT,
                RAT_KING
        );
    }

    @Override
    public CosmeticSkin getDefaultCosmetic() {
        return DEFAULT;
    }

    @Override
    public ItemStack getIcon() {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        SkinApplier.apply(meta, DEFAULT); // menu icon always shows default
        skull.setItemMeta(meta);
        return skull;
    }

    @Override
    public List<String> getDescription() {
        List<String> desc = new ArrayList<>();
        desc.add("&7Hold &8Ink Sac &7to aim &5Shadow Step&7.");
        desc.add("&7A &5ring&7 shows your destination up to &e" + (int) SHADOW_STEP_RANGE + " blocks&7 away.");
        desc.add("&7Right-click to teleport there instantly.");
        desc.add("&7Cooldown: &e" + SHADOW_STEP_COOLDOWN + "s");
        return desc;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> items = new ArrayList<>();
        items.add(new ItemBuilder(Material.IRON_SWORD)
                .enchantment(Enchantment.SHARPNESS, 1)
                .enchantment(Enchantment.UNBREAKING, 3)
                .build());
        items.add(buildInkSac());
        return items;
    }

    @Override
    public ItemStack[] getArmor(Player player) {

        ItemStack helmet = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) helmet.getItemMeta();

        CosmeticSkin skin = getDefaultCosmetic();

        if (player != null) {
            Profile profile = getProfile(player);

            String selectedId = profile.getSelectedCosmetic(getName());

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
                        new NamespacedKey(SoupPvP.getInstance(), "reaper_helmet_armor"),
                        4.0,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HEAD.getGroup()  // <-- EquipmentSlotGroup, not EquipmentSlot
                )
        );

        meta.addAttributeModifier(
                Attribute.ARMOR_TOUGHNESS,
                new AttributeModifier(
                        new NamespacedKey(SoupPvP.getInstance(), "reaper_helmet_toughness"),
                        1.0,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HEAD.getGroup()
                )
        );

        helmet.setItemMeta(meta);

        ItemMeta itemMeta = helmet.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        helmet.setItemMeta(itemMeta);

        return new ItemStack[]{
                new ItemBuilder(Material.LEATHER_BOOTS)
                        .color(Color.fromRGB(20, 0, 30))
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),

                new ItemBuilder(Material.LEATHER_LEGGINGS)
                        .color(Color.BLACK)
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),

                new ItemBuilder(Material.LEATHER_CHESTPLATE)
                        .color(Color.fromRGB(20, 0, 30))
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),

                helmet
        };
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        List<PotionEffect> effects = new ArrayList<>();
        effects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
        return effects;
    }

    @Override
    public void onSelect(Player player) {
        startAimTicker(player);
    }

    // ── Start aim ticker when player switches TO the ink sac ───────
    @EventHandler
    public void onHoldInkSac(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!isReaper(player)) return;

        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());

        // Switched TO ink sac — start ticker
        if (newItem != null && newItem.getType() == Material.INK_SAC) {
            startAimTicker(player);
        }

        // Switched AWAY from ink sac — stop ticker
        if (oldItem != null && oldItem.getType() == Material.INK_SAC) {
            stopAimTicker(player.getUniqueId());
        }
    }

    // ── Right-click to commit the teleport ─────────────────────────
    @EventHandler(priority = EventPriority.LOWEST)
    public void onShadowStep(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() != Material.INK_SAC) return;

        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setCancelled(true);

        if (!isReaper(player)) return;

        Profile profile = getProfile(player);
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;

        UUID uuid = player.getUniqueId();

        // Debounce
        long now = System.currentTimeMillis();
        if (now - lastClick.getOrDefault(uuid, 0L) < 200) return;
        lastClick.put(uuid, now);

        // Cooldown check
        if (SoupPvP.getInstance().getTimersHandler().getRemaining(uuid, "Shadow Step", true) > 0) {
            player.sendMessage(CC.translate("&5Shadow Step &7is on cooldown for another &e"
                    + DurationFormatter.getRemaining(
                    SoupPvP.getInstance().getTimersHandler().getRemaining(uuid, "Shadow Step", true), true)
                    + "&7."));
            return;
        }

        Location destination = resolveTeleportDestination(player);
        if (destination == null) {
            player.sendMessage(CC.translate("&5Shadow Step&7: &cNo clear path found!"));
            return;
        }

        performTeleport(player, destination);
    }

    // ── Aim ticker: runs every 2 ticks while holding ink sac ───────
    // Ray-traces each frame and draws the ring at the result.
    private void startAimTicker(Player player) {
        UUID uuid = player.getUniqueId();
        stopAimTicker(uuid); // clear any old one first
        ringOffsets.put(uuid, 0.0);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isReaper(player)) {
                    stopAimTicker(uuid);
                    cancel();
                    return;
                }

                ItemStack held = player.getInventory().getItemInMainHand();
                if (held.getType() != Material.INK_SAC) {
                    stopAimTicker(uuid);
                    cancel();
                    return;
                }

                Profile profile = getProfile(player);
                if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;

                // ✅ NEW: don't render particles while on cooldown
                if (SoupPvP.getInstance().getTimersHandler().getRemaining(uuid, "Shadow Step", true) > 0) {
                    return;
                }

                // ✅ NEW: stop rendering if player is idle (no movement/look change) for too long
                if (isIdle(player)) {
                    return;
                }

                Location dest = resolveTeleportDestination(player);
                if (dest == null) return;

                spawnDestinationRing(uuid, dest);
            }
        };

        aimTasks.put(uuid, task);
        task.runTaskTimer(SoupPvP.getInstance(), 0L, 2L);
    }

    private void stopAimTicker(UUID uuid) {
        BukkitRunnable existing = aimTasks.remove(uuid);
        if (existing != null) {
            try { existing.cancel(); } catch (IllegalStateException ignored) {}
        }
        ringOffsets.remove(uuid);
        lastLookLoc.remove(uuid);
        idleTicks.remove(uuid);
    }

    // ── Ray-trace ─────────────────────────────────────────────────
    private Location resolveTeleportDestination(Player player) {
        Location eye = player.getEyeLocation();
        Vector   dir = eye.getDirection().normalize();

        RayTraceResult result = player.getWorld().rayTraceBlocks(
                eye, dir, SHADOW_STEP_RANGE, FluidCollisionMode.NEVER, true);

        Location endPoint;
        if (result != null && result.getHitBlock() != null) {
            endPoint = result.getHitPosition().toLocation(player.getWorld())
                    .add(dir.clone().multiply(-0.6));
        } else {
            endPoint = eye.clone().add(dir.clone().multiply(SHADOW_STEP_RANGE));
        }

        Location dest = endPoint.clone();

        Block blockAt = dest.getBlock();
        Block blockAbove = dest.clone().add(0, 1, 0).getBlock();

        if (!blockAt.getType().isAir() || !blockAbove.getType().isAir()) {
            return null;
        }

        dest.add(0.5, 0.0, 0.5);

        if (!dest.getBlock().getType().isAir()
                || !dest.clone().add(0, 1, 0).getBlock().getType().isAir()) {
            return null;
        }

        // ✅ NEW: reject teleports outside the world border
        if (!player.getWorld().getWorldBorder().isInside(dest)) {
            return null;
        }

        dest.setYaw(player.getLocation().getYaw());
        dest.setPitch(player.getLocation().getPitch());
        return dest;
    }

    // ── Teleport ──────────────────────────────────────────────────
    private void performTeleport(Player player, Location destination) {
        UUID uuid = player.getUniqueId();

        player.getWorld().spawnParticle(Particle.SQUID_INK,
                player.getLocation().clone().add(0, 1, 0), 25, 0.35, 0.5, 0.35, 0.04);
        PlayerUtil.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT);

        player.teleport(destination);

        player.getWorld().spawnParticle(Particle.SQUID_INK,
                destination.clone().add(0, 1, 0), 25, 0.35, 0.5, 0.35, 0.04);
        player.getWorld().playSound(destination, Sound.ENTITY_WITHER_SHOOT, 0.6f, 1.8f);
        player.sendMessage(CC.translate("&5Shadow Step &7— vanished!"));

        SoupPvP.getInstance().getTimersHandler().addPlayerTimer(
                uuid,
                new Timer("Shadow Step", TimeUnit.SECONDS.toMillis(SHADOW_STEP_COOLDOWN)),
                true);
        XPBarTimer.runXpBar(player, SHADOW_STEP_COOLDOWN);
    }

    // ── Ring particles ─────────────────────────────────────────────
    private void spawnDestinationRing(UUID uuid, Location center) {
        World world = center.getWorld();
        if (world == null) return;

        double offset = ringOffsets.getOrDefault(uuid, 0.0);
        double step   = (2 * Math.PI) / RING_PARTICLES;

        // Inner ring — SQUID_INK
        for (int i = 0; i < RING_PARTICLES; i++) {
            double angle = step * i + offset;
            double x = RING_RADIUS * Math.cos(angle);
            double z = RING_RADIUS * Math.sin(angle);
            world.spawnParticle(Particle.SQUID_INK, center.clone().add(x, 0.05, z), 1, 0, 0, 0, 0);
        }

        // Outer ring — SPELL_WITCH (purple)
        for (int i = 0; i < RING_PARTICLES / 2; i++) {
            double angle = step * 2 * i - offset;
            double x = (RING_RADIUS + 0.4) * Math.cos(angle);
            double z = (RING_RADIUS + 0.4) * Math.sin(angle);
            world.spawnParticle(Particle.WITCH, center.clone().add(x, 0.1, z), 1, 0, 0, 0, 0);
        }

        ringOffsets.put(uuid, offset + 0.22);
    }

    // ── Idle detection ───────────────────────────────────────────
    // Returns true if the player hasn't moved or looked around for IDLE_TICK_LIMIT ticks.
    private boolean isIdle(Player player) {
        UUID uuid = player.getUniqueId();
        Location current = player.getLocation();

        Location last = lastLookLoc.get(uuid);
        if (last == null) {
            lastLookLoc.put(uuid, current.clone());
            idleTicks.put(uuid, 0);
            return false;
        }

        boolean moved = last.getX() != current.getX()
                || last.getY() != current.getY()
                || last.getZ() != current.getZ()
                || Math.abs(last.getYaw() - current.getYaw()) > 0.5f
                || Math.abs(last.getPitch() - current.getPitch()) > 0.5f;

        if (moved) {
            lastLookLoc.put(uuid, current.clone());
            idleTicks.put(uuid, 0);
            return false;
        }

        int ticks = idleTicks.getOrDefault(uuid, 0) + 2; // ticker runs every 2 ticks
        idleTicks.put(uuid, ticks);
        return ticks >= IDLE_TICK_LIMIT;
    }

    // ── Helpers ────────────────────────────────────────────────────
    private ItemStack buildInkSac() {
        return new ItemBuilder(Material.INK_SAC)
                .name(CC.translate("&5Shadow Step &7[Right-Click]"))
                .build();
    }

    private boolean isReaper(Player player) {
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Reaper");
        return getProfile(player).getCurrentKit().equals(kit);
    }

    private Profile getProfile(Player player) {
        return SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
    }
}