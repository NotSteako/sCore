package kami.gg.souppvp.listener;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.module.nametag.Nametag;
import com.lunarclient.apollo.module.nametag.NametagModule;
import com.lunarclient.apollo.recipients.Recipients;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.perk.Perk;
import kami.gg.souppvp.profile.Profile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LunarClientListener implements Listener {

    private static final LegacyComponentSerializer LEGACY_AMP =
            LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer LEGACY_SEC =
            LegacyComponentSerializer.legacySection();

    private static final Random RANDOM = new Random();

    /* ---------------------------
       Apollo module getter
    ---------------------------- */
    private static NametagModule nametagModule() {
        try {
            if (Apollo.getModuleManager() == null) return null;
            return Apollo.getModuleManager().getModule(NametagModule.class);
        } catch (Throwable t) {
            return null;
        }
    }

    /* ---------------------------
       EVENTS
    ---------------------------- */

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> updateNametag(player), 20L);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> updateNametag(player), 20L);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> updateNametag(player), 1L);
    }

    @EventHandler
    public void onRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Bukkit.getScheduler().runTaskLater(SoupPvP.getInstance(), () -> updateNametag(player), 1L);
    }

    /* ---------------------------
       NAMETAG BUILD
    ---------------------------- */

    public static List<Component> fetchNameTag(Player target) {
        List<Component> lines = new ArrayList<>();

        Profile profile = SoupPvP.getInstance()
                .getProfilesHandler()
                .getProfileByUUID(target.getUniqueId());

        // --- Name line (ampersand) ---
        String nameLine = "&f" + target.getName();

        try {
            var phoenixProfile = xyz.refinedev.phoenix.Phoenix.getInstance()
                    .getProfileHandler()
                    .getProfile(target.getUniqueId());

            if (phoenixProfile != null && phoenixProfile.getNameWithColor() != null) {
                nameLine = phoenixProfile.getNameWithColor();
            }
        } catch (Throwable ignored) {}

        Component nameComponent = LEGACY_AMP.deserialize(nameLine);

        // --- Guild tag (section — already translated by GuildText.translate()) ---
        try {
            kami.gg.souppvp.guild.Guild guild =
                    SoupPvP.getInstance().getGuildsHandler() == null
                            ? null
                            : SoupPvP.getInstance().getGuildsHandler().getByPlayer(target.getUniqueId());
            if (guild != null) {
                Component tagComponent = LEGACY_SEC.deserialize(" " + guild.getColoredTag());
                nameComponent = nameComponent.append(tagComponent);
            }
        } catch (Throwable ignored) {}

        lines.add(nameComponent);

        if (profile == null) return lines;

        if (profile.isJuggernaut()) {
            lines.add(LEGACY_AMP.deserialize("&4&lJuggernaut"));
        }

        Perk currentPerk = SoupPvP.getInstance()
                .getPerksHandler()
                .getPerkByName(profile.getActivePerks().get(0));

        Perk trickster = SoupPvP.getInstance()
                .getPerksHandler()
                .getPerkByName("Trickster");

        if (currentPerk == trickster) {
            lines.add(LEGACY_AMP.deserialize("&f" + RANDOM.nextInt(11) + " &4❤"));
        } else {
            lines.add(LEGACY_AMP.deserialize("&f" + ((int) target.getHealth() / 2) + " &4❤"));
        }

        if (profile.getBounty() > 0) {
            lines.add(LEGACY_AMP.deserialize("&aBounty: &e" +
                    (currentPerk == trickster
                            ? RANDOM.nextInt(1001)
                            : profile.getBounty())));
        }

        return lines;
    }

    /* ---------------------------
       UPDATE NAMETAG
    ---------------------------- */

    public static void updateNametag(Player player) {
        NametagModule module = nametagModule();
        if (module == null) return;

        try {
            List<Component> lines = new ArrayList<>(fetchNameTag(player));
            Collections.reverse(lines);

            module.overrideNametag(
                    Recipients.ofEveryone(),
                    player.getUniqueId(),
                    Nametag.builder()
                            .lines(lines)
                            .build()
            );
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}