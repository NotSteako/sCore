package kami.gg.souppvp.cosmetics;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.cosmetic.CosmeticSkin;
import kami.gg.souppvp.kit.cosmetic.SkinApplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class PreviewArmorStand {

    public static int spawn(Player viewer, Kit kit, CosmeticSkin skin) {
        int entityId = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE / 2, Integer.MAX_VALUE);
        UUID entityUUID = UUID.randomUUID();

        Location loc = viewer.getLocation().clone();
        loc.add(viewer.getLocation().getDirection().multiply(3));

        // Walk down until we hit a solid block instead of sinking into water/air
        while (loc.getY() > 0 && (loc.getBlock().getType() == Material.AIR
                || loc.getBlock().getType() == Material.WATER)) {
            loc.setY(loc.getY() - 1);
        }
        loc.setY(loc.getBlockY() + 1);

        spawnEntity(viewer, entityId, entityUUID, loc);
        applyMetadata(viewer, entityId);
        equipArmor(viewer, entityId, kit, skin);
        // startRotationTask removed from here

        return entityId;
    }

    // AFTER
    public static int startRotationTask(Player viewer, int entityId) {  // void -> int
        final float[] yaw = {0};

        return Bukkit.getScheduler().runTaskTimer(SoupPvP.getInstance(), () -> {
            yaw[0] = (yaw[0] + 3) % 360;

            WrapperPlayServerEntityHeadLook headPacket =
                    new WrapperPlayServerEntityHeadLook(entityId, yaw[0]);
            PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, headPacket);

            WrapperPlayServerEntityRotation rotPacket =
                    new WrapperPlayServerEntityRotation(entityId, yaw[0], 0, true);
            PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, rotPacket);

        }, 0L, 1L).getTaskId();  // return directly, session stores it in PreviewSession now
    }

    public static void remove(Player viewer, int entityId) {
        WrapperPlayServerDestroyEntities packet =
                new WrapperPlayServerDestroyEntities(entityId);

        PacketEvents.getAPI()
                .getPlayerManager()
                .sendPacket(viewer, packet);
    }

    private static void spawnEntity(Player viewer, int entityId, UUID uuid, Location loc) {
        WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(
                entityId,
                Optional.of(uuid),
                EntityTypes.ARMOR_STAND,
                new Vector3d(loc.getX(), loc.getY(), loc.getZ()),
                loc.getPitch(),
                loc.getYaw(),
                loc.getYaw(),
                0,
                Optional.empty()
        );

        PacketEvents.getAPI()
                .getPlayerManager()
                .sendPacket(viewer, packet);
    }

    public static int spawnHorse(Player viewer, Location loc) {
        int entityId = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE / 4, Integer.MAX_VALUE / 2);
        UUID entityUUID = UUID.randomUUID();

        // Spawn the horse at the same location as the armor stand
        WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(
                entityId,
                Optional.of(entityUUID),
                EntityTypes.HORSE,
                new Vector3d(loc.getX(), loc.getY(), loc.getZ()),
                0, 0, 0, 0,
                Optional.empty()
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);

        // Metadata: invisible, no gravity, silent
        List<EntityData<?>> metadata = new ArrayList<>();

        // Index 0: entity flags — 0x20 = invisible
        metadata.add(new EntityData<>(0, EntityDataTypes.BYTE, (byte) 0x20));
        // Index 5: no gravity
        metadata.add(new EntityData<>(5, EntityDataTypes.BOOLEAN, true));
        // Index 17: horse flags — 0x04 = saddled (required to be rideable)
        metadata.add(new EntityData<>(17, EntityDataTypes.BYTE, (byte) 0x04));

        WrapperPlayServerEntityMetadata metaPacket =
                new WrapperPlayServerEntityMetadata(entityId, metadata);
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, metaPacket);

        return entityId;
    }

    public static void mountPlayerOnHorse(Player viewer, int horseEntityId) {
        WrapperPlayServerSetPassengers packet = new WrapperPlayServerSetPassengers(
                horseEntityId,
                new int[]{viewer.getEntityId()}
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
    }

    private static void applyMetadata(Player viewer, int entityId) {
        List<EntityData<?>> metadata = new ArrayList<>();

        // Index 5: no gravity
        metadata.add(new EntityData<>(
                5,
                EntityDataTypes.BOOLEAN,
                true
        ));

        // Index 15: armor stand flags — 0x08 = no base plate
        metadata.add(new EntityData<>(
                15,
                EntityDataTypes.BYTE,
                (byte) 0x08
        ));

        WrapperPlayServerEntityMetadata packet =
                new WrapperPlayServerEntityMetadata(entityId, metadata);

        PacketEvents.getAPI()
                .getPlayerManager()
                .sendPacket(viewer, packet);
    }

    private static void equipArmor(Player viewer, int entityId, Kit kit, CosmeticSkin skin) {
        ItemStack[] armor = kit.getArmorForPreview();

        ItemStack boots  = armor[0];
        ItemStack legs   = armor[1];
        ItemStack chest  = armor[2];
        ItemStack helmet = buildPreviewHelmet(kit, skin);

        List<Equipment> equipment = new ArrayList<>();
        equipment.add(new Equipment(EquipmentSlot.HELMET,
                SpigotConversionUtil.fromBukkitItemStack(helmet)));
        equipment.add(new Equipment(EquipmentSlot.CHEST_PLATE,
                SpigotConversionUtil.fromBukkitItemStack(chest)));
        equipment.add(new Equipment(EquipmentSlot.LEGGINGS,
                SpigotConversionUtil.fromBukkitItemStack(legs)));
        equipment.add(new Equipment(EquipmentSlot.BOOTS,
                SpigotConversionUtil.fromBukkitItemStack(boots)));

        WrapperPlayServerEntityEquipment packet =
                new WrapperPlayServerEntityEquipment(entityId, equipment);

        PacketEvents.getAPI()
                .getPlayerManager()
                .sendPacket(viewer, packet);
    }

    private static ItemStack buildPreviewHelmet(Kit kit, CosmeticSkin skin) {
        ItemStack helmet = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) helmet.getItemMeta();

        SkinApplier.apply(meta, skin != null ? skin : kit.getDefaultCosmetic());

        helmet.setItemMeta(meta);
        return helmet;
    }
}