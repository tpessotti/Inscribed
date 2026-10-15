package dev.inscribed.recipe;

import dev.inscribed.altar.*;
import dev.inscribed.ritual.RitualType;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class RitualMatcher {
    public static List<RitualMatch> matches(ServerLevel level, AltarScanner.Snapshot altar) {
        var result=new ArrayList<RitualMatch>();
        for(var match:InfusionMatcher.matches(level,altar)) result.add(new RitualMatch(RitualType.INFUSION,match.recipe(),match.enchantment(),List.of(new RitualMatch.BookUse(match.book(),1)),match.ingredients()));
        if(!altar.valid() || altar.target().isEmpty() || !dev.inscribed.config.InscribedConfig.ENABLE_EXTRACTION.get())return List.copyOf(result);
        var enchantments=EnchantmentHelper.getEnchantmentsForCrafting(altar.target());
        var registry=level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        for(var recipe:InscribedRecipeRegistry.recipes()) {
            if(recipe.extraction().isEmpty() || altar.power()<recipe.minimumPower())continue;
            var holder=registry.getHolder(recipe.enchantment());
            if(holder.isEmpty() || enchantments.getLevel(holder.get())!=recipe.level())continue;
            int remaining=recipe.extraction().get().emptyBooksRequired();
            var books=new ArrayList<RitualMatch.BookUse>();
            for(var book:altar.books().empty()) {
                int count=Math.min(remaining,book.stack().getCount());
                if(count>0)books.add(new RitualMatch.BookUse(book,count));
                remaining-=count; if(remaining==0)break;
            }
            if(remaining!=0)continue;
            // Output needs an emptied physical slot; never overwrite a leftover stack or drop it.
            var output=books.stream().filter(b->b.count()==b.slot().stack().getCount()).findFirst();
            if(output.isEmpty())continue;
            books.remove(output.get()); books.addFirst(output.get());
            var costs=recipe.extraction().get().ingredients();var pedestals=altar.pedestals();
            if (!RecipeConstraints.valid(costs)) continue;
            var allocation=IngredientAllocator.allocate(costs.stream().mapToInt(EnchantmentRecipe.Cost::count).toArray(),pedestals.stream().mapToInt(RecipeConstraints::supply).toArray(),(d,s)->costs.get(d).ingredient().test(pedestals.get(s).stack()));
            if(allocation.isEmpty())continue;
            var uses=new ArrayList<InfusionMatcher.Use>();
            for(int i=0;i<pedestals.size();i++)if(allocation.get()[i]>0)uses.add(new InfusionMatcher.Use(pedestals.get(i),allocation.get()[i]));
            result.add(new RitualMatch(RitualType.EXTRACTION,recipe,holder.get(),books,uses));
        }
        return List.copyOf(result);
    }
    private RitualMatcher() {}
}
