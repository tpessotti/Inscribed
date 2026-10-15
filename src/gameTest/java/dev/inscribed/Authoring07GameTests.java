package dev.inscribed;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import java.nio.file.*;
import java.util.*;
import dev.inscribed.altar.*;
import dev.inscribed.recipe.*;
import dev.inscribed.ritual.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("inscribed_authoring") @PrefixGameTestTemplate(false)
public final class Authoring07GameTests {
    private static final BlockPos T=new BlockPos(10,2,10),S=T.offset(0,0,-3);
    private static ResourceLocation id(String s){return ResourceLocation.parse(s);}
    private static EnchantingTableBlockEntity setup(GameTestHelper h,String enchantment){
        h.setBlock(T,Blocks.ENCHANTING_TABLE);h.setBlock(S,Blocks.CHISELED_BOOKSHELF);h.setBlock(T.offset(0,0,3),Blocks.BOOKSHELF);h.setBlock(T.offset(1,0,3),Blocks.BOOKSHELF);
        var table=(EnchantingTableBlockEntity)h.getBlockEntity(T);AltarStateService.target(table,new ItemStack(enchantment.equals("minecraft:sharpness")?Items.IRON_SWORD:Items.IRON_PICKAXE));
        var book=new ItemStack(Items.ENCHANTED_BOOK);book.enchant(h.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(id(enchantment)).orElseThrow(),3);((ChiseledBookShelfBlockEntity)h.getBlockEntity(S)).setItem(0,book);
        UnitPedestals.set(h,T,List.of(new ItemStack(Items.FLINT),new ItemStack(Items.AMETHYST_SHARD),new ItemStack(Items.IRON_INGOT)));return table;
    }
    @GameTest(template="test_altar") public static void actualExamplePackLoadsRecipesLootAndConditionalCreate(GameTestHelper h){
        var ids=InscribedRecipeRegistry.recipes().stream().map(EnchantmentRecipe::id).collect(java.util.stream.Collectors.toSet());
        h.assertTrue(ids.contains(id("example:sharpness_basic"))&&ids.contains(id("example:fortune_advanced")),"Example pack recipes did not load");h.assertTrue(ids.contains(id("example:create_capacity"))==ModList.get().isLoaded("create"),"Conditional Create recipe violated optional dependency");
        for(var name:List.of("shipwreck","mineshaft","high_tier"))h.assertTrue(dev.inscribed.loot.LootInjection.pools().stream().anyMatch(p->p.id().equals(id("example:"+name))),"Example loot pool missing: "+name);h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=160) public static void exampleBasicRecipeExecutesPhysicalInfusion(GameTestHelper h){
        var table=setup(h,"minecraft:sharpness");h.assertTrue(RitualController.startSelected(h.getLevel(),table.getBlockPos(),UUID.randomUUID(),id("example:sharpness_basic"),RitualType.INFUSION).started(),"Example infusion did not match");
        h.runAtTickTime(110,()->{var enchant=h.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(id("minecraft:sharpness")).orElseThrow();h.assertTrue(EnchantmentHelper.getEnchantmentsForCrafting(AltarStateService.get(table).target()).getLevel(enchant)==1,"Example produced wrong level");h.succeed();});
    }
    @GameTest(template="test_altar",batch="zz_author_reload",timeoutTicks=500) public static void authorEditsEnchantmentReloadsExecutesAndRestoresPack(GameTestHelper h)throws Exception{
        var path=Path.of("world/datapacks/inscribed_example_datapack/data/example/inscribed/enchantment_recipes/sharpness_basic.json");var original=Files.readString(path);var doc=JsonParser.parseString(original).getAsJsonObject();doc.addProperty("enchantment","minecraft:unbreaking");doc.getAsJsonObject("infusion").addProperty("book_consumption_chance",0);doc.getAsJsonObject("infusion").addProperty("duration_ticks",40);Files.writeString(path,doc.toString());
        var server=h.getLevel().getServer();long generation=InscribedRecipeRegistry.generation();server.reloadResources(server.getPackRepository().getSelectedIds());var phase=new int[]{0};var restoreGeneration=new long[]{0};var table=new EnchantingTableBlockEntity[1];var error=new Throwable[1];
        h.succeedWhen(()->{
            if(phase[0]==0){h.assertTrue(InscribedRecipeRegistry.generation()>generation,"Author reload pending");try{
                var recipe=InscribedRecipeRegistry.recipes().stream().filter(r->r.id().equals(id("example:sharpness_basic"))).findFirst().orElseThrow();h.assertTrue(recipe.enchantment().equals(id("minecraft:unbreaking")),"Datapack edit was not applied");table[0]=setup(h,"minecraft:unbreaking");h.assertTrue(RitualController.startSelected(h.getLevel(),table[0].getBlockPos(),UUID.randomUUID(),recipe.id(),RitualType.INFUSION).started(),"Edited recipe not executable");phase[0]=1;
            }catch(Throwable failure){error[0]=failure;phase[0]=2;}h.assertTrue(false,"Awaiting edited ritual");}
            if(phase[0]==1){h.assertTrue(!AltarStateService.get(table[0]).running(),"Edited ritual pending");try{var enchant=h.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(id("minecraft:unbreaking")).orElseThrow();h.assertTrue(EnchantmentHelper.getEnchantmentsForCrafting(AltarStateService.get(table[0]).target()).getLevel(enchant)==1,"Edited recipe result incorrect");}catch(Throwable failure){error[0]=failure;}phase[0]=2;}
            if(phase[0]==2){try{Files.writeString(path,original);}catch(Exception failure){throw new RuntimeException(failure);}restoreGeneration[0]=InscribedRecipeRegistry.generation();server.reloadResources(server.getPackRepository().getSelectedIds());phase[0]=3;h.assertTrue(false,"Awaiting pack restoration");}
            h.assertTrue(InscribedRecipeRegistry.generation()>restoreGeneration[0],"Pack restoration pending");h.assertTrue(error[0]==null,"Author workflow failed: "+error[0]);h.assertTrue(InscribedRecipeRegistry.recipes().stream().anyMatch(r->r.id().equals(id("example:sharpness_basic"))&&r.enchantment().equals(id("minecraft:sharpness"))),"Example pack not restored");
        });
    }
}
