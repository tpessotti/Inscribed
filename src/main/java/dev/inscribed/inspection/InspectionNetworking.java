package dev.inscribed.inspection;

import java.util.function.Consumer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Client receiver installed on Dist.CLIENT; no optional-mod or client classes in common code. */
public final class InspectionNetworking {
    private static Consumer<InspectionPayload> receiver = p -> {};
    private static Consumer<dev.inscribed.ritual.SelectionPayload> selectionReceiver=p -> {};
    public static void selectionReceiver(Consumer<dev.inscribed.ritual.SelectionPayload> value){selectionReceiver=value;}
    public static void receiver(Consumer<InspectionPayload> value) { receiver = value; }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").executesOn(net.neoforged.neoforge.network.registration.HandlerThread.MAIN).playToClient(InspectionPayload.TYPE, InspectionPayload.STREAM_CODEC,
                (p, context) -> receiver.accept(p));
        var registrar=event.registrar("1").executesOn(net.neoforged.neoforge.network.registration.HandlerThread.MAIN);
        registrar.playToClient(dev.inscribed.ritual.SelectionPayload.TYPE,dev.inscribed.ritual.SelectionPayload.STREAM_CODEC,(p,c)->selectionReceiver.accept(p));
        registrar.playToServer(dev.inscribed.ritual.SelectionChoice.TYPE,dev.inscribed.ritual.SelectionChoice.STREAM_CODEC,(p,c)->{
            if(c.player() instanceof net.minecraft.server.level.ServerPlayer player){
                var result=dev.inscribed.ritual.RitualSelection.choose(player,p.token(),p.index());
                if(p.index()!=-1)player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.inscribed."+result.reason(),result.matches()),true);
            }
        });
    }
    private InspectionNetworking() {}
}
