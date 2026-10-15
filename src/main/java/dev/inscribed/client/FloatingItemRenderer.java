package dev.inscribed.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.inscribed.config.InscribedConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public final class FloatingItemRenderer {
    public static void render(ItemStack stack, Level level, float partial, double height, boolean running,
                              PoseStack pose, MultiBufferSource buffers, int light, int seed) {
        if (stack.isEmpty() || level == null || !InscribedConfig.RENDER_ITEMS.get()) return;
        render(stack,level,partial,height,running,running ? .6 : 0,pose,buffers,light,seed);
    }
    public static void render(ItemStack stack, Level level, float partial, double height, boolean running,double strength,
                              PoseStack pose, MultiBufferSource buffers, int light, int seed) {
        render(stack,level,partial,height,running,strength,InscribedConfig.ITEM_SCALE.get().floatValue(),pose,buffers,light,seed);
    }
    public static void render(ItemStack stack, Level level, float partial, double height, boolean running,double strength,float scale,
                              PoseStack pose, MultiBufferSource buffers, int light, int seed) {
        if(stack.isEmpty() || level==null || !InscribedConfig.RENDER_ITEMS.get())return;
        double time = level.getGameTime() + partial;
        strength=InscribedConfig.PHASE_VISUALS.get() ? strength : running ? .6 : 0;
        double pulse=1+.035*strength*Math.sin(time*.32);
        pose.pushPose();
        pose.translate(.5, height + .12*strength + Math.sin(time * InscribedConfig.BOB_SPEED.get()) * (InscribedConfig.IDLE_BOB.get() + strength*(InscribedConfig.RUNNING_BOB.get()-InscribedConfig.IDLE_BOB.get())), .5);
        pose.mulPose(Axis.YP.rotationDegrees((float)(time * InscribedConfig.IDLE_ROTATION.get() % 360 + (InscribedConfig.RUNNING_ROTATION.get()-InscribedConfig.IDLE_ROTATION.get())*strength*24*Math.sin(time*.025))));
        pose.scale(scale, scale, scale);
        pose.scale((float)pulse,(float)pulse,(float)pulse);
        if (strength>0 && InscribedConfig.DYNAMIC_LIGHT.get()) light = LightTexture.pack(Math.max(LightTexture.block(light),(int)(15*strength)),LightTexture.sky(light));
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, pose, buffers, level, seed);
        pose.popPose();
    }
    private FloatingItemRenderer() {}
}
