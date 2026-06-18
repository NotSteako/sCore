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
import kami.gg.souppvp.util.PlayerUtil;
import kami.gg.souppvp.util.XPBarTimer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
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

public class SpidermanKit extends Kit {

    private static final int MAX_CHARGES      = 3;
    private static final int RECHARGE_SECONDS = 6;
    private static final int PULLER_COOLDOWN  = 12;

    // UUID → remaining swing charges
    private final Map<UUID, Integer>  swingCharges  = new HashMap<>();
    // UUIDs currently running a recharge ticker
    private final Set<UUID>           recharging    = new HashSet<>();
    // Fall-damage immunity after a swing
    private final Set<UUID>           swingNoFall   = new HashSet<>();
    // Active puller bobbers: bobber entity UUID → caster UUID
    private final Map<UUID, UUID>     pullerBobbers = new HashMap<>();

    // ── Kit metadata ───────────────────────────────────────────────
    @Override public String getName()          { return "Spiderman"; }
    @Override public KitRarity getRarityType() { return KitRarity.ULTIMATE; }
    @Override public Integer getPrice()        { return getRarityType().getPrice(); }

    @Override
    public KitCategory getCategory() {
        return KitCategory.MARVEL;
    }

    // ── Cosmetic registration ───────────────────────────────────────

    private static final CosmeticSkin DEFAULT = new CosmeticSkin(
            "default",
            "&aDefault Spiderman",
            "17c6934d1ee188abb2e5b95f6be95c58aa4051c6ebaebf8a9fcb2b6442bf7b20",
            Color.RED,   // chestplate
            Color.BLUE,    // leggings
            Color.RED    // boots
    );

    private static final CosmeticSkin NOIR = new CosmeticSkin(
            "spiderman_noir",
            "&6Noir Spiderman",
            "aca4637ddb17bd7ce00ae65e4c748357577e8b48afbe4e4f078bb63398d93139",
            Color.BLACK,   // chestplate
            Color.BLACK,    // leggings
            Color.BLACK    // boots
    );

    private static final CosmeticSkin TWENTY99 = new CosmeticSkin(
            "twenty99_spiderman",
            "&c2099 Spiderman",
            "6f5422c0f6c6a926817f0bb60197fa834a2cee328504b89d72dcea1e59b7a17d",
            Color.RED,   // chestplate
            Color.BLUE,    // leggings
            Color.BLUE    // boots
    );

    private static final CosmeticSkin MILES = new CosmeticSkin(
            "miles_spiderman",
            "&fMiles Morales Spiderman",
            "de8e9fe1d129dff299be440db87d90f50a9e0c04aaefdc2dd5da7bccea39a54f",
            Color.BLACK,   // chestplate
            Color.RED,    // leggings
            Color.BLACK    // boots
    );

    @Override
    public List<CosmeticSkin> getAvailableCosmetics() {
        return Arrays.asList(
                DEFAULT,
                NOIR,
                TWENTY99,
                MILES
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
        desc.add("&7Swing through the air with 3 web charges, recharging every 6s.");
        desc.add("&7Shift to switch to Puller mode — GET OVER HERE!");
        return desc;
    }

    @Override
    public List<ItemStack> getCombatEquipments() {
        List<ItemStack> items = new ArrayList<>();
        items.add(new ItemBuilder(Material.IRON_SWORD)
                .enchantment(Enchantment.SHARPNESS, 1)
                .enchantment(Enchantment.UNBREAKING, 1).build());
        items.add(buildGrappleRod(MAX_CHARGES));
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
                        new NamespacedKey(SoupPvP.getInstance(), "spiderman_helmet_armor"),
                        4.0,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HEAD.getGroup()  // <-- EquipmentSlotGroup, not EquipmentSlot
                )
        );

        meta.addAttributeModifier(
                Attribute.ARMOR_TOUGHNESS,
                new AttributeModifier(
                        new NamespacedKey(SoupPvP.getInstance(), "spiderman_helmet_toughness"),
                        1.0,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HEAD.getGroup()
                )
        );

        helmet.setItemMeta(meta);

        ItemMeta itemMeta = helmet.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        helmet.setItemMeta(itemMeta);

        // Resolve the selected skin (same logic you already have)
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

        // Fall back to default colours if the skin somehow has none
        Color cpColor    = skin.hasOutfitColors() ? skin.getChestplateColor() : Color.BLUE;
        Color legColor   = skin.hasOutfitColors() ? skin.getLeggingsColor()   : Color.RED;
        Color bootColor  = skin.hasOutfitColors() ? skin.getBootsColor()      : Color.BLUE;

        return new ItemStack[]{
                new ItemBuilder(Material.LEATHER_BOOTS).color(bootColor)
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3).build(),
                new ItemBuilder(Material.LEATHER_LEGGINGS).color(legColor)
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3).build(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE).color(cpColor)
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3).build(),
                helmet
        };
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        List<PotionEffect> effects = new ArrayList<>();
        effects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        effects.add(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0));
        return effects;
    }

    @Override
    public void onSelect(Player player) {
        swingCharges.put(player.getUniqueId(), MAX_CHARGES);
        setRodInHand(player, buildGrappleRod(MAX_CHARGES));
    }

    // ── SHIFT: toggle Swing ↔ Puller ──────────────────────────────
    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!isSpiderman(player)) return;
        Profile profile = getProfile(player);
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;

        ItemStack held = player.getItemInHand();
        if (held == null || held.getType() != Material.FISHING_ROD) return;

        if (event.isSneaking()) {
            setRodInHand(player, buildPullerRod());
        } else {
            int charges = swingCharges.getOrDefault(player.getUniqueId(), MAX_CHARGES);
            setRodInHand(player, buildGrappleRod(charges));
        }
    }



    // ── On cast: register puller bobbers / block if no charges ─────
    @EventHandler
    public void onWebCast(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player shooter)) return;
        if (event.getEntity().getType() != EntityType.FISHING_BOBBER) return;
        if (!isSpiderman(shooter)) return;

        Profile profile = getProfile(shooter);
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;

        ItemStack held = shooter.getItemInHand();
        if (held == null || held.getType() != Material.FISHING_ROD) return;

        if (isPullerMode(held)) {
            // Check cooldown
            if (SoupPvP.getInstance().getTimersHandler().hasTimer(shooter.getUniqueId(), "Web Puller", true)) {
                shooter.sendMessage(ChatColor.RED + "Get Over Here is on cooldown for another "
                        + ChatColor.YELLOW
                        + DurationFormatter.getRemaining(SoupPvP.getInstance().getTimersHandler()
                        .getRemaining(shooter.getUniqueId(), "Web Puller", true), true)
                        + ChatColor.RED + ".");
                event.setCancelled(true);
                return;
            }

            // Register this bobber so we can tick-scan for players near it
            FishHook hook = (FishHook) event.getEntity();
            pullerBobbers.put(hook.getUniqueId(), shooter.getUniqueId());
            startPullerScan(shooter, hook);

        } else {
            // Swing mode — block if out of charges
            int charges = swingCharges.getOrDefault(shooter.getUniqueId(), MAX_CHARGES);
            if (charges <= 0) {
                shooter.sendMessage(ChatColor.RED + "No web charges! Recharging...");
                event.setCancelled(true);
            }
        }
    }

    // ── PlayerFishEvent: only used for SWING (grapple to terrain) ──

    @EventHandler
    public void onWebRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isSpiderman(player)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Profile profile = getProfile(player);
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;

        ItemStack held = player.getItemInHand();
        if (held == null || held.getType() != Material.FISHING_ROD) return;
        if (isPullerMode(held)) return;

        // Find any active bobber belonging to this player
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity.getType() != EntityType.FISHING_BOBBER) continue;
            FishHook hook = (FishHook) entity;
            if (!player.equals(hook.getShooter())) continue;

            // Bobber is still in the air — grapple toward its current position
            int charges = swingCharges.getOrDefault(player.getUniqueId(), MAX_CHARGES);
            if (charges <= 0) {
                player.sendMessage(ChatColor.RED + "No web charges! Recharging...");
                hook.remove();
                return;
            }

            event.setCancelled(true);

            int newCharges = charges - 1;
            swingCharges.put(player.getUniqueId(), newCharges);
            setRodInHand(player, buildGrappleRod(newCharges));

            Location hookLoc   = hook.getLocation();
            Location playerLoc = player.getLocation();

            Vector velocity = new Vector(
                    hookLoc.getX() - playerLoc.getX(),
                    hookLoc.getY() - playerLoc.getY() + 2.5,
                    hookLoc.getZ() - playerLoc.getZ()
            ).multiply(0.28);

            player.setVelocity(velocity);
            player.setFallDistance(0f);
            swingNoFall.add(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(),
                    () -> swingNoFall.remove(player.getUniqueId()), 60L);

            hook.remove();
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.8f, 1.4f);
            PlayerUtil.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
            sendChargeMessage(player, newCharges);
            startRechargeIfNeeded(player);
            return;
        }
    }

    // ── SWING: launch player toward hook, consume a charge ─────────
    private void handleGrapple(PlayerFishEvent event, Player player, Entity hook) {
        int charges = swingCharges.getOrDefault(player.getUniqueId(), MAX_CHARGES);
        if (charges <= 0) { event.setCancelled(true); hook.remove(); return; }

        event.setCancelled(true);

        int newCharges = charges - 1;
        swingCharges.put(player.getUniqueId(), newCharges);
        setRodInHand(player, buildGrappleRod(newCharges));

        Location playerLoc = player.getLocation();
        Location hookLoc   = hook.getLocation();

        Vector velocity = new Vector(
                hookLoc.getX() - playerLoc.getX(),
                hookLoc.getY() - playerLoc.getY() + 2.5,
                hookLoc.getZ() - playerLoc.getZ()
        ).multiply(0.28);

        player.setVelocity(velocity);
        player.setFallDistance(0f);
        swingNoFall.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(),
                () -> swingNoFall.remove(player.getUniqueId()), 60L);

        hook.remove();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.8f, 1.4f);
        PlayerUtil.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        sendChargeMessage(player, newCharges);
        startRechargeIfNeeded(player);
    }

    // ── PULLER tick scanner ─────────────────────────────────────────
    // Runs every 2 ticks while the bobber is alive.
    // The moment it gets within 1.5 blocks of any enemy player, pull them.
    private void startPullerScan(Player caster, FishHook hook) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Stop if the bobber died naturally (reeled in, expired, etc.)
                if (!hook.isValid() || hook.isDead()) {
                    pullerBobbers.remove(hook.getUniqueId());
                    cancel();
                    return;
                }

                if (!caster.isOnline()) {
                    hook.remove();
                    pullerBobbers.remove(hook.getUniqueId());
                    cancel();
                    return;
                }

                // Scan for any player within 1.5 blocks of the bobber
                for (Entity nearby : hook.getNearbyEntities(1.5, 1.5, 1.5)) {
                    if (!(nearby instanceof Player target)) continue;
                    if (target.getUniqueId().equals(caster.getUniqueId())) continue;

                    Profile targetProfile = getProfile(target);
                    if (targetProfile.getProfileState() == ProfileState.SPAWN) continue;

                    // Hit! Pull target toward caster
                    hook.remove();
                    pullerBobbers.remove(hook.getUniqueId());
                    cancel();

                    Vector pull = caster.getLocation().toVector()
                            .subtract(target.getLocation().toVector())
                            .normalize()
                            .multiply(1.8);
                    pull.setY(0.5);
                    target.setVelocity(pull);

                    // Apply cooldown
                    SoupPvP.getInstance().getTimersHandler().addPlayerTimer(
                            caster.getUniqueId(),
                            new Timer("Web Puller", TimeUnit.SECONDS.toMillis(PULLER_COOLDOWN)),
                            true);
                    XPBarTimer.runXpBar(caster, PULLER_COOLDOWN);

                    PlayerUtil.playSound(caster, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
                    PlayerUtil.playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT);
                    caster.sendMessage(CC.translate("&dGet Over Here! &7You webbed &c" + target.getName() + "&7!"));
                    target.sendMessage(CC.translate("&cYou were webbed by &d" + caster.getName() + "&c!"));
                    caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.8f, 1.4f);
                    return;
                }
            }
        }.runTaskTimer(SoupPvP.getInstance(), 1L, 2L);
    }

    // ── Cancel fall damage after a swing ───────────────────────────
    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (swingNoFall.remove(player.getUniqueId())) {
            event.setCancelled(true);
            player.setFallDistance(0f);
        }
    }

    // ── Charge recharge ticker ─────────────────────────────────────
    private void startRechargeIfNeeded(Player player) {
        UUID uuid = player.getUniqueId();
        if (recharging.contains(uuid)) return;
        recharging.add(uuid);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isSpiderman(player)) {
                    recharging.remove(uuid);
                    cancel();
                    return;
                }

                int current = swingCharges.getOrDefault(uuid, MAX_CHARGES);
                if (current >= MAX_CHARGES) {
                    recharging.remove(uuid);
                    cancel();
                    return;
                }

                int restored = current + 1;
                swingCharges.put(uuid, restored);

                ItemStack held = player.getItemInHand();
                if (held != null && held.getType() == Material.FISHING_ROD && !isPullerMode(held)) {
                    setRodInHand(player, buildGrappleRod(restored));
                }

                sendChargeMessage(player, restored);

                if (restored >= MAX_CHARGES) {
                    recharging.remove(uuid);
                    cancel();
                }
            }
        }.runTaskTimer(SoupPvP.getInstance(), RECHARGE_SECONDS * 20L, RECHARGE_SECONDS * 20L);
    }

    // ── Helpers ────────────────────────────────────────────────────
    private ItemStack buildGrappleRod(int charges) {
        return new ItemBuilder(Material.FISHING_ROD)
                .name(CC.translate("&dWeb Shooter &7[Swing] " + buildChargeBar(charges)))
                .build();
    }

    private ItemStack buildPullerRod() {
        return new ItemBuilder(Material.FISHING_ROD)
                .name(CC.translate("&cWeb Shooter &7[Get Over Here!]"))
                .build();
    }

    private String buildChargeBar(int charges) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAX_CHARGES; i++) {
            sb.append(i < charges ? "&d■" : "&8■");
        }
        return sb.toString();
    }

    private void setRodInHand(Player player, ItemStack rod) {
        ItemStack held = player.getItemInHand();
        if (held != null && held.getType() == Material.FISHING_ROD) {
            player.setItemInHand(rod);
            player.updateInventory();
        } else {
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null && item.getType() == Material.FISHING_ROD) {
                    player.getInventory().setItem(i, rod);
                    player.updateInventory();
                    break;
                }
            }
        }
    }

    private void sendChargeMessage(Player player, int charges) {
        player.sendActionBar(CC.translate("&dWeb Charges: " + buildChargeBar(charges) + " &7(" + charges + "/3)"));
    }

    private boolean isPullerMode(ItemStack item) {
        if (!item.hasItemMeta()) return false;
        String name = item.getItemMeta().getDisplayName();
        return name != null && name.contains("Get Over Here");
    }

    private boolean isSpiderman(Player player) {
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Spiderman");
        return getProfile(player).getCurrentKit().equals(kit);
    }

    private Profile getProfile(Player player) {
        return SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
    }
}