package dev.inscribed.compat.ponder;

import net.createmod.ponder.api.registration.*;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.resources.ResourceLocation;

public final class InscribedPonderPlugin implements PonderPlugin {
    public static void register(){PonderIndex.addPlugin(new InscribedPonderPlugin());}
    @Override public String getModId(){return "inscribed";}
    @Override public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper){
        helper.forComponents(ResourceLocation.parse("minecraft:enchanting_table"),ResourceLocation.parse("inscribed:infusion_pedestal"))
            .addStoryBoard("enchanting_setup",InscribedPonderScenes::enchantingSetup);
    }
}
