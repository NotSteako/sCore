package kami.gg.souppvp.kit.valorant;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.KitCategory;
import kami.gg.souppvp.kit.KitRarity;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.timer.Timer;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.DurationFormatter;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.PlayerUtil;
import kami.gg.souppvp.util.XPBarTimer;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * JettKit — based on Jett from VALORANT.
 *
 * Abilities:
 *  • UPDRAFT  (Feather)     — Right-click to launch straight up into the air.
 *                             3 charges, each recharges every 8 s. No fall damage while
 *                             the immunity window is active.
 *
 *  • TAILWIND (Packed Ice)  — Right-click to dash in the direction you are moving
 *                             (or forward if standing still). 12 s cooldown shown on XP bar.
 *
 * Passive: Speed II, no fall damage for 3 s after any ability activation.
 */
public class JettKit extends Kit {

    // ── Tuning constants ───────────────────────────────────────────
    private static final int    UPDRAFT_MAX_CHARGES      = 3;
    private static final int    UPDRAFT_RECHARGE_SECONDS = 8;
    private static final double UPDRAFT_POWER            = 1.15;   // vertical boost

    private static final int    TAILWIND_COOLDOWN        = 12;     // seconds
    private static final double TAILWIND_POWER           = 1.6;    // horizontal sprint

    private static final long   NO_FALL_TICKS            = 80L;    // 4 s

    // ── State ──────────────────────────────────────────────────────
    private final Map<UUID, Integer> updraftCharges  = new HashMap<>();
    private final Set<UUID>          recharging      = new HashSet<>();
    private final Map<UUID, Long> noFall = new HashMap<>();

    // ── Kit metadata ───────────────────────────────────────────────
    @Override public String     getName()       { return "Jett"; }
    @Override public KitRarity  getRarityType() { return KitRarity.ULTIMATE; }
    @Override public Integer    getPrice()      { return getRarityType().getPrice(); }


    @Override
    public KitCategory getCategory() {
        return KitCategory.VALORANT;
    }

    @Override
    public ItemStack getIcon() {
        // Base64 value from minecraft-heads.com #55320 (Jett)
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2MxMzYyMDk4ZmVjYjAxZGQ4MzNiZGRlMzZkZTgwOGRmMzkwMTBjMjc1ZjE4YmJiY2NmYzhkYjY4NTkzZmZlMSJ9fX0=";

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta  = (SkullMeta) skull.getItemMeta();

        // Use a stable UUID derived from the base64 value so Bukkit can cache the profile correctly.
        // UUID.randomUUID() causes the texture to appear as a generic head each time.
        PlayerProfile profile = Bukkit.createPlayerProfile(
                UUID.nameUUIDFromBytes(base64.getBytes())
        );
        PlayerTextures textures = profile.getTextures();
        try {
            textures.setSkin(new URL(
                    "http://textures.minecraft.net/texture/cc1362098fecb01dd833bdde36de808df39010c275f18bbbccfc8db68593ffe1"
            ));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        profile.setTextures(textures);
        meta.setOwnerProfile(profile);
        skull.setItemMeta(meta);
        return skull;
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
                "&7&l[Feather]&7 Right-click to Updraft — launch straight up!",
                "&7(" + UPDRAFT_MAX_CHARGES + " charges, recharge every " + UPDRAFT_RECHARGE_SECONDS + "s)",
                "&7&l[Packed Ice]&7 Right-click to Tailwind — dash forward!",
                "&7(" + TAILWIND_COOLDOWN + "s cooldown)"
        );
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        return Arrays.asList(
                new ItemBuilder(Material.IRON_SWORD)
                        .enchantment(Enchantment.SHARPNESS, 2)
                        .enchantment(Enchantment.UNBREAKING, 1)
                        .build(),
                buildUpdraftFeather(UPDRAFT_MAX_CHARGES),
                buildTailwindBlock()
        );
    }

    @Override
    public ItemStack[] getArmor(Player player) {
        return new ItemStack[]{
                new ItemBuilder(Material.LEATHER_BOOTS)
                        .color(Color.fromRGB(100, 180, 230))   // Jett teal-blue
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.FEATHER_FALLING, 2)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                new ItemBuilder(Material.LEATHER_LEGGINGS)
                        .color(Color.WHITE)
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE)
                        .color(Color.fromRGB(100, 180, 230))
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                new ItemBuilder(Material.LEATHER_HELMET)
                        .color(Color.WHITE)
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build()
        };
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        return Arrays.asList(
                new PotionEffect(PotionEffectType.SPEED,      Integer.MAX_VALUE, 1),  // Speed II passive
                new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 0)   // slight extra jump
        );
    }

    @Override
    public void onSelect(Player player) {
        updraftCharges.put(player.getUniqueId(), UPDRAFT_MAX_CHARGES);
        refreshFeatherInInventory(player, UPDRAFT_MAX_CHARGES);
    }

    // ══════════════════════════════════════════════════════════════
    //  ABILITY HANDLER
    // ══════════════════════════════════════════════════════════════

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isJett(player)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Profile profile = getProfile(player);
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;

        ItemStack held = player.getItemInHand();
        if (held == null) return;

        if (held.getType() == Material.FEATHER && isUpdraftFeather(held)) {
            event.setCancelled(true);
            handleUpdraft(player);
            return;
        }

        if (held.getType() == Material.PACKED_ICE && isTailwindBlock(held)) {
            event.setCancelled(true);
            handleTailwind(player);
        }
    }

    // ── UPDRAFT ────────────────────────────────────────────────────

    private void handleUpdraft(Player player) {
        UUID uuid    = player.getUniqueId();
        int  charges = updraftCharges.getOrDefault(uuid, UPDRAFT_MAX_CHARGES);

        if (charges <= 0) {
            player.sendMessage(CC.translate("&bUpdraft charges empty! Recharging..."));
            return;
        }

        int newCharges = charges - 1;
        updraftCharges.put(uuid, newCharges);
        refreshFeatherInInventory(player, newCharges);

        // Launch straight up
        Vector vel = player.getVelocity();
        vel.setY(UPDRAFT_POWER);
        player.setVelocity(vel);
        player.setFallDistance(0f);
        grantNoFall(player);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 0.9f, 1.3f);
        PlayerUtil.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        sendUpdraftMessage(player, newCharges);
        startRechargeIfNeeded(player);
    }

    // ── TAILWIND (DASH) ────────────────────────────────────────────

    private void handleTailwind(Player player) {
        UUID uuid = player.getUniqueId();
        String timerKey = "Jett Tailwind";

        if (SoupPvP.getInstance().getTimersHandler().hasTimer(uuid, timerKey, true)) {
            player.sendMessage(CC.translate("&bTailwind &7is on cooldown for another &b"
                    + DurationFormatter.getRemaining(
                    SoupPvP.getInstance().getTimersHandler().getRemaining(uuid, timerKey, true), true)
                    + "&7."));
            return;
        }

        // Dash direction: movement direction if sprinting, otherwise facing direction
// Always start from the facing direction as a safe baseline
        Vector direction = player.getLocation().getDirection().setY(0);

// If the player has real horizontal momentum, blend that in instead
        if (player.isSprinting()) {
            Vector vel = player.getVelocity().setY(0);
            if (vel.lengthSquared() > 0.001) {
                direction = vel;
            }
        }

// Final safety guard — if still zero (e.g. looking straight up/down edge case), abort
        if (direction.lengthSquared() < 0.001) {
            player.sendMessage(CC.translate("&bCan't dash right now!"));
            return;
        }

        direction.normalize();

        Vector dash = direction.multiply(TAILWIND_POWER);
        dash.setY(0.35);   // tiny upward kick so it feels like a hop-dash
        player.setVelocity(dash);
        player.setFallDistance(0f);
        grantNoFall(player);

        // Apply cooldown + XP bar
        SoupPvP.getInstance().getTimersHandler().addPlayerTimer(
                uuid,
                new Timer(timerKey, TimeUnit.SECONDS.toMillis(TAILWIND_COOLDOWN)),
                true
        );
        XPBarTimer.runXpBar(player, TAILWIND_COOLDOWN);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_SHOOT, 1f, 1.5f);
        PlayerUtil.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT);
        player.sendMessage(CC.translate("&bTailwind! &7Dashing..."));
    }

    // ── Fall damage cancellation ───────────────────────────────────


    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!isJett(player)) return;

        Long expiry = noFall.get(player.getUniqueId());
        if (expiry == null) return;

        if (System.currentTimeMillis() <= expiry) {
            noFall.remove(player.getUniqueId()); // consume
            event.setCancelled(true);
            player.setFallDistance(0f);
        } else {
            noFall.remove(player.getUniqueId());
        }
    }

    // ── Updraft charge recharger ───────────────────────────────────

    private void startRechargeIfNeeded(Player player) {
        UUID uuid = player.getUniqueId();
        if (recharging.contains(uuid)) return;
        recharging.add(uuid);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isJett(player)) {
                    recharging.remove(uuid);
                    cancel();
                    return;
                }

                int current = updraftCharges.getOrDefault(uuid, UPDRAFT_MAX_CHARGES);
                if (current >= UPDRAFT_MAX_CHARGES) {
                    recharging.remove(uuid);
                    cancel();
                    return;
                }

                int restored = current + 1;
                updraftCharges.put(uuid, restored);
                refreshFeatherInInventory(player, restored);
                sendUpdraftMessage(player, restored);

                if (restored >= UPDRAFT_MAX_CHARGES) {
                    recharging.remove(uuid);
                    cancel();
                }
            }
        }.runTaskTimer(SoupPvP.getInstance(),
                UPDRAFT_RECHARGE_SECONDS * 20L,
                UPDRAFT_RECHARGE_SECONDS * 20L);
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════

    private void grantNoFall(Player player) {
        noFall.put(
                player.getUniqueId(),
                System.currentTimeMillis() + (NO_FALL_TICKS * 50L)
        );
    }

    // ── Item builders ──────────────────────────────────────────────

    private ItemStack buildUpdraftFeather(int charges) {
        return new ItemBuilder(Material.FEATHER)
                .name(CC.translate("&bUpdraft " + buildChargeBar(charges)))
                .build();
    }

    private ItemStack buildTailwindBlock() {
        return new ItemBuilder(Material.PACKED_ICE)
                .name(CC.translate("&bTailwind &7[Dash]"))
                .build();
    }

    private String buildChargeBar(int charges) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < UPDRAFT_MAX_CHARGES; i++) {
            sb.append(i < charges ? "&b■" : "&8■");
        }
        return sb.toString();
    }

    // ── Inventory update helpers ───────────────────────────────────

    /** Replace whichever feather is in the inventory with the freshly built one. */
    private void refreshFeatherInInventory(Player player, int charges) {
        ItemStack newFeather = buildUpdraftFeather(charges);
        ItemStack held       = player.getItemInHand();

        if (held != null && held.getType() == Material.FEATHER && isUpdraftFeather(held)) {
            player.setItemInHand(newFeather);
            player.updateInventory();
            return;
        }

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == Material.FEATHER && isUpdraftFeather(item)) {
                player.getInventory().setItem(i, newFeather);
                player.updateInventory();
                return;
            }
        }
    }

    // ── Item identity checks ───────────────────────────────────────

    private boolean isUpdraftFeather(ItemStack item) {
        if (!item.hasItemMeta() || item.getItemMeta().getDisplayName() == null) return false;
        return item.getItemMeta().getDisplayName().contains("Updraft");
    }

    private boolean isTailwindBlock(ItemStack item) {
        if (!item.hasItemMeta() || item.getItemMeta().getDisplayName() == null) return false;
        return item.getItemMeta().getDisplayName().contains("Tailwind");
    }

    // ── Messages ───────────────────────────────────────────────────

    private void sendUpdraftMessage(Player player, int charges) {
        player.sendActionBar(CC.translate("&bUpdraft: " + buildChargeBar(charges)
                + " &7(" + charges + "/" + UPDRAFT_MAX_CHARGES + ")"));
    }


    // ── Profile / kit guards ───────────────────────────────────────

    private boolean isJett(Player player) {
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Jett");
        return getProfile(player).getCurrentKit().equals(kit);
    }

    private Profile getProfile(Player player) {
        return SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
    }

}