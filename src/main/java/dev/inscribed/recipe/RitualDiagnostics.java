package dev.inscribed.recipe;

import dev.inscribed.altar.*;
import dev.inscribed.config.InscribedConfig;
import dev.inscribed.registry.InscribedRegistries;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Read-only diagnostics. Cannot grant knowledge, consume inputs or start a ritual. */
public final class RitualDiagnostics {
    public record Failure(String recipe,String mode,String reason,double score) {}
    public static List<Failure> failures(ServerLevel level,AltarScanner.Snapshot altar) {
        if(!altar.valid())return List.of(new Failure("","",altar.failure(),0));
        if(altar.target().isEmpty())return List.of(new Failure("","","missing_target",0));
        if(altar.pedestals().stream().anyMatch(p -> p.stack().getCount()>1))return List.of(new Failure("","","legacy_pedestal_stack",0));
        var result=new ArrayList<Failure>();
        var registry=level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var existing=EnchantmentHelper.getEnchantmentsForCrafting(altar.target());
        for(var recipe:InscribedRecipeRegistry.recipes()) {
            var holder=registry.getHolder(recipe.enchantment());if(holder.isEmpty())continue;
            for(boolean extraction:new boolean[]{false,true}) {
                if(extraction ? !InscribedConfig.ENABLE_EXTRACTION.get() || recipe.extraction().isEmpty() || existing.getLevel(holder.get())!=recipe.level()
                        : !InscribedConfig.ENABLE_INFUSION.get() || recipe.infusion().isEmpty() || !InfusionMatcher.compatible(altar.target(),holder.get(),recipe.level()))continue;
                var costs=extraction ? recipe.extraction().get().ingredients() : recipe.infusion().get().ingredients();
                var slots=altar.pedestals();
                boolean ingredients=IngredientAllocator.allocate(costs.stream().mapToInt(EnchantmentRecipe.Cost::count).toArray(),slots.stream().mapToInt(RecipeConstraints::supply).toArray(),(d,s)->costs.get(d).ingredient().test(slots.get(s).stack())).isPresent();
                long present=costs.stream().filter(c -> slots.stream().anyMatch(p -> RecipeConstraints.supply(p)==1 && c.ingredient().test(p.stack()))).count();
                boolean book=extraction ? altar.books().empty().stream().mapToInt(b -> b.stack().getCount()).sum()>=recipe.extraction().get().emptyBooksRequired()
                        : altar.books().enchanted().stream().anyMatch(b -> {int n=EnchantmentHelper.getEnchantmentsForCrafting(b.stack()).getLevel(holder.get());return recipe.exactBookLevel() ? n==recipe.level() : n>=recipe.level();});
                boolean output=!extraction;
                if(extraction){int remaining=recipe.extraction().get().emptyBooksRequired();for(var slot:altar.books().empty()){int count=Math.min(remaining,slot.stack().getCount());if(count>0 && count==slot.stack().getCount())output=true;remaining-=count;if(remaining==0)break;}}
                boolean catalyst=costs.stream().anyMatch(c -> c.ingredient().test(InscribedRegistries.ENCHANTMENT_CATALYST.get().getDefaultInstance()))
                        && slots.stream().noneMatch(p -> p.stack().is(InscribedRegistries.ENCHANTMENT_CATALYST.get()) && RecipeConstraints.supply(p)==1);
                String reason=!ingredients ? catalyst ? "missing_catalyst" : "missing_ingredients" : !book ? extraction ? "missing_empty_book" : "missing_enchanted_book" : !output ? "no_empty_book_slot" : altar.power()<recipe.minimumPower() ? "insufficient_power" : "no_recipe";
                double score=(ingredients ? 100 : costs.isEmpty() ? 0 : 40.0*present/costs.size())+(book ? 15 : 0)+(extraction ? 2 : 0);
                result.add(new Failure(recipe.id().toString(),extraction ? "EXTRACTION" : "INFUSION",reason,score));
            }
        }
        result.sort(Comparator.comparingDouble(Failure::score).reversed().thenComparing(Failure::recipe).thenComparing(Failure::mode));
        return List.copyOf(result);
    }
    public static String reason(ServerLevel level,AltarScanner.Snapshot altar) {
        var failures=failures(level,altar);return failures.isEmpty() ? "no_recipe" : failures.getFirst().reason();
    }
    private RitualDiagnostics() {}
}
