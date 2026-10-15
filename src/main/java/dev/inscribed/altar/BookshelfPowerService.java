package dev.inscribed.altar;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/** NeoForge's vanilla extension point supplies each block's power, without the vanilla 15 cap. */
public final class BookshelfPowerService {
    public static float contribution(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return 0;
        var state = level.getBlockState(pos);
        if (state.is(Blocks.CHISELED_BOOKSHELF)) return 0;
        float value = state.getEnchantPowerBonus(level, pos);
        return Float.isFinite(value) && value > 0 ? value : 0;
    }
    public static float total(ServerLevel level, List<BlockPos> sources) {
        float total = 0;
        for (var pos : sources) total += contribution(level, pos);
        return total;
    }
    private BookshelfPowerService() {}
}
