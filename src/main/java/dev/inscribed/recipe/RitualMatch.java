package dev.inscribed.recipe;

import dev.inscribed.altar.BookKnowledgeScanner;
import dev.inscribed.ritual.RitualType;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

public record RitualMatch(RitualType type, EnchantmentRecipe recipe, Holder<Enchantment> enchantment,
                          List<BookUse> books, List<InfusionMatcher.Use> ingredients) {
    public record BookUse(BookKnowledgeScanner.BookSlot slot, int count) {}
    public RitualMatch { books=List.copyOf(books); ingredients=List.copyOf(ingredients); }
    public BookKnowledgeScanner.BookSlot book() { return books.getFirst().slot(); }
    public int duration() { return type==RitualType.INFUSION ? recipe.infusion().orElseThrow().durationTicks() : recipe.extraction().orElseThrow().durationTicks(); }
}
