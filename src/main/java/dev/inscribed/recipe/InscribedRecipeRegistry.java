package dev.inscribed.recipe;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import java.util.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

public final class InscribedRecipeRegistry extends SimpleJsonResourceReloadListener {
    private static Map<ResourceLocation, EnchantmentRecipe> recipes = Map.of();
    private static long generation;
    private final RegistryAccess registries;
    private InscribedRecipeRegistry(RegistryAccess registries) { super(new Gson(), "inscribed/enchantment_recipes"); this.registries = registries; }
    public static void registerReload(AddReloadListenerEvent event) { event.addListener(new InscribedRecipeRegistry(event.getRegistryAccess())); }
    @Override protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager resources, ProfilerFiller profiler) {
        var next = new TreeMap<ResourceLocation, EnchantmentRecipe>();
        var configured=dev.inscribed.config.ConfigRecipeStore.merge(dev.inscribed.config.ConfigRecipeStore.load(),data,"enchantment_recipes");
        for (var entry : configured.entrySet()) {
            try {
                var object=entry.getValue().getAsJsonObject();
                if(object.has("enabled")){if(!object.get("enabled").isJsonPrimitive()||!object.get("enabled").getAsJsonPrimitive().isBoolean())throw new IllegalArgumentException("enabled must be boolean");if(!object.get("enabled").getAsBoolean())continue;}
                if (!net.neoforged.neoforge.common.conditions.ICondition.conditionsMatched(makeConditionalOps(), entry.getValue())) continue;
                next.put(entry.getKey(), RecipeParser.parse(entry.getKey(), entry.getValue(), registries));
            }
            catch (RuntimeException ex) { LogUtils.getLogger().error("Invalid Inscribed recipe {}: {}", entry.getKey(), ex.getMessage()); }
        }
        recipes = Collections.unmodifiableMap(next); generation++;
        LogUtils.getLogger().info("Loaded {} Inscribed enchantment recipes (generation {})", recipes.size(), generation);
    }
    public static Collection<EnchantmentRecipe> recipes() { return recipes.values(); }
    public static long generation() { return generation; }
    public static void clear() { recipes = Map.of(); generation++; }
}
