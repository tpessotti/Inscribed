package dev.inscribed.mixin;

import dev.inscribed.altar.AltarStateService;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Non-cancelling removal hook: events alone do not cover creative, explosions and /setblock equally. */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class TableRemovalMixin {
    @Inject(method = "onRemove", at = @At("HEAD"))
    private void inscribed$dropTarget(Level level, BlockPos pos, BlockState next, boolean moving, CallbackInfo ci) {
        BlockState previous = (BlockState)(Object)this;
        if (previous.is(Blocks.ENCHANTING_TABLE) && !next.is(Blocks.ENCHANTING_TABLE)) AltarStateService.removed(level, pos);
    }
}
