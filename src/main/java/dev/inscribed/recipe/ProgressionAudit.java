package dev.inscribed.recipe;

import java.util.*;
import dev.inscribed.loot.LootInjection;
import net.minecraft.resources.ResourceLocation;

/** Read-only consistency audit on immutable reload snapshots, never on each loot roll. */
public final class ProgressionAudit {
    public static List<String> warnings() {
        var warnings=new ArrayList<String>();var recipes=InscribedRecipeRegistry.recipes();var pools=LootInjection.pools();
        boolean catalystSource=dev.inscribed.config.InscribedConfig.CATALYST_LOOT.get() && pools.stream().filter(ProgressionAudit::activePool).flatMap(p->p.entries().stream()).anyMatch(e->e.item().toString().equals("inscribed:enchantment_catalyst"));
        for(var recipe:recipes) {
            if(recipe.infusion().isPresent()) {
                boolean source=dev.inscribed.config.InscribedConfig.BOOK_LOOT.get() && pools.stream().filter(ProgressionAudit::activePool).flatMap(p->p.entries().stream()).anyMatch(e->recipe.enchantment().equals(e.enchantment()) && (recipe.exactBookLevel() ? e.minLevel()<=recipe.level() && e.maxLevel()>=recipe.level() : e.maxLevel()>=recipe.level()));
                if(!source)warnings.add("NO_INJECTED_BOOK_SOURCE "+recipe.id()+" (vanilla loot/trades/give may still supply it)");
                boolean catalyst=recipe.infusion().get().ingredients().stream().anyMatch(c->c.ingredient().test(dev.inscribed.registry.InscribedRegistries.ENCHANTMENT_CATALYST.get().getDefaultInstance()));
                if(catalyst && !catalystSource)warnings.add("NO_CATALYST_SOURCE "+recipe.id());
            }
        }
        for(var pool:pools)for(var entry:pool.entries())if(entry.enchantment()!=null) {
            for(int level=entry.minLevel();level<=entry.maxLevel();level++) {
                final int n=level;
                var recipe=recipes.stream().filter(r->r.enchantment().equals(entry.enchantment()) && r.level()==n).findFirst();
                if(recipe.isEmpty())warnings.add("BOOK_WITHOUT_RECIPE "+pool.id()+" "+entry.enchantment()+" "+level);
                else if(recipe.get().tier().ordinal()>pool.structureTier())warnings.add("HIGH_TIER_IN_LOW_STRUCTURE "+recipe.get().id()+" -> "+pool.id());
            }
        }
        return List.copyOf(warnings);
    }
    public static List<String> sources(ResourceLocation enchantment) {
        return LootInjection.pools().stream().filter(p->p.entries().stream().anyMatch(e->enchantment.equals(e.enchantment()))).map(p->p.id()+" ["+p.theme()+"] "+p.tables()).toList();
    }
    private ProgressionAudit() {}
    private static boolean activePool(LootInjection.Pool pool){
        return dev.inscribed.config.InscribedConfig.LOOT_INJECTION.get() && dev.inscribed.config.InscribedConfig.LOOT_MULTIPLIER.get()>0
                && (!pool.treasure() || dev.inscribed.config.InscribedConfig.TREASURE_LOOT_MULTIPLIER.get()>0) && pool.chance()>0 && pool.maxRolls()>0;
    }
}
