package dev.inscribed.compat.ponder;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Never resolves Ponder classes on dedicated servers or clients without Create. */
@EventBusSubscriber(modid="inscribed",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class OptionalPonderClient {
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        if(!ModList.get().isLoaded("create")||!ModList.get().isLoaded("ponder"))return;
        event.enqueueWork(()->{
            try { Class.forName("dev.inscribed.compat.ponder.InscribedPonderPlugin").getMethod("register").invoke(null); }
            catch(ReflectiveOperationException|LinkageError error) { com.mojang.logging.LogUtils.getLogger().error("Inscribed optional Ponder registration failed",error); }
        });
    }
}
