package dev.inscribed.recipe;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import java.util.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

/** Schema v1. Validates executable extraction and infusion data. */
public final class RecipeParser {
    public static EnchantmentRecipe parse(ResourceLocation id, JsonElement json, RegistryAccess registries) {
        JsonObject root = object(json, "recipe");
        ResourceLocation enchantment = ResourceLocation.parse(string(root, "enchantment"));
        var holder = registries.registryOrThrow(Registries.ENCHANTMENT).getHolder(enchantment)
                .orElseThrow(() -> new IllegalArgumentException("Unknown enchantment " + enchantment));
        int level = integer(root, "level", 1, holder.value().getMaxLevel());
        int power = integer(root, "minimum_bookshelf_power", 0, 1000000);
        var visibility = root.has("recipe_visibility") ? EnchantmentRecipe.Visibility.valueOf(string(root, "recipe_visibility")) : EnchantmentRecipe.Visibility.ALWAYS;
        boolean exact = false;
        if (root.has("exact_book_level")) {
            var e = root.get("exact_book_level");
            if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isBoolean()) throw new IllegalArgumentException("exact_book_level must be boolean");
            exact = e.getAsBoolean();
        }
        Optional<EnchantmentRecipe.Infusion> infusion = Optional.empty();
        Optional<EnchantmentRecipe.Extraction> extraction = Optional.empty();
        if (root.has("infusion")) {
            var o = object(root.get("infusion"), "infusion");
            infusion = Optional.of(new EnchantmentRecipe.Infusion(costs(o), integer(o, "duration_ticks", 1, 72000), chance(o)));
        }
        if (root.has("extraction")) {
            var o = object(root.get("extraction"), "extraction");
            extraction = Optional.of(new EnchantmentRecipe.Extraction(costs(o), integer(o, "duration_ticks", 1, 72000), integer(o, "empty_books_required", 1, 64), o.has("failure_chance") ? chance(o,"failure_chance") : 0));
        }
        if (infusion.isEmpty() && extraction.isEmpty()) throw new IllegalArgumentException("Expected infusion and/or extraction");
        var tier=root.has("progression_tier") ? ProgressionTier.valueOf(string(root,"progression_tier")) : ProgressionTier.UNCLASSIFIED;
        return new EnchantmentRecipe(id, enchantment, level, power, visibility, exact, infusion, extraction,tier);
    }
    private static List<EnchantmentRecipe.Cost> costs(JsonObject o) {
        if (!o.has("ingredients") || !o.get("ingredients").isJsonArray()) throw new IllegalArgumentException("ingredients must be an array");
        var array = o.getAsJsonArray("ingredients");
        if (array.size() > RecipeConstraints.MAX_INGREDIENTS) throw new IllegalArgumentException("At most 8 pedestal ingredient entries; recipe omitted");
        var result = new ArrayList<EnchantmentRecipe.Cost>();
        for (var entry : array) {
            var original = object(entry, "ingredient");
            int count = original.has("count") ? integer(original, "count", 1, 2048) : 1;
            var ingredientJson = original.deepCopy(); ingredientJson.remove("count");
            var ingredient = Ingredient.CODEC_NONEMPTY.parse(JsonOps.INSTANCE, ingredientJson).getOrThrow();
            if (result.size() + count > RecipeConstraints.MAX_INGREDIENTS)
                throw new IllegalArgumentException("Effective ingredient count exceeds 8 pedestals after expanding count=" + count + "; recipe omitted");
            // Backward compatible counts represent repeated physical pedestals.
            for (int unit = 0; unit < count; unit++) result.add(new EnchantmentRecipe.Cost(ingredient, 1));
        }
        return List.copyOf(result);
    }
    private static double chance(JsonObject o) {
        return chance(o,"book_consumption_chance");
    }
    private static double chance(JsonObject o,String field) {
        var e = o.get(field);
        if (e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException(field+" must be a number");
        double v = e.getAsDouble();
        if (!Double.isFinite(v) || v < 0 || v > 1) throw new IllegalArgumentException(field+" outside 0..1");
        return v;
    }
    private static JsonObject object(JsonElement e, String field) {
        if (e == null || !e.isJsonObject()) throw new IllegalArgumentException(field + " must be an object"); return e.getAsJsonObject();
    }
    private static String string(JsonObject o, String field) {
        var e = o.get(field);
        if (e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString()) throw new IllegalArgumentException(field + " must be a string");
        return e.getAsString();
    }
    private static int integer(JsonObject o, String field, int min, int max) {
        var e = o.get(field);
        if (e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException(field + " must be an integer");
        try {
            int v = e.getAsBigDecimal().intValueExact();
            if (v < min || v > max) throw new IllegalArgumentException(field + " outside " + min + ".." + max);
            return v;
        } catch (ArithmeticException ex) { throw new IllegalArgumentException(field + " must be an integer", ex); }
    }
    private RecipeParser() {}
}
