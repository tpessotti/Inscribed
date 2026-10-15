package dev.inscribed.recipe;

import java.util.List;
import dev.inscribed.altar.AltarScanner;

/** Physical ritual rules, independent of discovery and display adapters. */
public final class RecipeConstraints {
    public static final int MAX_INGREDIENTS = 8;
    public static boolean valid(List<EnchantmentRecipe.Cost> costs) {
        return costs.size() <= MAX_INGREDIENTS && costs.stream().allMatch(c -> c.count() == 1);
    }
    public static int supply(AltarScanner.PedestalSlot slot) {
        // Legacy saved stacks are retrievable but never counted as several slots.
        return slot.stack().getCount() == 1 ? 1 : 0;
    }
    private RecipeConstraints() {}
}
