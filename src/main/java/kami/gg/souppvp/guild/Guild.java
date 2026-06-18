package kami.gg.souppvp.guild;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Guild {

    private String name;
    private UUID leader;
    private List<UUID> members;
    private List<UUID> pendingInvites;
    /** Legacy code "&a"/"&1"/... OR hex "#rrggbb". Default "&7". */
    private String tag;

    public Guild(String name, UUID leader) {
        this.name = name;
        this.leader = leader;
        this.members = new ArrayList<>();
        this.members.add(leader);
        this.pendingInvites = new ArrayList<>();
        this.tag = "&7";
    }

    public Guild() {
        this.members = new ArrayList<>();
        this.pendingInvites = new ArrayList<>();
        this.tag = "&7";
    }

    /** "&a[GuildName]" or "#ff66cc[GuildName]". */
    /** Returns a translated, ready-to-send colored tag string. */
    public String getColoredTag() {
        String raw;
        if (tag == null || tag.isEmpty()) {
            raw = "&7[&7" + name + "&7]";
        } else {
            raw = "&7[" + tag + name + "&7]";
        }
        return GuildText.translate(raw);
    }

    public boolean isLeader(UUID uuid) {
        return leader != null && leader.equals(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members != null && members.contains(uuid);
    }
}