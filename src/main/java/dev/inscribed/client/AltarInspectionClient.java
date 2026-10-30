package dev.inscribed.client;

import dev.inscribed.inspection.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid="inscribed", value=Dist.CLIENT)
public final class AltarInspectionClient {
    private static InspectionPayload progress;
    private static long received;
    public static void receive(InspectionPayload payload) {
        var mc = Minecraft.getInstance();
        if (payload.progress() < 0) { progress = null; return; }
        if (mc.player == null || mc.level == null || !InspectionLensHeld.holding(mc)
                || mc.player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(payload.table())) > 36) return;
        if (payload.progress() == 100) {
            progress = null;
            if (mc.screen == null) {
                if (AltarInspection.isBookFallback(mc.player.getMainHandItem())) mc.player.openItemGui(mc.player.getMainHandItem(),net.minecraft.world.InteractionHand.MAIN_HAND);
                else if (AltarInspection.isBookFallback(mc.player.getOffhandItem())) mc.player.openItemGui(mc.player.getOffhandItem(),net.minecraft.world.InteractionHand.OFF_HAND);
                else mc.setScreen(new AltarInspectionScreen(payload));
            }
        } else { progress = payload; received = mc.level.getGameTime(); }
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { progress = null; }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !mc.player.isUsingItem() || !AltarInspection.isLens(mc.player.getUseItem())
                || mc.level.getGameTime() - received > 20) progress = null;
    }
    private static final class InspectionLensHeld {
        static boolean holding(Minecraft mc) { return AltarInspection.isInspectionTool(mc.player.getMainHandItem()) || AltarInspection.isInspectionTool(mc.player.getOffhandItem()); }
    }
    @EventBusSubscriber(modid="inscribed", value=Dist.CLIENT, bus=EventBusSubscriber.Bus.MOD)
    public static final class Setup {
        @SubscribeEvent public static void setup(FMLClientSetupEvent event) { InspectionNetworking.receiver(AltarInspectionClient::receive);
            dev.inscribed.knowledge.KnowledgeNetworking.receiver(ClientKnowledge::receive);
            InspectionNetworking.selectionReceiver(p->{var mc=Minecraft.getInstance();if(mc.player!=null && mc.screen==null)mc.setScreen(new RitualSelectionScreen(p));});
        }
        @SubscribeEvent public static void layers(RegisterGuiLayersEvent event) {
            event.registerAboveAll(ResourceLocation.parse("inscribed:altar_analysis"), (g, delta) -> {
                var mc = Minecraft.getInstance();
                if (progress == null || mc.player == null || mc.screen != null || mc.options.hideGui || !mc.player.isUsingItem()) return;
                int x = g.guiWidth()/2, y = g.guiHeight()/2 - 30;
                g.fill(x-105,y-4,x+105,y+28,0xF0201B22);
                g.drawCenteredString(mc.font, Component.translatable("inspection.inscribed.scanning"),x,y,0xFFE2BF78);
                g.fill(x-80,y+15,x+80,y+18,0xFF45404A);
                g.fill(x-80,y+15,x-80+160*progress.progress()/100,y+18,0xFFE2BF78);
            });
        }
    }
    private AltarInspectionClient() {}
}
