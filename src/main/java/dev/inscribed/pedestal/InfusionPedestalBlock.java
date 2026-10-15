package dev.inscribed.pedestal;

import com.mojang.serialization.MapCodec;
import dev.inscribed.altar.AltarInteraction;
import dev.inscribed.ritual.RitualController;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class InfusionPedestalBlock extends BaseEntityBlock {
    public static final MapCodec<InfusionPedestalBlock> CODEC = simpleCodec(InfusionPedestalBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(Block.box(1, 0, 1, 15, 3, 15), Block.box(5, 3, 5, 11, 11, 11), Block.box(2, 11, 2, 14, 13, 14), Block.box(3, 13, 3, 13, 14, 13));
    public InfusionPedestalBlock(BlockBehaviour.Properties properties) { super(properties); }
    @Override protected MapCodec<InfusionPedestalBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return SHAPE; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PedestalBlockEntity(pos, state); }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return interact(level, pos, player, hand) ? ItemInteractionResult.sidedSuccess(level.isClientSide) : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return interact(level, pos, player, InteractionHand.MAIN_HAND) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }
    public static boolean interact(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || player.isSpectator() || !level.mayInteract(player, pos) || !player.isAlive() || player.getEyePosition().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos)) > Math.pow(player.blockInteractionRange()+1,2)) return false;
        if (!(level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal)) return false;
        if (level.isClientSide) return true;
        ItemStack held = player.getItemInHand(hand), stored = pedestal.item();
        if (held.isEmpty()) {
            if (!stored.isEmpty()) { pedestal.setItem(ItemStack.EMPTY); AltarInteraction.returnItem(player, stored); }
        } else if (stored.isEmpty()) {
            pedestal.setItem(held.copyWithCount(1)); held.shrink(1);
        } else {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.inscribed.pedestal_occupied"), true);
        }
        return true;
    }
    @Override protected void onRemove(BlockState old, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!old.is(next.getBlock()) && !level.isClientSide && level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal) {
            RitualController.cancelUsing(level, pos, "pedestal_removed");
            ItemStack stack = pedestal.item(); pedestal.setItem(ItemStack.EMPTY);
            if (!stack.isEmpty()) Containers.dropItemStack(level, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, stack);
        }
        super.onRemove(old, level, pos, next, moving);
    }
}
