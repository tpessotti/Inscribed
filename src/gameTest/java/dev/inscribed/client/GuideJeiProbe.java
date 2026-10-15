package dev.inscribed.client;
import java.util.*;
import mezz.jei.api.*;
import mezz.jei.api.runtime.IJeiRuntime;
import dev.inscribed.compat.jei.InscribedJeiPlugin;
import net.minecraft.resources.ResourceLocation;
@JeiPlugin
public final class GuideJeiProbe implements IModPlugin {
    private static IJeiRuntime runtime;
    private static final Set<ResourceLocation> expectedVariantIds=new HashSet<>();
    public static void expectChangedVariant(ResourceLocation id){expectedVariantIds.add(id);}
    public ResourceLocation getPluginUid(){return ResourceLocation.parse("inscribed:validation_only");}
    public void onRuntimeAvailable(IJeiRuntime r){runtime=r;}
    public static boolean ready(){return runtime!=null;}
    public static void check(){
        if(runtime==null)throw new AssertionError("JEI runtime absent");
        var manager=runtime.getRecipeManager();
        for(var type:List.of(InscribedJeiPlugin.INFUSION,InscribedJeiPlugin.EXTRACTION)){
            var rows=manager.createRecipeLookup(type).get().toList();boolean extraction=type.equals(InscribedJeiPlugin.EXTRACTION);
            var expected=ClientKnowledge.snapshot().recipes().stream().filter(r->r.extraction()==extraction).map(r->r.id()).collect(java.util.stream.Collectors.toSet());
            var actual=rows.stream().map(r->r.view.id()).collect(java.util.stream.Collectors.toSet());
            if(!actual.equals(expected)||actual.size()!=rows.size())throw new AssertionError("JEI visibility/stale recipes differ from server: "+actual+" expected "+expected);
            var all=manager.createRecipeLookup(type).includeHidden().get().toList();var unchanged=all.stream().filter(r->!expectedVariantIds.contains(r.view.id())).toList();if(unchanged.stream().map(r->r.view.id()).distinct().count()!=unchanged.size())throw new AssertionError("JEI duplicated unchanged recipe registrations after knowledge/reload");
            if(manager.getRecipeCategory(type)==null)throw new AssertionError("JEI category absent");
        }
    }
    public static void checkIntegrationToggle(){
        var cfg=dev.inscribed.config.InscribedConfig.JEI_RECIPES;boolean old=cfg.get();var snapshot=ClientKnowledge.snapshot();
        try{cfg.set(false);ClientKnowledge.receive(new dev.inscribed.knowledge.GuidePayload(false,snapshot,System.nanoTime(),true));
            for(var type:List.of(InscribedJeiPlugin.INFUSION,InscribedJeiPlugin.EXTRACTION))if(runtime.getRecipeManager().createRecipeLookup(type).get().findAny().isPresent())throw new AssertionError("JEI disabled toggle left visible recipes");
            cfg.set(true);ClientKnowledge.receive(new dev.inscribed.knowledge.GuidePayload(false,snapshot,System.nanoTime(),true));check();
        }finally{cfg.set(old);ClientKnowledge.receive(new dev.inscribed.knowledge.GuidePayload(false,snapshot,System.nanoTime(),true));}
    }
    public static void showEnchantment(String enchantment){var type=InscribedJeiPlugin.INFUSION;var manager=runtime.getRecipeManager();var row=manager.createRecipeLookup(type).get().filter(r->r.view.enchantment().toString().equals(enchantment)).findFirst().orElseThrow();runtime.getRecipesGui().showRecipes(manager.getRecipeCategory(type),List.of(row),List.of());}
    public static void show(boolean extraction){runtime.getRecipesGui().showTypes(List.of(extraction?InscribedJeiPlugin.EXTRACTION:InscribedJeiPlugin.INFUSION));}
    public static void showCatalyst(){
        var type=InscribedJeiPlugin.INFUSION;var manager=runtime.getRecipeManager();
        var rows=manager.createRecipeLookup(type).get().filter(r->r.view.id().toString().equals("inscribed:minecraft/unbreaking_3")).toList();
        if(rows.size()!=1 || rows.getFirst().view.costs().stream().flatMap(c->c.alternatives().stream()).noneMatch(s->s.is(dev.inscribed.registry.InscribedRegistries.ENCHANTMENT_CATALYST.get())))throw new AssertionError("JEI catalyst cost absent");
        runtime.getRecipesGui().showRecipes(manager.getRecipeCategory(type),rows,List.of());
    }
}
