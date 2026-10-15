package dev.inscribed.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Keep the Ponder-specific fixture out of absent-provider client class loading. */
@EventBusSubscriber(modid="inscribed",value=Dist.CLIENT)
public final class OptionalPonderValidation {
    private static boolean enabled(){return Boolean.getBoolean("inscribed.validatePonder")&&ModList.get().isLoaded("create")&&ModList.get().isLoaded("ponder");}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){if(enabled())PonderClientValidation.tick(event);}
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event){if(enabled())PonderClientValidation.frame(event);}
}
