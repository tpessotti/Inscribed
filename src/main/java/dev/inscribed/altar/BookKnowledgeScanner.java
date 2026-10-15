package dev.inscribed.altar;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;

public final class BookKnowledgeScanner {
    public record BookSlot(BlockPos pos, int slot, ChiseledBookShelfBlockEntity owner, ItemStack stack) {
        public BookSlot { pos = pos.immutable(); stack = stack.copy(); }
        @Override public ItemStack stack() { return stack.copy(); }
        public boolean unchanged() { return !owner.isRemoved() && ItemStack.matches(owner.getItem(slot), stack); }
    }
    public record Library(List<BookSlot> enchanted, List<BookSlot> empty) {}
    public static Library scan(List<ChiseledBookShelfBlockEntity> shelves) {
        var enchanted = new ArrayList<BookSlot>(); var empty = new ArrayList<BookSlot>();
        for (var shelf : shelves) for (int slot = 0; slot < shelf.getContainerSize(); slot++) {
            var stack = shelf.getItem(slot);
            if (stack.is(Items.ENCHANTED_BOOK)) enchanted.add(new BookSlot(shelf.getBlockPos(), slot, shelf, stack));
            else if (stack.is(Items.BOOK)) empty.add(new BookSlot(shelf.getBlockPos(), slot, shelf, stack));
        }
        return new Library(List.copyOf(enchanted), List.copyOf(empty));
    }
    private BookKnowledgeScanner() {}
}
