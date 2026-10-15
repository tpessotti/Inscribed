package dev.inscribed;

import java.util.*;
import dev.inscribed.pedestal.PedestalBlockEntity;
import dev.inscribed.registry.InscribedRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

/** Physical fixture helper: each unit has its own block, never bypasses storage. */
public final class UnitPedestals {
    public static final List<BlockPos> OFFSETS=List.of(new BlockPos(-2,0,0),new BlockPos(2,0,0),new BlockPos(-2,0,-1),new BlockPos(2,0,-1),new BlockPos(-2,0,1),new BlockPos(2,0,1),new BlockPos(-1,0,-2),new BlockPos(1,0,-2));
    public static List<PedestalBlockEntity> set(GameTestHelper h,BlockPos table,List<ItemStack> stacks) {
        var units=new ArrayList<ItemStack>();
        // First two entries retain the historical primary pedestal positions.
        for(var stack:stacks)if(!stack.isEmpty())units.add(stack.copyWithCount(1));else units.add(ItemStack.EMPTY);
        for(var stack:stacks)for(int i=1;i<stack.getCount();i++)units.add(stack.copyWithCount(1));
        if(units.size()>8)throw new IllegalArgumentException("Fixture exceeds eight unit pedestals");
        var result=new ArrayList<PedestalBlockEntity>();
        for(int i=0;i<8;i++) {
            var pos=table.offset(OFFSETS.get(i));
            if(!(h.getLevel().getBlockEntity(h.absolutePos(pos)) instanceof PedestalBlockEntity))h.setBlock(pos,InscribedRegistries.PEDESTAL.get());
            var pedestal=(PedestalBlockEntity)h.getBlockEntity(pos);pedestal.setItem(i<units.size() ? units.get(i) : ItemStack.EMPTY);result.add(pedestal);
        }
        return List.copyOf(result);
    }
    private UnitPedestals() {}
}
