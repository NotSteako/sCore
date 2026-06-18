package kami.gg.souppvp.coinflip.events;

import kami.gg.souppvp.coinflip.CoinFlip;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

@Getter
@Setter
public class WagerStartEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private CoinFlip coinFlip;
    private UUID opponent;

    public WagerStartEvent(CoinFlip coinFlip, UUID opponent){
        this.coinFlip = coinFlip;
        this.opponent = opponent;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
