package dev.inscribed.knowledge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
/** A request can only read the sender's knowledge, never grant discoveries. */
public record GuideRequest() implements CustomPacketPayload {
    public static final Type<GuideRequest> TYPE=new Type<>(ResourceLocation.parse("inscribed:guide_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf,GuideRequest> STREAM_CODEC=StreamCodec.unit(new GuideRequest());
    public Type<GuideRequest> type(){return TYPE;}
}
