package dev.inscribed.compat.jei;
import java.util.*;
import dev.inscribed.client.ClientKnowledge;
import dev.inscribed.knowledge.GuideSnapshot;
import dev.inscribed.registry.InscribedRegistries;
import mezz.jei.api.*;
import mezz.jei.api.registration.*;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

/** Loaded by JEI only. No dependency from common or standalone client classes. */
@JeiPlugin
public final class InscribedJeiPlugin implements IModPlugin {
    public static final RecipeType<DisplayRecipe> INFUSION=RecipeType.create("inscribed","infusion",DisplayRecipe.class);
    public static final RecipeType<DisplayRecipe> EXTRACTION=RecipeType.create("inscribed","extraction",DisplayRecipe.class);
    public static final class DisplayRecipe {public GuideSnapshot.Recipe view;public DisplayRecipe(GuideSnapshot.Recipe v){view=v;}}
    private IJeiRuntime runtime;
    private final Map<String,DisplayRecipe> current=new HashMap<>();
    private final Map<String,List<DisplayRecipe>> registered=new HashMap<>();
    public InscribedJeiPlugin(){ClientKnowledge.listen(this::refresh);}
    public ResourceLocation getPluginUid(){return ResourceLocation.parse("inscribed:guide");}
    public void registerCategories(IRecipeCategoryRegistration r){var gui=r.getJeiHelpers().getGuiHelper();r.addRecipeCategories(new RitualCategory(INFUSION,false,gui),new RitualCategory(EXTRACTION,true,gui));}
    public void registerRecipes(IRecipeRegistration r){}
    public void registerRecipeCatalysts(IRecipeCatalystRegistration r){for(var catalyst:List.of(Blocks.ENCHANTING_TABLE,InscribedRegistries.PEDESTAL.get(),Blocks.CHISELED_BOOKSHELF,Blocks.BOOKSHELF))r.addRecipeCatalyst(catalyst,INFUSION,EXTRACTION);}
    public void onRuntimeAvailable(IJeiRuntime r){runtime=r;registered.clear();current.clear();refresh();}
    public void onRuntimeUnavailable(){runtime=null;current.clear();registered.clear();}
    private static String key(GuideSnapshot.Recipe r){return r.id()+"/"+r.extraction();}
    private static boolean sameStacks(List<net.minecraft.world.item.ItemStack> a,List<net.minecraft.world.item.ItemStack> b){if(a.size()!=b.size())return false;for(int i=0;i<a.size();i++)if(!net.minecraft.world.item.ItemStack.matches(a.get(i),b.get(i)))return false;return true;}
    private static boolean sameStructure(GuideSnapshot.Recipe a,GuideSnapshot.Recipe b){
        if(a==null||b==null||!a.id().equals(b.id())||!a.enchantment().equals(b.enchantment())||a.level()!=b.level()||a.extraction()!=b.extraction()||a.power()!=b.power()||a.ticks()!=b.ticks()||a.chance()!=b.chance()||a.emptyBooks()!=b.emptyBooks()||a.exact()!=b.exact()||a.costs().size()!=b.costs().size()||!sameStacks(a.targets(),b.targets()))return false;
        for(int i=0;i<a.costs().size();i++){var x=a.costs().get(i);var y=b.costs().get(i);if(x.count()!=y.count()||!sameStacks(x.alternatives(),y.alternatives()))return false;}return true;
    }
    public void refresh(){
        if(runtime==null)return;var snapshot=ClientKnowledge.snapshot();var manager=runtime.getRecipeManager();
        var next=new HashMap<String,GuideSnapshot.Recipe>();if(net.minecraft.client.Minecraft.getInstance().level!=null&&dev.inscribed.config.InscribedConfig.JEI_RECIPES.get())for(var r:snapshot.recipes())next.put(key(r),r);
        var removed=current.entrySet().stream().filter(e->!next.containsKey(e.getKey())||!sameStructure(e.getValue().view,next.get(e.getKey()))).map(Map.Entry::getValue).toList();
        manager.hideRecipes(INFUSION,removed.stream().filter(r->!r.view.extraction()).toList());manager.hideRecipes(EXTRACTION,removed.stream().filter(r->r.view.extraction()).toList());
        for(var r:removed)current.remove(key(r.view));
        var added=new ArrayList<DisplayRecipe>();var restored=new ArrayList<DisplayRecipe>();
        for(var e:next.entrySet()){
            var row=current.get(e.getKey());
            if(row==null){var versions=registered.computeIfAbsent(e.getKey(),k->new ArrayList<>());row=versions.stream().filter(v->sameStructure(v.view,e.getValue())).findFirst().orElse(null);
                if(row==null){row=new DisplayRecipe(e.getValue());versions.add(row);added.add(row);}else restored.add(row);
                current.put(e.getKey(),row);
            }
            row.view=e.getValue();
        }
        manager.addRecipes(INFUSION,added.stream().filter(r->!r.view.extraction()).toList());manager.addRecipes(EXTRACTION,added.stream().filter(r->r.view.extraction()).toList());
        manager.unhideRecipes(INFUSION,restored.stream().filter(r->!r.view.extraction()).toList());manager.unhideRecipes(EXTRACTION,restored.stream().filter(r->r.view.extraction()).toList());

    }
}
