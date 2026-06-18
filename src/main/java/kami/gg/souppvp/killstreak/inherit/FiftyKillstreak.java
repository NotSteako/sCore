package kami.gg.souppvp.killstreak.inherit;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.killstreak.Killstreak;
import kami.gg.souppvp.perk.Perk;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileState;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.ItemBuilder;
import kami.gg.souppvp.util.PlayerUtil;
import kami.gg.souppvp.util.countdown.CountdownBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FiftyKillstreak extends Killstreak implements Listener {

    @Override
    public String getName() {
        return "Nuke";
    }

    @Override
    public int getRequired() {
        return 50;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.TNT)
                .name(CC.translate("&a" + getName()))
                .lore(Arrays.asList(CC.MENU_BAR, CC.translate("&7Will start a 10 second timer and upon finish,"), CC.translate("&7it will decimate all enemies in a 25 block radius."), CC.MENU_BAR, "", CC.translate("&fKillstreak Required: &d" + getRequired()), "")).build();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeathEvent(PlayerDeathEvent event){
        if (event.getEntity().getKiller() == null) return;
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(event.getEntity().getKiller().getUniqueId());
        Perk hardlinePerk = SoupPvP.getInstance().getPerksHandler().getPerkByName("Hardline");
        if (SoupPvP.getInstance().getPerksHandler().getPerkByName(profile.getActivePerks().get(1)) == hardlinePerk){
            if (profile.getCurrentKillstreak() == getRequired()-1){
                event.getEntity().getKiller().sendMessage(CC.translate("&aYou've received the &d" + getName() + " &aperk for reaching a &d" + getRequired() + " &akillstreak!"));
                CountdownBuilder countdownBuilder = new CountdownBuilder(10);
                List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                countdownBuilder.setMessageFilter(players);
                countdownBuilder.withMessage("&eTactical Nuke Incoming! &c{time}...");
                for (int i=0; i<11; i++){
                    countdownBuilder.broadcastAt(i, TimeUnit.SECONDS);
                }
                countdownBuilder.onBroadcast(() -> {
                    event.getEntity().getKiller().getWorld().spawnParticle(org.bukkit.Particle.FLAME, event.getEntity().getKiller().getLocation().add(new Vector(0, 3, 0)), 2, 0.1, 0.5, 0.1, 0.2);
                    for (Player player : Bukkit.getOnlinePlayers()){
                        PlayerUtil.playSound(player, Sound.ENTITY_CHICKEN_EGG);
                    }
                });
                countdownBuilder.onFinish(() -> {
                    int nukedCount = 0;
                    for (Player player : Bukkit.getOnlinePlayers()){
                        Profile profile1 = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
                        if (!(SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player) && profile1.getProfileState().equals(ProfileState.SPAWN))){
                            if (player.getLocation().distance(event.getEntity().getKiller().getLocation()) <= 25){
                                if (player != event.getEntity().getKiller()){
                                    player.damage(100, event.getEntity().getKiller());
                                    nukedCount++;
                                }
                            }
                            String context = nukedCount == 1 ? "player" : "players";
                            Bukkit.broadcastMessage(CC.translate("&eThe nuke eliminated a total of &c" + nukedCount + " &e" + context + "."));
                        }
                    }
                });
                countdownBuilder.start();
                Bukkit.broadcastMessage(CC.translate("&a" + event.getEntity().getKiller().getName() + " &ehas unlocked a &cNuke&e!"));
            }
        } else {
            if (profile.getCurrentKillstreak() == getRequired()){
                event.getEntity().getKiller().sendMessage(CC.translate("&aYou've received the &d" + getName() + " &aperk for reaching a &d" + getRequired() + " &akillstreak!"));
                CountdownBuilder countdownBuilder = new CountdownBuilder(10);
                List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                countdownBuilder.setMessageFilter(players);
                countdownBuilder.withMessage("&eTactical Nuke Incoming! &c{time}...");
                for (int i=0; i<11; i++){
                    countdownBuilder.broadcastAt(i, TimeUnit.SECONDS);
                }
                countdownBuilder.onBroadcast(() -> {
                    event.getEntity().getKiller().getWorld().spawnParticle(org.bukkit.Particle.FLAME, event.getEntity().getKiller().getLocation().add(new Vector(0, 3, 0)), 2, 0.1, 0.5, 0.1, 0.2);
                    for (Player player : Bukkit.getOnlinePlayers()){
                        PlayerUtil.playSound(player, Sound.ENTITY_CHICKEN_EGG);
                    }
                });
                countdownBuilder.onFinish(() -> {
                    int nukedCount = 0;
                    for (Player player : Bukkit.getOnlinePlayers()){
                        Profile profile1 = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(player.getUniqueId());
                        if (!(SoupPvP.getInstance().getSpawnHandler().getCuboid().contains(player) && profile1.getProfileState().equals(ProfileState.SPAWN))){
                            if (player.getLocation().distance(event.getEntity().getKiller().getLocation()) <= 25){
                                if (player != event.getEntity().getKiller()){
                                    player.damage(100, event.getEntity().getKiller());
                                    nukedCount++;
                                }
                            }
                            String context = nukedCount == 1 ? "player" : "players";
                            Bukkit.broadcastMessage(CC.translate("&eThe nuke eliminated a total of &c" + nukedCount + " &e" + context + "."));
                        }
                    }
                });
                countdownBuilder.start();
                Bukkit.broadcastMessage(CC.translate("&a" + event.getEntity().getKiller().getName() + " &ehas unlocked a &cNuke&e!"));
            }
        }
    }

}
