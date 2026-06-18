package kami.gg.souppvp.cosmetics;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import kami.gg.souppvp.SoupPvP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class PreviewPacketListener extends PacketListenerAbstract {

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.ENTITY_ACTION) return;
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!PreviewSession.isInPreview(player)) return;

        WrapperPlayClientEntityAction packet = new WrapperPlayClientEntityAction(event);

        if (packet.getAction() == WrapperPlayClientEntityAction.Action.START_SNEAKING) {
            Bukkit.getScheduler().runTask(SoupPvP.getInstance(), () ->
                    PreviewSession.endPreview(player));
        }
    }
}