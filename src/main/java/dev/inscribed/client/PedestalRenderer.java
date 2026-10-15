package dev.inscribed.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.inscribed.pedestal.PedestalBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;

public final class PedestalRenderer implements BlockEntityRenderer<PedestalBlockEntity> {
    public PedestalRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public void render(PedestalBlockEntity pedestal, float partial, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        double strength=pedestal.getLevel()==null ? 0 : pedestal.visualStrength(pedestal.getLevel().getGameTime()+partial);
        if(pedestal.item().is(dev.inscribed.registry.InscribedRegistries.ENCHANTMENT_CATALYST.get()))strength=Math.min(1,strength*1.15);
        FloatingItemRenderer.render(pedestal.item(), pedestal.getLevel(), partial, dev.inscribed.config.InscribedConfig.PEDESTAL_HEIGHT.get(), pedestal.running(),strength,dev.inscribed.config.InscribedConfig.PEDESTAL_ITEM_SCALE.get().floatValue(), pose, buffers, light, pedestal.getBlockPos().hashCode());
    }
}
