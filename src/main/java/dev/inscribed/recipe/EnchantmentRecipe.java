package dev.inscribed.recipe;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

public record EnchantmentRecipe(ResourceLocation id, ResourceLocation enchantment, int level, int minimumPower,
                                Visibility visibility, boolean exactBookLevel, Optional<Infusion> infusion, Optional<Extraction> extraction, ProgressionTier tier) {
    public EnchantmentRecipe(ResourceLocation id,ResourceLocation enchantment,int level,int minimumPower,Visibility visibility,boolean exactBookLevel,Optional<Infusion> infusion,Optional<Extraction> extraction) {
        this(id,enchantment,level,minimumPower,visibility,exactBookLevel,infusion,extraction,ProgressionTier.UNCLASSIFIED);
    }
    public enum Visibility { ALWAYS, ON_ENCHANTMENT_DISCOVERY, EXPLICIT_DISCOVERY }
    public record Cost(Ingredient ingredient, int count) {}
    public record Infusion(List<Cost> ingredients, int durationTicks, double bookConsumptionChance) {}
    public record Extraction(List<Cost> ingredients, int durationTicks, int emptyBooksRequired, double failureChance) {}
}
