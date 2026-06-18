package kami.gg.souppvp.events.impl.redrover.player;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RedRoverPlayerState {

    WAITING("Waiting"),
    ELIMINATED("Eliminated");

    private String readable;

}