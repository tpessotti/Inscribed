package dev.inscribed.knowledge;
import java.util.function.Consumer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
public final class KnowledgeNetworking {
    private static Consumer<GuidePayload> receiver=p->{};
    public static void receiver(Consumer<GuidePayload> r){receiver=r;}
    public static void register(RegisterPayloadHandlersEvent e){
        var r=e.registrar("1").executesOn(net.neoforged.neoforge.network.registration.HandlerThread.MAIN);r.playToClient(GuidePayload.TYPE,GuidePayload.STREAM_CODEC,(p,c)->receiver.accept(p));
        r.playToServer(GuideRequest.TYPE,GuideRequest.STREAM_CODEC,(p,c)->{if(c.player() instanceof net.minecraft.server.level.ServerPlayer player)KnowledgeService.request(player);});
    }
}
