package dev.inscribed.client;

import dev.inscribed.altar.AltarStateService;
import dev.inscribed.config.InscribedConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** Adds the target after vanilla block entities; never registers/replaces the vanilla table renderer. */
@EventBusSubscriber(modid = "inscribed", value = Dist.CLIENT)
public final class TableTargetRenderer {
    @SubscribeEvent public static void animateBook(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        RitualAmbientSound.update();
        if(mc.level==null || !InscribedConfig.PHASE_VISUALS.get())return;
        mc.levelRenderer.iterateVisibleBlockEntities(be -> {
            if(!(be instanceof EnchantingTableBlockEntity table) || table.isRemoved())return;
            double strength=AltarStateService.get(table).visualStrength(mc.level.getGameTime());
            if(strength<=0)return;
            // Client-only enhancement of the existing vanilla book renderer.
            // Vanilla's ticker resumes its own idle values when the ritual ends.
            table.open=Math.max(table.open,(float)(.45+.35*strength));
            table.flip+=(float)(.035*strength);
        });
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent event) {
        var mc = Minecraft.getInstance();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES || mc.level == null || !InscribedConfig.RENDER_ITEMS.get()) return;
        var camera = event.getCamera().getPosition(); var pose = event.getPoseStack(); var buffers = mc.renderBuffers().bufferSource();
        event.getLevelRenderer().iterateVisibleBlockEntities(be -> {
            if (!(be instanceof EnchantingTableBlockEntity table) || table.isRemoved() || table.getBlockPos().distToCenterSqr(camera) > Math.pow(InscribedConfig.RENDER_DISTANCE.get(),2)) return;
            var state = AltarStateService.get(table);
            if (state.target().isEmpty()) return;
            pose.pushPose();
            pose.translate(table.getBlockPos().getX() - camera.x, table.getBlockPos().getY() - camera.y, table.getBlockPos().getZ() - camera.z);
            // Scale only the floating target, keeping the pedestal models at their original size.
            double height=InscribedConfig.TARGET_HEIGHT.get();float scale=InscribedConfig.TARGET_SCALE.get().floatValue();
            pose.translate(.5, height, .5);
            pose.scale(scale, scale, scale);
            pose.translate(-.5, -height, -.5);
            float partial=event.getPartialTick().getGameTimeDeltaPartialTick(false);
            FloatingItemRenderer.render(state.target(), mc.level, partial, height, state.running(),state.visualStrength(mc.level.getGameTime()+partial),
                    pose, buffers, LevelRenderer.getLightColor(mc.level, table.getBlockPos().above()), table.getBlockPos().hashCode());
            pose.popPose();
        });
    }
}
