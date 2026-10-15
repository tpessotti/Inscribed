package dev.inscribed.ritual;

import java.util.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SelectionPayload(UUID token,List<Option> options) implements CustomPacketPayload {
    public record Option(Component name,RitualType type,int duration,int power,double chance,Component ingredients) {}
    public SelectionPayload {options=List.copyOf(options);}
    public static final Type<SelectionPayload> TYPE=new Type<>(ResourceLocation.parse("inscribed:ritual_options"));
    public static final StreamCodec<RegistryFriendlyByteBuf,SelectionPayload> STREAM_CODEC=new StreamCodec<>() {
        public SelectionPayload decode(RegistryFriendlyByteBuf b) {
            var token=b.readUUID();int size=b.readVarInt();if(size<2 || size>4096)throw new IllegalArgumentException("Invalid option count");
            var options=new ArrayList<Option>();
            for(int i=0;i<size;i++)options.add(new Option(ComponentSerialization.STREAM_CODEC.decode(b),b.readEnum(RitualType.class),b.readVarInt(),b.readVarInt(),b.readDouble(),ComponentSerialization.STREAM_CODEC.decode(b)));
            return new SelectionPayload(token,options);
        }
        public void encode(RegistryFriendlyByteBuf b,SelectionPayload p) {
            b.writeUUID(p.token());b.writeVarInt(p.options().size());
            for(var o:p.options()){ComponentSerialization.STREAM_CODEC.encode(b,o.name());b.writeEnum(o.type());b.writeVarInt(o.duration());b.writeVarInt(o.power());b.writeDouble(o.chance());ComponentSerialization.STREAM_CODEC.encode(b,o.ingredients());}
        }
    };
    public Type<SelectionPayload> type(){return TYPE;}
}
