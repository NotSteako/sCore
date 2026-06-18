package kami.gg.souppvp.coinflip.events;

import kami.gg.souppvp.coinflip.CoinFlip;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter @Setter
public class WagerCancelEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private CoinFlip coinFlip;

    public WagerCancelEvent(CoinFlip coinFlip){
        this.coinFlip = coinFlip;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
