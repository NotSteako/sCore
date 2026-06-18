package kami.gg.souppvp.tasks;

public class PlayerProfile {
    private final String uuid;
    private final String name;
    private final int kills;
    private final int deaths;

    // Constructor
    public PlayerProfile(String uuid, String name, int kills, int deaths) {
        this.uuid = uuid;
        this.name = name;
        this.kills = kills;
        this.deaths = deaths;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }
}
