package kami.gg.souppvp.coinflip.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

@Getter @Setter
public class WagerCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private UUID creator;
    private Integer amount;

    public WagerCreateEvent(UUID creator, Integer amount){
        this.creator = creator;
        this.amount = amount;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
