package kami.gg.souppvp.cosmetics;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.cosmetic.CosmeticSkin;
import kami.gg.souppvp.kit.menu.CosmeticSkinMenu;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.PlayerUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.boat.OakBoat;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PreviewSession {

    private static final Map<UUID, PreviewSession> sessions = new ConcurrentHashMap<>();

    @Getter private final Player player;
    @Getter private final Kit kit;
    @Getter private final Location returnLocation;
    @Getter private final GameMode previousGameMode;
    @Getter private final int armorStandEntityId;
    @Getter @Setter private int rotationTaskId = -1;


    @Getter private final Location lockedLocation;

    // Fixed location where all previews happen — adjust coords/world to match your lobby
    private static final Location PREVIEW_LOCATION = new Location(
            Bukkit.getWorld("world"), 0, 96, -39, 179, 2
    );

    // Separate spawn to return to after preview ends
    private static final Location SPAWN_LOCATION = new Location(
            Bukkit.getWorld("world"), 0, 100, 0, 0, 1
    );

    private PreviewSession(Player player, Kit kit, int armorStandEntityId) {
        this.player = player;
        this.kit = kit;
        this.returnLocation = player.getLocation().clone();
        this.previousGameMode = player.getGameMode();
        this.armorStandEntityId = armorStandEntityId;

        this.lockedLocation = PREVIEW_LOCATION.clone();
    }

    public static void startPreview(Player player, Kit kit, CosmeticSkin skin) {
        endPreview(player);

        // Teleport to the fixed preview spot before spawning the armor stand
        player.teleport(PREVIEW_LOCATION);


        int armorStandId = PreviewArmorStand.spawn(player, kit, skin);

        Location seatLoc = player.getLocation().clone();
//        OakBoat boat = player.getWorld().spawn(seatLoc, OakBoat.class, b -> {
//            b.setInvulnerable(true);
//            b.setGravity(false);
//        });


        // Hide previewing player from all others
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                other.hideEntity(SoupPvP.getInstance(), player);
            }
        }

        PreviewSession session = new PreviewSession(player, kit, armorStandId);
        sessions.put(player.getUniqueId(), session);

        int taskId = PreviewArmorStand.startRotationTask(player, armorStandId);
        session.setRotationTaskId(taskId);

        // Spectator hides inventory and prevents interaction — no potion needed
        player.getInventory().clear();
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2000, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));

        Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
            player.sendMessage(CC.translate("&dPreviewing &5&l" + skin.getDisplayName()
                    + " &d— &7Sneak to exit"));
        }, 3L);
    }

    public static void endPreview(Player player) {
        PreviewSession session = sessions.remove(player.getUniqueId());
        if (session == null) return;

        if (session.getRotationTaskId() != -1) {
            Bukkit.getScheduler().cancelTask(session.getRotationTaskId());
        }

        PreviewArmorStand.remove(player, session.getArmorStandEntityId());

        // Restore visibility to all other players
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                other.showEntity(SoupPvP.getInstance(), player);
            }
        }

        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        PlayerUtil.resetPlayer(player);
        player.sendMessage(CC.translate("&7Preview ended."));

        Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> {
            if (player.isOnline()) {
                new CosmeticSkinMenu(session.getKit()).openMenu(player);
            }
        }, 2L);
    }

    public static boolean isInPreview(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public static PreviewSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }
}