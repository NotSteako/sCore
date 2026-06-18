package kami.gg.souppvp.events.impl.fourcorners.player;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FourCornersPlayerState {
    WAITING("Waiting"),
    ELIMINATED("Eliminated");

    private String readable;
}