package kami.gg.souppvp.profile;

public enum ProfileState {

    SPAWN,
    COMBAT,
    IN_EVENT,
    SPECTATING_EVENT,
    FFA,

    /**
     * Player is inside the /1v1 arena lobby. They can see fighters,
     * but cannot deal/take damage and are invisible to active fighters.
     */
    IN_1V1_LOBBY,

    /**
     * Player is actively inside a /1v1 arena duel.
     */
    IN_1V1_FIGHT,

    /**
     * Player is inside the standalone Duels world lobby. Cannot deal/take
     * damage, cannot see active fighters, main stats are not touched.
     */
    IN_DUELS_LOBBY,

    /**
     * Player is actively inside a Duels match (ranked, unranked or arcade).
     * Main kills/deaths/combat-tag are bypassed for this state.
     */
    IN_DUELS_FIGHT;

}
