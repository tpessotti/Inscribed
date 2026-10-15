package dev.inscribed.pedestal;

import dev.inscribed.registry.InscribedRegistries;
import dev.inscribed.ritual.RitualController;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** One bounded stack. Every mutation copies, marks dirty and syncs; no mutable inventory escapes. */
public final class PedestalBlockEntity extends BlockEntity {
    private ItemStack item = ItemStack.EMPTY;
    private long revision;
    private boolean running;
    private long visualStart, visualEnd;
    private int visualDuration;
    public PedestalBlockEntity(BlockPos pos, BlockState state) { super(InscribedRegistries.PEDESTAL_ENTITY.get(), pos, state); }
    public ItemStack item() { return item.copy(); }
    public long revision() { return revision; }
    public boolean running() { return running; }
    public void setItem(ItemStack stack) {
        if (level == null || level.isClientSide) return;
        if (stack.getCount() > 1) throw new IllegalArgumentException("Inscribed pedestal accepts exactly one item; split across pedestals");
        RitualController.cancelUsing(level, worldPosition, "ingredient_changed");
        item = stack.copy(); revision++; sync();
    }
    public void running(boolean active) { running = active; if(!active){visualStart=0;visualEnd=0;visualDuration=0;} sync(); }
    public void visual(int duration,boolean finish) {
        if(level==null)return;
        running=!finish;visualDuration=duration;
        if(finish)visualEnd=level.getGameTime()+20;else {visualStart=level.getGameTime();visualEnd=0;}
        sync();
    }
    public double visualStrength(double time) {
        if(running && visualDuration>0)return dev.inscribed.ritual.RitualPhase.strength((time-visualStart)/visualDuration);
        return visualEnd>0 ? dev.inscribed.ritual.RitualPhase.fade(visualEnd-time) : 0;
    }
    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (!item.isEmpty()) tag.put("Item", item.save(provider));
        tag.putLong("Revision", revision);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        item = tag.contains("Item") ? ItemStack.parseOptional(provider, tag.getCompound("Item")) : ItemStack.EMPTY;
        // Preserve every item in old saved stacks. Retrieve before reusing this
        // pedestal; the matcher refuses legacy stacks until they are split.
        revision = tag.getLong("Revision");running=false;visualStart=visualEnd=0;visualDuration=0;
    }
    @Override public void handleUpdateTag(CompoundTag tag,HolderLookup.Provider provider){
        loadWithComponents(tag,provider);
        if(level!=null&&level.isClientSide){running=tag.getBoolean("Running");visualStart=tag.getLong("VisualStart");visualEnd=tag.getLong("VisualEnd");visualDuration=tag.getInt("VisualDuration");}
    }
    @Override public void onDataPacket(net.minecraft.network.Connection connection,ClientboundBlockEntityDataPacket packet,HolderLookup.Provider provider){handleUpdateTag(packet.getTag(),provider);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        var tag = saveWithoutMetadata(provider); tag.putBoolean("Running", running);
        tag.putLong("VisualStart",visualStart);tag.putLong("VisualEnd",visualEnd);tag.putInt("VisualDuration",visualDuration);return tag;
    }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
