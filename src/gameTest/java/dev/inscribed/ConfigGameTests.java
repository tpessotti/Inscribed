package dev.inscribed;

import com.google.gson.*;
import dev.inscribed.config.*;
import dev.inscribed.recipe.*;
import java.nio.file.Files;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("inscribed") @PrefixGameTestTemplate(false)
public final class ConfigGameTests {
    private static JsonObject document(){return JsonParser.parseString("{\"format_version\":1,\"include_datapack_recipes\":true,\"enchantment_recipes\":{},\"crafting_recipes\":{}}").getAsJsonObject();}
    @GameTest(template="test_altar") public static void configOverlayOverridesAddsDisablesAndPreservesUnrelatedCrafting(GameTestHelper h){
        var doc=document();var a=ResourceLocation.parse("inscribed:a");var b=ResourceLocation.parse("inscribed:b");
        var data=Map.<ResourceLocation,JsonElement>of(a,JsonParser.parseString("{\"old\":true}"),b,JsonParser.parseString("{\"other\":true}"));
        doc.getAsJsonObject("enchantment_recipes").add("inscribed:a",JsonParser.parseString("{\"enabled\":false}"));
        doc.getAsJsonObject("enchantment_recipes").add("inscribed:b",JsonParser.parseString("{\"enabled\":true,\"new\":true}"));
        doc.getAsJsonObject("enchantment_recipes").add("inscribed:c",JsonParser.parseString("{\"added\":true}"));
        var merged=ConfigRecipeStore.merge(doc,data,"enchantment_recipes");
        h.assertTrue(!merged.containsKey(a) && merged.get(b).getAsJsonObject().has("new") && !merged.get(b).getAsJsonObject().has("enabled") && merged.containsKey(ResourceLocation.parse("inscribed:c")),"Config precedence/disable/add failed");
        doc.addProperty("include_datapack_recipes",false);doc.getAsJsonObject("enchantment_recipes").remove("inscribed:b");
        h.assertTrue(!ConfigRecipeStore.merge(doc,data,"enchantment_recipes").containsKey(b),"Config-only still included datapack ritual");
        h.assertTrue(ConfigRecipeStore.merge(doc,data,"crafting_recipes").containsKey(a),"Config-only removed unrelated crafting recipes");h.succeed();
    }
    @GameTest(template="test_altar") public static void malformedConfigKeepsLastValidFileWithoutOverwritingIt(GameTestHelper h)throws Exception{
        var path=ConfigRecipeStore.path();var original=Files.readString(path);
        try{
            var valid=ConfigRecipeStore.load();Files.writeString(path,"{ malformed");var fallback=ConfigRecipeStore.load();
            h.assertTrue(fallback.equals(valid) && Files.readString(path).equals("{ malformed"),"Malformed config discarded last valid settings or overwrote edits");
            for(String value:List.of("2","1.5","\"1\"","true")){var bad=document();bad.add("format_version",JsonParser.parseString(value));boolean rejected=false;try{ConfigRecipeStore.validateDocument(bad);}catch(RuntimeException ex){rejected=true;}h.assertTrue(rejected,"Bad format version accepted "+value);}
        }finally{Files.writeString(path,original);ConfigRecipeStore.load();}h.succeed();
    }
    @GameTest(template="test_altar",batch="zx_config_options") public static void anvilRestrictionCanBeConfigured(GameTestHelper h){
        var registry=h.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);var base=new ItemStack(Items.DIAMOND_SWORD);var result=base.copy();result.enchant(registry.getHolder(ResourceLocation.parse("minecraft:sharpness")).orElseThrow(),3);
        boolean old=InscribedConfig.RESTRICT_ANVIL.get();try{InscribedConfig.RESTRICT_ANVIL.set(true);h.assertTrue(!dev.inscribed.anvil.AnvilRestrictionService.allowed(base,result),"Enabled restriction did not block");InscribedConfig.RESTRICT_ANVIL.set(false);h.assertTrue(dev.inscribed.anvil.AnvilRestrictionService.allowed(base,result),"Disabled restriction still blocked");}finally{InscribedConfig.RESTRICT_ANVIL.set(old);}h.succeed();
    }
    @GameTest(template="test_altar",batch="zz_config_recipe_reload",timeoutTicks=500) public static void actualConfigReloadChangesRitualAndCraftingAndRestores(GameTestHelper h)throws Exception{
        var path=ConfigRecipeStore.path();var original=Files.readString(path);var changed=JsonParser.parseString(original).getAsJsonObject();
        var fortune=changed.getAsJsonObject("enchantment_recipes").getAsJsonObject("inscribed:minecraft/fortune_3");fortune.getAsJsonObject("extraction").addProperty("failure_chance",.75);fortune.getAsJsonObject("extraction").addProperty("duration_ticks",143);
        changed.getAsJsonObject("enchantment_recipes").add("inscribed:config_only_test",fortune.deepCopy());
        changed.getAsJsonObject("crafting_recipes").add("inscribed:infusion_pedestal",JsonParser.parseString("{\"type\":\"minecraft:crafting_shaped\",\"pattern\":[\"I\"],\"key\":{\"I\":{\"item\":\"minecraft:iron_ingot\"}},\"result\":{\"id\":\"inscribed:infusion_pedestal\",\"count\":2}}"));
        Files.writeString(path,new Gson().toJson(changed));long generation=InscribedRecipeRegistry.generation();
        var server=h.getLevel().getServer();server.reloadResources(server.getPackRepository().getSelectedIds());
        var restored=new boolean[]{false};var restoreGeneration=new long[]{0};var failure=new Throwable[]{null};
        h.succeedWhen(()->{
            if(!restored[0]) {
                h.assertTrue(InscribedRecipeRegistry.generation()>generation,"Reload pending");
                try {
                    var recipe=InscribedRecipeRegistry.recipes().stream().filter(r->r.id().equals(ResourceLocation.parse("inscribed:config_only_test"))).findFirst().orElseThrow();
                    h.assertTrue(recipe.extraction().orElseThrow().failureChance()==.75 && recipe.extraction().orElseThrow().durationTicks()==143,"Configured ritual fields ignored");
                    var crafting=(ShapedRecipe)server.getRecipeManager().byKey(ResourceLocation.parse("inscribed:infusion_pedestal")).orElseThrow().value();
                    h.assertTrue(crafting.getResultItem(h.getLevel().registryAccess()).getCount()==2 && crafting.matches(CraftingInput.of(1,1,List.of(new ItemStack(Items.IRON_INGOT))),h.getLevel()),"Configured crafting ignored or not usable");
                }catch(Throwable ex){failure[0]=ex;}finally {
                    try{Files.writeString(path,original);}catch(Exception ex){throw new RuntimeException(ex);}
                    restored[0]=true;restoreGeneration[0]=InscribedRecipeRegistry.generation();server.reloadResources(server.getPackRepository().getSelectedIds());
                }
                h.assertTrue(false,"Waiting for restoration reload");
            }
            h.assertTrue(InscribedRecipeRegistry.generation()>restoreGeneration[0],"Restoration pending");
            h.assertTrue(failure[0]==null,"Config reload verification failed: "+failure[0]);
            h.assertTrue(InscribedRecipeRegistry.recipes().stream().noneMatch(r->r.id().equals(ResourceLocation.parse("inscribed:config_only_test"))),"Removed config recipe survived reload");
        });
    }
}
