package dev.inscribed.altar;

import dev.inscribed.registry.InscribedRegistries;
import dev.inscribed.ritual.RitualController;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;

public final class AltarStateService {
    public static AltarState get(EnchantingTableBlockEntity table) {
        var state = table.getExistingDataOrNull(InscribedRegistries.ALTAR.get());
        return state == null ? AltarState.empty() : state;
    }
    public static void target(EnchantingTableBlockEntity table, ItemStack stack) {
        if (table.getLevel() == null || table.getLevel().isClientSide) return;
        RitualController.cancelAt(table.getLevel(), table.getBlockPos(), "target_changed");
        table.setData(InscribedRegistries.ALTAR.get(), get(table).withTarget(stack));
    }
    public static void running(EnchantingTableBlockEntity table, boolean active) {
        table.setData(InscribedRegistries.ALTAR.get(), get(table).withRunning(active));
    }
    public static void visual(EnchantingTableBlockEntity table,int duration,boolean extraction,boolean finish) {
        long now=table.getLevel().getGameTime();var old=get(table);
        table.setData(InscribedRegistries.ALTAR.get(),old.withVisual(finish ? old.visualStart() : now,duration,extraction,finish ? now+20 : 0));
    }
    /** Called before the original vanilla block entity is removed, once per replacement. */
    public static void removed(Level level, BlockPos pos) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof EnchantingTableBlockEntity table)) return;
        RitualController.cancelAt(level, pos, "table_removed");
        ItemStack target = get(table).target();
        table.setData(InscribedRegistries.ALTAR.get(), AltarState.empty());
        if (!target.isEmpty()) Containers.dropItemStack(level, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, target);
    }
    private AltarStateService() {}
}
