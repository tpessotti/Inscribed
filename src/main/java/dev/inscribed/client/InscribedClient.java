package dev.inscribed.client;

import dev.inscribed.registry.InscribedRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = "inscribed", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class InscribedClient {
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(InscribedRegistries.PEDESTAL_ENTITY.get(), PedestalRenderer::new);
    }
}
