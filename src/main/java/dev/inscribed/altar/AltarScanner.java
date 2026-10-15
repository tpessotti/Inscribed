package dev.inscribed.altar;

import dev.inscribed.config.InscribedConfig;
import dev.inscribed.pedestal.PedestalBlockEntity;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;

/** Bounded demand-only scan. Never loads a chunk, modifies a slot or consumes an input. */
public final class AltarScanner {
    public record PedestalSlot(BlockPos pos, PedestalBlockEntity owner, ItemStack stack, long revision) {
        public PedestalSlot { pos = pos.immutable(); stack = stack.copy(); }
        @Override public ItemStack stack() { return stack.copy(); }
    }
    public record Snapshot(BlockPos tablePos, EnchantingTableBlockEntity table, ItemStack target, long targetRevision,
                           List<PedestalSlot> pedestals, float power, List<BlockPos> powerSources,
                           List<ChiseledBookShelfBlockEntity> shelves, BookKnowledgeScanner.Library books,
                           Set<ChunkPos> chunks, InscribedConfig.Settings settings, String failure) {
        public Snapshot { target = target.copy(); }
        @Override public ItemStack target() { return target.copy(); }
        public boolean valid() { return failure.isEmpty(); }
    }
    public static Comparator<BlockPos> positionOrder(BlockPos origin) {
        return Comparator.<BlockPos>comparingDouble(p -> p.distSqr(origin)).thenComparingInt(BlockPos::getX)
                .thenComparingInt(BlockPos::getY).thenComparingInt(BlockPos::getZ);
    }
    public static Snapshot scan(ServerLevel level, BlockPos origin) {
        var settings = InscribedConfig.settings();
        var pedestals = new ArrayList<PedestalSlot>(); var sources = new ArrayList<BlockPos>();
        var shelves = new ArrayList<ChiseledBookShelfBlockEntity>(); var chunks = new HashSet<ChunkPos>();
        var table = level.hasChunkAt(origin) && level.getBlockEntity(origin) instanceof EnchantingTableBlockEntity t
                && level.getBlockState(origin).is(Blocks.ENCHANTING_TABLE) ? t : null;
        var state = table == null ? AltarState.empty() : AltarStateService.get(table);
        String failure = table == null ? "missing_table" : "";
        int radius = Math.max(settings.bookshelfRadius(), Math.max(settings.chiseledRadius(), settings.pedestalRadius()));
        for (BlockPos mutable : BlockPos.betweenClosed(origin.offset(-radius, -settings.verticalRadius(), -radius), origin.offset(radius, settings.verticalRadius(), radius))) {
            int dx = mutable.getX() - origin.getX(), dz = mutable.getZ() - origin.getZ(), d2 = dx * dx + dz * dz;
            if (d2 > radius * radius || level.isOutsideBuildHeight(mutable)) continue;
            var pos = mutable.immutable(); chunks.add(new ChunkPos(pos));
            if (!level.hasChunkAt(pos)) { failure = "unloaded_chunk"; continue; }
            if (d2 <= settings.bookshelfRadius() * settings.bookshelfRadius() && BookshelfPowerService.contribution(level, pos) > 0) sources.add(pos);
            if (level.getBlockEntity(pos) instanceof PedestalBlockEntity p && d2 <= settings.pedestalRadius() * settings.pedestalRadius())
                pedestals.add(new PedestalSlot(pos, p, p.item(), p.revision()));
            else if (level.getBlockEntity(pos) instanceof ChiseledBookShelfBlockEntity s && d2 <= settings.chiseledRadius() * settings.chiseledRadius()) shelves.add(s);
        }
        var order = positionOrder(origin);
        pedestals.sort(Comparator.comparing(PedestalSlot::pos, order)); sources.sort(order);
        shelves.sort(Comparator.comparing(BlockEntity::getBlockPos, order));
        if (pedestals.size() > settings.maxPedestals()) failure = "too_many_pedestals";
        return new Snapshot(origin.immutable(), table, state.target(), state.revision(), List.copyOf(pedestals),
                BookshelfPowerService.total(level, sources), List.copyOf(sources), List.copyOf(shelves), BookKnowledgeScanner.scan(shelves),
                Set.copyOf(chunks), settings, failure);
    }
    private AltarScanner() {}
}
