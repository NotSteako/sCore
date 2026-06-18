package kami.gg.souppvp.kit.valorant;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.*;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.KitCategory;
import kami.gg.souppvp.kit.KitRarity;
import kami.gg.souppvp.kit.cosmetic.CosmeticSkin;
import kami.gg.souppvp.kit.cosmetic.SkinApplier;
import kami.gg.souppvp.listener.LunarClientListener;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.timer.Timer;
import kami.gg.souppvp.util.*;
import lombok.experimental.PackagePrivate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
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
 * YoruKit — based on Yoru from VALORANT.
 *
 * Abilities:
 *  • GATECRASH  (Blue Dye)  — Right-click to send a Gatecrash marker walking forward.
 *                             Right-click again within 4s to teleport to it.
 *                             20s cooldown. Cannot pass world border.
 *
 *  • FAKEOUT    (Paper)     — Right-click to place a walking decoy at your location.
 *                             Decoy walks forward, mirrors your skin/rank/health nametag.
 *                             Enemies who hit it are blinded for 3s.
 *                             15s cooldown.
 *
 * Passive: Invisibility for 1.5s when a decoy is hit.
 */
public class YoruKit extends Kit {

    // ── Tuning constants ─────────────────────────────────────────────
    private static final int    GATECRASH_COOLDOWN    = 20;
    private static final int    FAKEOUT_COOLDOWN      = 15;
    private static final int    DECOY_LIFETIME_TICKS  = 160; // 8s
    private static final int    BLIND_DURATION_TICKS  = 60;  // 3s
    private static final int    INVIS_DURATION_TICKS  = 30;  // 1.5s
    private static final int    MARKER_LIFETIME_TICKS = 80;  // 4s
    private static final double MARKER_WALK_SPEED     = 0.215;
    private static final double DECOY_WALK_SPEED      = 0.215;

    // ── State ─────────────────────────────────────────────────────────
    private final Map<UUID, FakeDecoy>       decoysByHitbox = new HashMap<>();
    private final Map<UUID, GatecrashMarker> activeMarkers  = new HashMap<>();
    private int nextFakeEntityId = Integer.MAX_VALUE - 10000;

    private final Map<Integer, FakeDecoy> decoysByEntityId = new HashMap<>();


    // ── Kit metadata ──────────────────────────────────────────────────
    @Override public String    getName()       { return "Yoru"; }
    @Override public KitRarity getRarityType() { return KitRarity.ULTIMATE; }
    @Override public Integer   getPrice()      { return getRarityType().getPrice(); }

    @Override
    public KitCategory getCategory() {
        return KitCategory.VALORANT;
    }


    // ── Cosmetic registration ───────────────────────────────────────

    private static final CosmeticSkin DEFAULT = new CosmeticSkin(
            "default",
            "&aDefault Yoru",
            "691d4e18bd3d3e46dddae3f5483ce69b12f5fd2f9ecca798d27d0fc17a72c585",
            Color.fromRGB(52, 73, 180),   // dark russet jacket
            Color.fromRGB(90, 130, 255),  // gold visor trim
            Color.fromRGB(255, 102, 45)    // burnt-orange accent
    );

    private static final CosmeticSkin MASKED = new CosmeticSkin(
            "masked_yoru",
            "&6Masked Yoru",
            "6d1fd8782837d1292a471b283f1f64193bf6cd0a157ec31ca94b086d266050cd",
            Color.fromRGB(52, 73, 180),   // dark russet jacket
            Color.fromRGB(90, 130, 255),  // gold visor trim
            Color.fromRGB(255, 102, 45)    // burnt-orange acce
    );

    @Override
    public List<CosmeticSkin> getAvailableCosmetics() {
        return Arrays.asList(
                DEFAULT,
                MASKED
        );
    }

    @Override
    public CosmeticSkin getDefaultCosmetic() {
        return DEFAULT;
    }

    @Override
    public org.bukkit.inventory.ItemStack getIcon() {
        org.bukkit.inventory.ItemStack skull = new org.bukkit.inventory.ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        SkinApplier.apply(meta, DEFAULT); // menu icon always shows default
        skull.setItemMeta(meta);
        return skull;
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
                "&5&l[Paper]&7 Right-click to place a &5Walking Decoy&7!",
                "&7Enemies who hit it are &5Blinded for 3s&7. (" + FAKEOUT_COOLDOWN + "s cooldown)"
        );
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getCombatEquipments() {
        return Arrays.asList(
                new ItemBuilder(Material.IRON_SWORD)
                        .enchantment(Enchantment.SHARPNESS, 1)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                buildFakeoutItem()
        );
    }

    @Override
    public org.bukkit.inventory.ItemStack[] getArmor(Player player) {


        org.bukkit.inventory.ItemStack helmet = new org.bukkit.inventory.ItemStack(Material.PLAYER_HEAD);
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


        // ── AttributeModifier workaround for player skull protection ──
        // Protection enchants don't apply to PLAYER_HEAD, so we grant
        // armor & toughness directly via attribute modifiers instead.
        meta.addAttributeModifier(
                Attribute.ARMOR,
                new AttributeModifier(
                        new NamespacedKey(SoupPvP.getInstance(), "yoru_helmet_armor"),
                        4.0,
                        AttributeModifier.Operation.ADD_NUMBER,
                        org.bukkit.inventory.EquipmentSlot.HEAD.getGroup()  // <-- EquipmentSlotGroup, not EquipmentSlot
                )
        );

        meta.addAttributeModifier(
                Attribute.ARMOR_TOUGHNESS,
                new AttributeModifier(
                        new NamespacedKey(SoupPvP.getInstance(), "yoru_helmet_toughness"),
                        1.0,
                        AttributeModifier.Operation.ADD_NUMBER,
                        org.bukkit.inventory.EquipmentSlot.HEAD.getGroup()
                )
        );

        helmet.setItemMeta(meta);

        ItemMeta itemMeta = helmet.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        helmet.setItemMeta(itemMeta);

        Color cpColor    = skin.hasOutfitColors() ? skin.getChestplateColor() : Color.BLUE;
        Color legColor   = skin.hasOutfitColors() ? skin.getLeggingsColor()   : Color.RED;
        Color bootColor  = skin.hasOutfitColors() ? skin.getBootsColor()      : Color.BLUE;


        return new org.bukkit.inventory.ItemStack[]{
                new ItemBuilder(Material.LEATHER_BOOTS)
                        .color(bootColor)
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                new ItemBuilder(Material.LEATHER_LEGGINGS)
                        .color(legColor)
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                new ItemBuilder(Material.LEATHER_CHESTPLATE)
                        .color(cpColor)
                        .enchantment(Enchantment.PROTECTION, 2)
                        .enchantment(Enchantment.UNBREAKING, 3)
                        .build(),
                helmet
        };
    }

    public YoruKit() {
        PacketEvents.getAPI()
                .getEventManager()
                .registerListener(new DecoyPacketListener());
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        return Arrays.asList(
                new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0)
        );
    }

    @Override
    public void onSelect(Player player) {
        decoysByHitbox.entrySet().removeIf(entry -> {
            if (entry.getValue().ownerUUID.equals(player.getUniqueId())) {
                entry.getValue().destroy();
                entry.getValue().hitbox.remove();
                return true;
            }
            return false;
        });

        GatecrashMarker marker = activeMarkers.remove(player.getUniqueId());
        if (marker != null) marker.cancel();
    }

    // ══════════════════════════════════════════════════════════════════
    //  ABILITY HANDLER — right-click
    // ══════════════════════════════════════════════════════════════════

    private boolean canUseAbility(Player player) {
        Block below = player.getLocation()
                .clone()
                .subtract(0, 0.15, 0)
                .getBlock();

        if (!below.getType().isSolid()) {
            player.sendMessage(
                    CC.translate("&cYou must be on the ground to use this ability.")
            );
            return false;
        }

        return true;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isYoru(player)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Profile profile = getProfile(player);
        if (profile.isInEvent() || profile.getProfileState() == ProfileState.SPAWN) return;

        org.bukkit.inventory.ItemStack held = player.getItemInHand();
        if (held == null) return;

        if (held.getType() == Material.BLUE_DYE && isGatecrashItem(held)) {
            event.setCancelled(true);

            if (activeMarkers.containsKey(player.getUniqueId())) {
                teleportToMarker(player);
                return;
            }

            if (!canUseAbility(player)) {
                return;
            }

            handleGatecrashActivate(player);
            return;
        }

        if (held.getType() == Material.PAPER && isFakeoutItem(held)) {
            event.setCancelled(true);

            if (!canUseAbility(player)) {
                return;
            }

            handleFakeout(player);
        }
    }

    // ── GATECRASH ─────────────────────────────────────────────────────

    private void handleGatecrashActivate(Player player) {
        String timerKey = "Yoru Gatecrash";

        if (SoupPvP.getInstance().getTimersHandler().hasTimer(player.getUniqueId(), timerKey, true)) {
            player.sendMessage(CC.translate("&5Gatecrash &7is on cooldown for another &5"
                    + DurationFormatter.getRemaining(
                    SoupPvP.getInstance().getTimersHandler().getRemaining(player.getUniqueId(), timerKey, true), true)
                    + "&7."));
            return;
        }

        applyCooldown(player, timerKey, GATECRASH_COOLDOWN);

        GatecrashMarker marker = new GatecrashMarker(player);
        activeMarkers.put(player.getUniqueId(), marker);
        marker.start();

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.6f);
        player.sendMessage(CC.translate("&5Gatecrash! &7Marker moving... right-click again to teleport to it."));
    }

    private void teleportToMarker(Player player) {
        GatecrashMarker marker = activeMarkers.remove(player.getUniqueId());
        if (marker == null) return;

        Location targetLoc = marker.getCurrentLocation();
        marker.cancel();

        if (targetLoc == null) {
            player.sendMessage(CC.translate("&5Gatecrash &7marker was lost."));
            return;
        }

        // World border safety check
        WorldBorder border = targetLoc.getWorld().getWorldBorder();

        double margin = 1.0; // player hitbox safety

        Location center = border.getCenter();
        double halfSize = border.getSize() / 2.0;

        double minX = center.getX() - halfSize + margin;
        double maxX = center.getX() + halfSize - margin;
        double minZ = center.getZ() - halfSize + margin;
        double maxZ = center.getZ() + halfSize - margin;

        if (targetLoc.getX() <= minX
                || targetLoc.getX() >= maxX
                || targetLoc.getZ() <= minZ
                || targetLoc.getZ() >= maxZ) {

            player.sendMessage(CC.translate(
                    "&5Gatecrash &7cannot teleport that close to the world border!"
            ));
            return;
        }

        targetLoc.setYaw(player.getLocation().getYaw());
        targetLoc.setPitch(player.getLocation().getPitch());

        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.1);
        player.teleport(targetLoc.clone().add(0, 0.1, 0));
        player.getWorld().spawnParticle(Particle.PORTAL, targetLoc.clone().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.1);
        player.getWorld().playSound(targetLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        player.sendMessage(CC.translate("&5Gatecrash! &7Teleported to marker."));
    }

    // ── FAKEOUT ───────────────────────────────────────────────────────

    private void handleFakeout(Player player) {
        String timerKey = "Yoru Fakeout";

        if (SoupPvP.getInstance().getTimersHandler().hasTimer(player.getUniqueId(), timerKey, true)) {
            player.sendMessage(CC.translate("&5Fakeout &7is on cooldown for another &5"
                    + DurationFormatter.getRemaining(
                    SoupPvP.getInstance().getTimersHandler().getRemaining(player.getUniqueId(), timerKey, true), true)
                    + "&7."));
            return;
        }

        FakeDecoy decoy = spawnDecoy(player);
        if (decoy == null) return;

        applyCooldown(player, timerKey, FAKEOUT_COOLDOWN);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1f, 1.2f);
        player.sendMessage(CC.translate("&5Fakeout! &7Walking decoy placed."));
    }

    // ── DECOY HIT ─────────────────────────────────────────────────────

    @EventHandler
    public void onDecoyHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand)) return;
        FakeDecoy decoy = decoysByHitbox.get(stand.getUniqueId());
        if (decoy == null) return;

        event.setCancelled(true);

        Player attacker = null;
        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof Projectile proj
                && proj.getShooter() instanceof Player p) {
            attacker = p;
        }
        if (attacker == null) return;
        if (attacker.getUniqueId().equals(decoy.ownerUUID)) return;

        attacker.addPotionEffect(
                new PotionEffect(PotionEffectType.BLINDNESS, BLIND_DURATION_TICKS, 0, false, true), true
        );
        attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1f, 1.4f);
        attacker.sendMessage(CC.translate("&5You attacked a decoy! &7Blinded!"));

        Player owner = Bukkit.getPlayer(decoy.ownerUUID);
        if (owner != null && owner.isOnline()) {
            owner.addPotionEffect(
                    new PotionEffect(PotionEffectType.INVISIBILITY, INVIS_DURATION_TICKS, 0, false, false), true
            );
            owner.sendMessage(CC.translate("&5Decoy triggered! &7You vanish briefly..."));
            owner.getWorld().playSound(owner.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.5f);
        }

        destroyDecoy(stand, decoy);
    }

    // ══════════════════════════════════════════════════════════════════
    //  DECOY SPAWNING
    // ══════════════════════════════════════════════════════════════════

    private FakeDecoy spawnDecoy(Player owner) {
        Location spawnLoc = owner.getLocation().clone();

        // Invisible ArmorStand hitbox
        ArmorStand hitbox = (ArmorStand) owner.getWorld().spawnEntity(
                spawnLoc,
                EntityType.ARMOR_STAND
        );

        hitbox.setVisible(false);
        hitbox.setGravity(false);
        hitbox.setBasePlate(false);
        hitbox.setArms(false);
        hitbox.setMarker(false);
        hitbox.setCustomNameVisible(false);
        hitbox.setCanPickupItems(false);
        hitbox.setInvulnerable(false);
        hitbox.setSmall(false);

        FakeDecoy decoy = new FakeDecoy(
                owner,
                hitbox,
                spawnLoc.clone(),
                nextFakeEntityId--
        );

        // Register decoy
        decoysByHitbox.put(hitbox.getUniqueId(), decoy);
        decoysByEntityId.put(decoy.getEntityId(), decoy);

        // Spawn fake player packets
        decoy.spawnForNearbyPlayers();

        new BukkitRunnable() {
            int ticksLived = 0;

            @Override
            public void run() {
                if (hitbox.isDead() || !hitbox.isValid()) {
                    decoysByHitbox.remove(hitbox.getUniqueId());
                    decoysByEntityId.remove(decoy.getEntityId());
                    decoy.destroy();
                    cancel();
                    return;
                }

                ticksLived++;
                decoy.tick(ticksLived);

                if (ticksLived >= DECOY_LIFETIME_TICKS) {
                    destroyDecoy(hitbox, decoy);
                    cancel();
                }
            }
        }.runTaskTimer(SoupPvP.getInstance(), 1L, 1L);

        return decoy;
    }


    private void destroyDecoy(ArmorStand hitbox, FakeDecoy decoy) {
        decoysByHitbox.remove(hitbox.getUniqueId());


        hitbox.getWorld().spawnParticle(
                Particle.PORTAL,
                hitbox.getLocation().add(0, 1, 0),
                30,
                0.3, 0.5, 0.3,
                0.1
        );

        hitbox.getWorld().playSound(
                hitbox.getLocation(),
                Sound.ENTITY_ENDERMAN_DEATH,
                0.7f,
                1.8f
        );

        decoysByEntityId.remove(decoy.getEntityId());
        decoy.destroy();
        hitbox.remove();
    }

    // ══════════════════════════════════════════════════════════════════
    //  FAKE DECOY
    // ══════════════════════════════════════════════════════════════════

    private static class FakeDecoy {

        private static final double VIEW_DISTANCE_SQUARED = 64 * 64;

        private static final LegacyComponentSerializer LEGACY_SEC =
                LegacyComponentSerializer.legacySection();

        private static final LegacyComponentSerializer LEGACY_AMP =
                LegacyComponentSerializer.legacyAmpersand();

        final UUID ownerUUID;
        private final String ownerName;
        final ArmorStand hitbox;
        private final int entityId;
        private final UUID fakeUUID;
        private final UserProfile fakeProfile;

        // Walking state
        private Location baseLocation;
        private Location currentLocation;
        private final Vector walkDirection;
        private final float currentYaw;

        private final Set<UUID> viewers = new HashSet<>();

        int getEntityId() {
            return entityId;
        }

        FakeDecoy(Player owner, ArmorStand hitbox, Location spawnLoc, int entityId) {
            this.ownerUUID     = owner.getUniqueId();
            this.ownerName     = owner.getName();
            this.hitbox        = hitbox;
            this.entityId      = entityId;
            this.fakeUUID      = UUID.randomUUID();
            // Feet Y — no offset; the spawn packet positions a player entity at foot level
            this.baseLocation  = spawnLoc.clone();
            this.currentLocation = baseLocation.clone();
            this.currentYaw    = spawnLoc.getYaw();

            // Walk forward in the direction the owner was facing
            Vector dir = owner.getLocation().getDirection().clone();
            dir.setY(0);
            if (dir.lengthSquared() < 1e-4) dir = new Vector(0, 0, -1);
            this.walkDirection = dir.normalize();

            this.fakeProfile = buildProfileFromOwner(owner, fakeUUID);
        }

        private UserProfile buildProfileFromOwner(Player owner, UUID fakeUUID) {
            com.github.retrooper.packetevents.protocol.player.User ownerUser =
                    PacketEvents.getAPI().getPlayerManager().getUser(owner);
            UserProfile ownerProfile = ownerUser.getProfile();

            String rawName = ownerName;
            String fakeName = rawName.length() > 16 ? rawName.substring(0, 16) : rawName;

            UserProfile fake = new UserProfile(fakeUUID, fakeName);
            for (TextureProperty property : ownerProfile.getTextureProperties()) {
                fake.getTextureProperties().add(property);
            }
            return fake;
        }

        private Component buildOwnerDisplayName(Player owner) {
            String nameLine = "&f" + owner.getName();

            try {
                var phoenixProfile = xyz.refinedev.phoenix.Phoenix.getInstance()
                        .getProfileHandler()
                        .getProfile(owner.getUniqueId());

                if (phoenixProfile != null && phoenixProfile.getNameWithColor() != null) {
                    nameLine = phoenixProfile.getNameWithColor();
                }
            } catch (Throwable ignored) {}

            Component component = LEGACY_AMP.deserialize(nameLine);

            try {
                kami.gg.souppvp.guild.Guild guild =
                        SoupPvP.getInstance().getGuildsHandler() == null
                                ? null
                                : SoupPvP.getInstance()
                                .getGuildsHandler()
                                .getByPlayer(owner.getUniqueId());

                if (guild != null) {
                    component = component.append(
                            LEGACY_SEC.deserialize(" " + guild.getColoredTag())
                    );
                }
            } catch (Throwable ignored) {}

            return component;
        }

        void spawnForNearbyPlayers() {
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (!isInRange(viewer)) continue;
                spawnFor(viewer);
            }
        }

        private boolean isInRange(Player viewer) {
            if (!viewer.getWorld().equals(baseLocation.getWorld())) return false;
            return viewer.getLocation().distanceSquared(baseLocation) <= VIEW_DISTANCE_SQUARED;
        }

        private void spawnFor(Player viewer) {
            if (viewers.contains(viewer.getUniqueId())) return;
            var user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);

            try {
                // 1) PLAYER_INFO add — skin texture resolution
                WrapperPlayServerPlayerInfoUpdate.PlayerInfo playerInfo =
                        new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                                fakeProfile, false, 0, GameMode.SURVIVAL, buildOwnerDisplayName(viewer), null
                        );
                WrapperPlayServerPlayerInfoUpdate addPacket = new WrapperPlayServerPlayerInfoUpdate(
                        EnumSet.of(
                                WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                                WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED
                        ),
                        Collections.singletonList(playerInfo)
                );
                user.sendPacket(addPacket);

                // 2) Spawn entity at foot level (no +1.1 offset)
                WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
                        entityId,
                        Optional.of(fakeUUID),
                        EntityTypes.PLAYER,
                        new Vector3d(currentLocation.getX(), currentLocation.getY(), currentLocation.getZ()),
                        currentLocation.getPitch(),
                        currentYaw,
                        currentYaw,
                        0,
                        Optional.empty()
                );
                user.sendPacket(spawnPacket);

                // 3) Equipment
                sendEquipment(user);

                // 4) Nametag (rank colour + guild tag + health)
                Bukkit.getScheduler().runTaskLater(
                        SoupPvP.getInstance(),
                        () -> sendNametag(user),
                        1L
                );

                // 5) Remove from tab list after skin is cached
                Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
                    WrapperPlayServerPlayerInfoRemove removePacket =
                            new WrapperPlayServerPlayerInfoRemove(fakeUUID);
                    user.sendPacket(removePacket);
                }, 2L);

                viewers.add(viewer.getUniqueId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void sendEquipment(com.github.retrooper.packetevents.protocol.player.User user) {
            Player owner = Bukkit.getPlayer(ownerUUID);
            if (owner == null) return;

            try {
                List<com.github.retrooper.packetevents.protocol.player.Equipment> equipmentList = new ArrayList<>();
                equipmentList.add(new com.github.retrooper.packetevents.protocol.player.Equipment(
                        com.github.retrooper.packetevents.protocol.player.EquipmentSlot.MAIN_HAND,
                        toPacketItem(owner.getInventory().getItemInHand())
                ));
                equipmentList.add(new com.github.retrooper.packetevents.protocol.player.Equipment(
                        com.github.retrooper.packetevents.protocol.player.EquipmentSlot.HELMET,
                        toPacketItem(owner.getInventory().getHelmet())
                ));
                equipmentList.add(new com.github.retrooper.packetevents.protocol.player.Equipment(
                        EquipmentSlot.CHEST_PLATE,
                        toPacketItem(owner.getInventory().getChestplate())
                ));
                equipmentList.add(new com.github.retrooper.packetevents.protocol.player.Equipment(
                        com.github.retrooper.packetevents.protocol.player.EquipmentSlot.LEGGINGS,
                        toPacketItem(owner.getInventory().getLeggings())
                ));
                equipmentList.add(new com.github.retrooper.packetevents.protocol.player.Equipment(
                        com.github.retrooper.packetevents.protocol.player.EquipmentSlot.BOOTS,
                        toPacketItem(owner.getInventory().getBoots())
                ));

                WrapperPlayServerEntityEquipment equipmentPacket =
                        new WrapperPlayServerEntityEquipment(entityId, equipmentList);
                user.sendPacket(equipmentPacket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Sends a nametag metadata packet to the viewer using the same
         * rank colour + guild tag + health lines that LunarClientListener builds.
         *
         * Lines from fetchNameTag come as:
         *   [0] rank-coloured name (+ guild tag appended)
         *   [1] health line  e.g. "10 ❤"
         *   [2] bounty line  (optional)
         *
         * We stack them top-to-bottom separated by newlines and set them as
         * the entity's custom name, with custom_name_visible = true.
         */
        private void sendNametag(com.github.retrooper.packetevents.protocol.player.User user) {
            Player owner = Bukkit.getPlayer(ownerUUID);
            if (owner == null) return;

            try {
                List<Component> lines = LunarClientListener.fetchNameTag(owner);

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < lines.size(); i++) {
                    sb.append(LEGACY_SEC.serialize(lines.get(i)));
                    if (i < lines.size() - 1) {
                        sb.append("\n");
                    }
                }

                Component nameComponent =
                        LunarClientListener.fetchNameTag(
                                Bukkit.getPlayer(ownerUUID)
                        ).get(0);

                String json =
                        GsonComponentSerializer.gson().serialize(nameComponent);

                List<EntityData<?>> metadata = new ArrayList<>();

                metadata.add(
                        new EntityData<>(
                                2,
                                EntityDataTypes.OPTIONAL_COMPONENT,
                                Optional.of(json)
                        )
                );

                metadata.add(
                        new EntityData<>(
                                3,
                                EntityDataTypes.BOOLEAN,
                                true
                        )
                );

                WrapperPlayServerEntityMetadata metaPacket =
                        new WrapperPlayServerEntityMetadata(
                                entityId,
                                entity -> metadata
                        );

                user.sendPacket(metaPacket);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private ItemStack toPacketItem(org.bukkit.inventory.ItemStack bukkitItem) {
            if (bukkitItem == null) return ItemStack.EMPTY;
            return SpigotConversionUtil.fromBukkitItemStack(bukkitItem);
        }

        /**
         * Called every tick by the BukkitRunnable in spawnDecoy.
         * Moves the decoy forward along walkDirection, snapping to ground height,
         * respecting world border / walls / ledges. Also re-syncs equipment and
         * nametag every second so health stays accurate.
         */
        void tick(int ticksLived) {
            // Range check — spawn/despawn for players who moved in or out
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                boolean inRange = viewer.getWorld().equals(baseLocation.getWorld())
                        && viewer.getLocation().distanceSquared(baseLocation) <= VIEW_DISTANCE_SQUARED;

                if (inRange && !viewers.contains(viewer.getUniqueId())) {
                    spawnFor(viewer);
                } else if (!inRange && viewers.contains(viewer.getUniqueId())) {
                    despawnFor(viewer);
                }
            }

            if (viewers.isEmpty()) return;

            // ── Step forward ───────────────────────────────────────────
            World world = baseLocation.getWorld();
            if (world != null) {
                Location next = baseLocation.clone().add(
                        walkDirection.getX() * DECOY_WALK_SPEED,
                        0,
                        walkDirection.getZ() * DECOY_WALK_SPEED
                );

                // World border check
                if (world.getWorldBorder().isInside(next)) {
                    // Wall check at chest height
                    Block wall = next.clone().add(0, 1, 0).getBlock();
                    if (!wall.getType().isSolid()) {
                        // Ground snap — scan down up to 2 blocks
                        Double groundY = null;
                        for (int dy = 1; dy >= -2; dy--) {
                            Block b = next.clone().add(0, dy, 0).getBlock();
                            if (b.getType().isSolid()) {
                                groundY = b.getY() + 1.0;
                                break;
                            }
                        }

                        if (groundY != null && (baseLocation.getY() - groundY) <= 1.0) {
                            next.setY(groundY);
                            baseLocation = next;
                            // Keep hitbox aligned with the visual
                            hitbox.teleport(baseLocation.clone());
                        }
                        // else: ledge or no ground — stop moving, keep emitting packets
                    }
                    // else: wall — don't advance
                }
                // else: border — don't advance
            }

            // ── Send teleport to all viewers ───────────────────────────
            try {
                WrapperPlayServerEntityTeleport teleport = new WrapperPlayServerEntityTeleport(
                        entityId,
                        new Vector3d(baseLocation.getX(), baseLocation.getY(), baseLocation.getZ()),
                        currentYaw,
                        baseLocation.getPitch(),
                        true
                );
                WrapperPlayServerEntityHeadLook headLook =
                        new WrapperPlayServerEntityHeadLook(entityId, currentYaw);

                for (Player viewer : Bukkit.getOnlinePlayers()) {
                    if (!viewers.contains(viewer.getUniqueId())) continue;
                    var user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
                    user.sendPacket(teleport);
                    user.sendPacket(headLook);
                }

                currentLocation = baseLocation.clone();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // ── Re-sync equipment + nametag every second ───────────────
            if (ticksLived % 20 == 0) {
                for (Player viewer : Bukkit.getOnlinePlayers()) {
                    if (!viewers.contains(viewer.getUniqueId())) continue;
                    var user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
                    sendEquipment(user);
                    sendNametag(user);
                }
            }
        }

        private void despawnFor(Player viewer) {
            if (!viewers.remove(viewer.getUniqueId())) return;
            try {
                var user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
                WrapperPlayServerDestroyEntities destroy =
                        new WrapperPlayServerDestroyEntities(entityId);
                user.sendPacket(destroy);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void destroy() {
            try {
                WrapperPlayServerDestroyEntities destroy =
                        new WrapperPlayServerDestroyEntities(entityId);
                WrapperPlayServerPlayerInfoRemove removeInfo =
                        new WrapperPlayServerPlayerInfoRemove(fakeUUID);

                for (UUID viewerUUID : new ArrayList<>(viewers)) {
                    Player viewer = Bukkit.getPlayer(viewerUUID);
                    if (viewer != null) {
                        var user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
                        user.sendPacket(destroy);
                        user.sendPacket(removeInfo);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                viewers.clear();
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  GATECRASH MARKER
    // ══════════════════════════════════════════════════════════════════

    private class GatecrashMarker {

        private final UUID ownerUUID;
        private Location currentLocation;
        private final Vector direction;
        private BukkitRunnable task;
        private boolean stoppedEarly = false;

        GatecrashMarker(Player owner) {
            this.ownerUUID = owner.getUniqueId();
            this.currentLocation = owner.getLocation().clone();

            Vector dir = owner.getLocation().getDirection().clone();
            dir.setY(0);
            if (dir.lengthSquared() < 1e-4) dir = new Vector(0, 0, -1);
            this.direction = dir.normalize();
        }

        Location getCurrentLocation() {
            return currentLocation == null ? null : currentLocation.clone();
        }

        void start() {
            task = new BukkitRunnable() {
                int ticksLived = 0;

                @Override
                public void run() {
                    ticksLived++;

                    if (!stoppedEarly) {
                        stepForward();
                    }

                    World world = currentLocation.getWorld();
                    if (world != null) {
                        world.spawnParticle(
                                Particle.DUST,
                                currentLocation.clone().add(0, 0.1, 0),
                                6, 0.15, 0.05, 0.15, 0,
                                new Particle.DustOptions(Color.fromRGB(40, 80, 220), 1.0f)
                        );
                    }

                    if (ticksLived >= MARKER_LIFETIME_TICKS) {
                        expire();
                        cancel();
                    }
                }
            };
            task.runTaskTimer(SoupPvP.getInstance(), 1L, 1L);
        }

        private void stepForward() {
            World world = currentLocation.getWorld();
            if (world == null) return;

            Location next = currentLocation.clone().add(
                    direction.getX() * MARKER_WALK_SPEED,
                    0,
                    direction.getZ() * MARKER_WALK_SPEED
            );

            // World border check
            WorldBorder border = world.getWorldBorder();
            Location center = border.getCenter();
            double halfSize = border.getSize() / 2.0;

            double margin = 1.0;

            if (next.getX() <= center.getX() - halfSize + margin
                    || next.getX() >= center.getX() + halfSize - margin
                    || next.getZ() <= center.getZ() - halfSize + margin
                    || next.getZ() >= center.getZ() + halfSize - margin) {

                stoppedEarly = true;
                return;
            }

            // Wall check at chest height
            Block wallCheck = next.clone().add(0, 1, 0).getBlock();
            if (wallCheck.getType().isSolid()) {
                stoppedEarly = true;
                return;
            }

            // Ground snap — scan down up to 2 blocks
            Location groundProbe = next.clone();
            Double groundY = null;
            for (int dy = 1; dy >= -2; dy--) {
                Block b = groundProbe.clone().add(0, dy, 0).getBlock();
                if (b.getType().isSolid()) {
                    groundY = b.getY() + 1.0;
                    break;
                }
            }

            if (groundY == null) {
                stoppedEarly = true;
                return;
            }

            double drop = currentLocation.getY() - groundY;
            if (drop > 1.0) {
                stoppedEarly = true;
                return;
            }

            next.setY(groundY);
            currentLocation = next;
        }

        void cancel() {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }

        private void expire() {
            activeMarkers.remove(ownerUUID, this);

            if (currentLocation == null) return;
            World world = currentLocation.getWorld();
            if (world == null) return;

            world.spawnParticle(Particle.POOF, currentLocation.clone().add(0, 0.2, 0), 20, 0.2, 0.2, 0.2, 0.02);
            world.playSound(currentLocation, Sound.ENTITY_ENDERMAN_DEATH, 0.6f, 1.6f);

            Player owner = Bukkit.getPlayer(ownerUUID);
            if (owner != null && owner.isOnline()) {
                owner.sendMessage(CC.translate("&5Gatecrash &7marker faded away."));
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════

    private void applyCooldown(Player player, String timerKey, int seconds) {
        SoupPvP.getInstance().getTimersHandler().addPlayerTimer(
                player.getUniqueId(),
                new Timer(timerKey, TimeUnit.SECONDS.toMillis(seconds)),
                true
        );
        XPBarTimer.runXpBar(player, seconds);
    }

    private org.bukkit.inventory.ItemStack buildGatecrashItem() {
        return new ItemBuilder(Material.BLUE_DYE)
                .name(CC.translate("&5Gatecrash &7[Send marker / teleport]"))
                .build();
    }

    private org.bukkit.inventory.ItemStack buildFakeoutItem() {
        return new ItemBuilder(Material.PAPER)
                .name(CC.translate("&5Fakeout &7[Walking Decoy]"))
                .build();
    }

    private boolean isGatecrashItem(org.bukkit.inventory.ItemStack item) {
        if (!item.hasItemMeta() || item.getItemMeta().getDisplayName() == null) return false;
        return item.getItemMeta().getDisplayName().contains("Gatecrash");
    }

    private boolean isFakeoutItem(org.bukkit.inventory.ItemStack item) {
        if (!item.hasItemMeta() || item.getItemMeta().getDisplayName() == null) return false;
        return item.getItemMeta().getDisplayName().contains("Fakeout");
    }

    private boolean isYoru(Player player) {
        Kit kit = SoupPvP.getInstance().getKitsHandler().getKitByName("Yoru");
        return getProfile(player).getCurrentKit().equals(kit);
    }

    private Profile getProfile(Player player) {
        return SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
    }

    private class DecoyPacketListener extends PacketListenerAbstract {

        @Override
        public void onPacketReceive(PacketReceiveEvent event) {

            if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) {
                return;
            }

            WrapperPlayClientInteractEntity packet =
                    new WrapperPlayClientInteractEntity(event);

            FakeDecoy decoy =
                    decoysByEntityId.get(packet.getEntityId());

            if (decoy == null) {
                return;
            }

            Player attacker = (Player) event.getPlayer();

            if (attacker.getUniqueId().equals(decoy.ownerUUID)) {
                return;
            }

            Bukkit.getScheduler().runTask(
                    SoupPvP.getInstance(),
                    () -> {

                        attacker.addPotionEffect(
                                new PotionEffect(
                                        PotionEffectType.BLINDNESS,
                                        BLIND_DURATION_TICKS,
                                        0
                                ),
                                true
                        );

                        Player owner = Bukkit.getPlayer(decoy.ownerUUID);

                        if (owner != null && owner.isOnline()) {
                            owner.addPotionEffect(
                                    new PotionEffect(
                                            PotionEffectType.INVISIBILITY,
                                            INVIS_DURATION_TICKS,
                                            0,
                                            false,
                                            false
                                    ),
                                    true
                            );
                        }

                        attacker.sendMessage(
                                CC.translate("&5You attacked a decoy! &7Blinded!")
                        );

                        destroyDecoy(decoy.hitbox, decoy);
                    }
            );
        }
    }

}