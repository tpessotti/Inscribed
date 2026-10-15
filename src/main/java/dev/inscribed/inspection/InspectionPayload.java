package dev.inscribed.inspection;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Private, read-only server snapshot. Progress -1 clears, 0..99 scans, 100 opens results. */
public record InspectionPayload(BlockPos table, int progress, ItemStack target, float power,
                                List<Row> rows, String failure) implements CustomPacketPayload {
    public record Row(ItemStack icon, Component text) {
        public Row { icon = icon.copy(); }
        @Override public ItemStack icon() { return icon.copy(); }
    }
    public InspectionPayload { table = table.immutable(); target = target.copy(); rows = List.copyOf(rows); }
    @Override public ItemStack target() { return target.copy(); }
    public static final Type<InspectionPayload> TYPE = new Type<>(ResourceLocation.parse("inscribed:altar_inspection"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InspectionPayload> STREAM_CODEC = new StreamCodec<>() {
        public InspectionPayload decode(RegistryFriendlyByteBuf b) {
            var pos = b.readBlockPos(); int progress = b.readVarInt();
            var target = ItemStack.OPTIONAL_STREAM_CODEC.decode(b); float power = b.readFloat();
            int size = b.readVarInt();
            if (size < 0 || size > 8192 || progress < -1 || progress > 100 || !Float.isFinite(power))
                throw new IllegalArgumentException("Invalid altar inspection");
            var rows = new java.util.ArrayList<Row>(size);
            for (int i = 0; i < size; i++) rows.add(new Row(ItemStack.OPTIONAL_STREAM_CODEC.decode(b), ComponentSerialization.STREAM_CODEC.decode(b)));
            return new InspectionPayload(pos, progress, target, power, rows, b.readUtf(64));
        }
        public void encode(RegistryFriendlyByteBuf b, InspectionPayload p) {
            b.writeBlockPos(p.table()); b.writeVarInt(p.progress());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(b, p.target()); b.writeFloat(p.power()); b.writeVarInt(p.rows().size());
            for (var row : p.rows()) { ItemStack.OPTIONAL_STREAM_CODEC.encode(b, row.icon()); ComponentSerialization.STREAM_CODEC.encode(b, row.text()); }
            b.writeUtf(p.failure(), 64);
        }
    };
    public Type<InspectionPayload> type() { return TYPE; }
    public static InspectionPayload progress(BlockPos pos, int value) { return new InspectionPayload(pos, value, ItemStack.EMPTY, 0, List.of(), ""); }
}
