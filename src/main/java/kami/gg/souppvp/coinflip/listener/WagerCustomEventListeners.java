package kami.gg.souppvp.coinflip.listener;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.coinflip.CoinFlip;
import kami.gg.souppvp.coinflip.events.WagerCancelEvent;
import kami.gg.souppvp.coinflip.events.WagerCreateEvent;
import kami.gg.souppvp.coinflip.events.WagerStartEvent;
import kami.gg.souppvp.coinflip.menu.animation.AnimatedMenu;
import kami.gg.souppvp.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class WagerCustomEventListeners implements Listener {

    @EventHandler
    public void onWagerCreateEvent(WagerCreateEvent event){
        UUID creator = event.getCreator();
        Integer amount = event.getAmount();
        CoinFlip coinFlip = new CoinFlip(creator, amount);
    }

    @EventHandler
    public void onWagerCancelEvent(WagerCancelEvent event){
        CoinFlip coinFlip = event.getCoinFlip();
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(coinFlip.getCreator());
        profile.setCredits(profile.getCredits() + coinFlip.getAmount());
        SoupPvP.getInstance().getCoinFlipsHandler().removeCoinFlip(coinFlip);
    }

    @EventHandler
    public void onWagerStartEvent(WagerStartEvent event){
        CoinFlip coinFlip = event.getCoinFlip();
        UUID opponent = event.getOpponent();
        coinFlip.setOpponent(opponent);
        new AnimatedMenu(coinFlip).openMenu(Bukkit.getPlayer(coinFlip.getCreator()));
        new AnimatedMenu(coinFlip).openMenu(Bukkit.getPlayer(coinFlip.getOpponent()));
    }

}
