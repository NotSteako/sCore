package kami.gg.souppvp.timer;

import lombok.Getter;
import lombok.Setter;

/**
 * @author hieu
 * @date 24/06/2023
 */

@Getter
@Setter
public class Timer {

    private String abilityName;
    private long cooldown;

    public Timer(String abilityName, long cooldown){
        this.abilityName = abilityName;
        this.cooldown = System.currentTimeMillis() + cooldown;
    }

}
