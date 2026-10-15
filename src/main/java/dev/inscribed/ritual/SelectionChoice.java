package dev.inscribed.ritual;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SelectionChoice(UUID token,int index) implements CustomPacketPayload {
    public static final Type<SelectionChoice> TYPE=new Type<>(ResourceLocation.parse("inscribed:ritual_choice"));
    public static final StreamCodec<RegistryFriendlyByteBuf,SelectionChoice> STREAM_CODEC=new StreamCodec<>() {
        public SelectionChoice decode(RegistryFriendlyByteBuf b){return new SelectionChoice(b.readUUID(),b.readVarInt());}
        public void encode(RegistryFriendlyByteBuf b,SelectionChoice p){b.writeUUID(p.token());b.writeVarInt(p.index());}
    };
    public Type<SelectionChoice> type(){return TYPE;}
}
