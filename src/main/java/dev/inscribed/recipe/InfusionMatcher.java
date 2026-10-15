package dev.inscribed.recipe;

import dev.inscribed.altar.*;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.enchantment.*;

public final class InfusionMatcher {
    public record Use(AltarScanner.PedestalSlot pedestal, int count) {}
    public record Match(EnchantmentRecipe recipe, Holder<Enchantment> enchantment, BookKnowledgeScanner.BookSlot book, List<Use> ingredients) {}
    public static List<Match> matches(ServerLevel level, AltarScanner.Snapshot altar) {
        var result = new ArrayList<Match>();
        if (!altar.valid() || altar.target().isEmpty() || !dev.inscribed.config.InscribedConfig.ENABLE_INFUSION.get()) return List.of();
        var registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        for (var recipe : InscribedRecipeRegistry.recipes()) {
            if (recipe.infusion().isEmpty() || altar.power() < recipe.minimumPower()) continue;
            var enchantment = registry.getHolder(recipe.enchantment());
            if (enchantment.isEmpty() || !compatible(altar.target(), enchantment.get(), recipe.level())) continue;
            var book = altar.books().enchanted().stream().filter(b -> {
                int stored = EnchantmentHelper.getEnchantmentsForCrafting(b.stack()).getLevel(enchantment.get());
                return recipe.exactBookLevel() ? stored == recipe.level() : stored >= recipe.level();
            }).findFirst();
            if (book.isEmpty()) continue;
            var costs = recipe.infusion().get().ingredients(); var pedestals = altar.pedestals();
            if (!RecipeConstraints.valid(costs)) continue;
            var allocation = IngredientAllocator.allocate(costs.stream().mapToInt(EnchantmentRecipe.Cost::count).toArray(),
                    pedestals.stream().mapToInt(RecipeConstraints::supply).toArray(), (d, s) -> costs.get(d).ingredient().test(pedestals.get(s).stack()));
            if (allocation.isEmpty()) continue;
            var uses = new ArrayList<Use>();
            for (int i = 0; i < pedestals.size(); i++) if (allocation.get()[i] > 0) uses.add(new Use(pedestals.get(i), allocation.get()[i]));
            result.add(new Match(recipe, enchantment.get(), book.get(), List.copyOf(uses)));
        }
        return List.copyOf(result);
    }
    public static boolean compatible(net.minecraft.world.item.ItemStack target, Holder<Enchantment> enchantment, int level) {
        if ((!target.is(net.minecraft.world.item.Items.BOOK) && !target.supportsEnchantment(enchantment))
                || level < 1 || level > enchantment.value().getMaxLevel()) return false;
        var existing = EnchantmentHelper.getEnchantmentsForCrafting(target);
        if (existing.getLevel(enchantment) >= level) return false;
        for (var other : existing.keySet()) if (!other.equals(enchantment) && !Enchantment.areCompatible(other, enchantment)) return false;
        return true;
    }
    private InfusionMatcher() {}
}
