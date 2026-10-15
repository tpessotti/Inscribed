package dev.inscribed.altar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/** Copy-on-write attachment. Active animations are network state, deliberately not persisted. */
public record AltarState(ItemStack target, long revision, boolean running, long visualStart, int visualDuration, boolean extraction, long visualEnd) {
    public AltarState(ItemStack target, long revision, boolean running) { this(target,revision,running,0,0,false,0); }
    public static final Codec<AltarState> CODEC = RecordCodecBuilder.create(i -> i.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("target").forGetter(AltarState::target),
            Codec.LONG.optionalFieldOf("revision", 0L).forGetter(AltarState::revision)
    ).apply(i, (target, revision) -> new AltarState(target, revision, false)));
    public static final StreamCodec<RegistryFriendlyByteBuf, AltarState> STREAM_CODEC = new StreamCodec<>() {
        public AltarState decode(RegistryFriendlyByteBuf b) {
            return new AltarState(ItemStack.OPTIONAL_STREAM_CODEC.decode(b), b.readLong(), b.readBoolean(),b.readLong(),b.readVarInt(),b.readBoolean(),b.readLong());
        }
        public void encode(RegistryFriendlyByteBuf b, AltarState s) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(b, s.target); b.writeLong(s.revision); b.writeBoolean(s.running);
            b.writeLong(s.visualStart);b.writeVarInt(s.visualDuration);b.writeBoolean(s.extraction);b.writeLong(s.visualEnd);
        }
    };
    public AltarState { target = target.copy(); }
    @Override public ItemStack target() { return target.copy(); }
    public static AltarState empty() { return new AltarState(ItemStack.EMPTY, 0, false); }
    public AltarState withTarget(ItemStack item) { return new AltarState(item, revision + 1, running,visualStart,visualDuration,extraction,visualEnd); }
    public AltarState withRunning(boolean value) { return new AltarState(target, revision, value); }
    public AltarState withVisual(long start,int duration,boolean reverse,long end) { return new AltarState(target,revision,end==0,start,duration,reverse,end); }
    public double visualStrength(double time) {
        if(running && visualDuration>0)return dev.inscribed.ritual.RitualPhase.strength((time-visualStart)/visualDuration);
        return visualEnd>0 ? dev.inscribed.ritual.RitualPhase.fade(visualEnd-time) : 0;
    }
}
