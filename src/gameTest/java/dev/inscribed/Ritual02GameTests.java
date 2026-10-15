package dev.inscribed;

import com.mojang.authlib.GameProfile;
import dev.inscribed.altar.*;
import dev.inscribed.recipe.*;
import dev.inscribed.ritual.*;
import dev.inscribed.pedestal.PedestalBlockEntity;
import dev.inscribed.registry.InscribedRegistries;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("inscribed") @PrefixGameTestTemplate(false)
public final class Ritual02GameTests {
    private static final BlockPos T=new BlockPos(10,2,10),A=T.offset(-2,0,0),B=T.offset(2,0,0),S=T.offset(0,0,-3);
    private record Rig(GameTestHelper h,FakePlayer player,EnchantingTableBlockEntity table,PedestalBlockEntity a,PedestalBlockEntity b,ChiseledBookShelfBlockEntity shelf) {
        BlockPos pos(){return h.absolutePos(T);}
        RitualController.StartResult start(String recipe){return RitualController.startSelected(h.getLevel(),pos(),player.getUUID(),ResourceLocation.parse(recipe),RitualType.EXTRACTION);}
    }
    private static Rig rig(GameTestHelper h) {
        h.setBlock(T,Blocks.ENCHANTING_TABLE);h.setBlock(A,InscribedRegistries.PEDESTAL.get());h.setBlock(B,InscribedRegistries.PEDESTAL.get());h.setBlock(S,Blocks.CHISELED_BOOKSHELF);
        h.setBlock(T.offset(0,0,3),Blocks.BOOKSHELF);h.setBlock(T.offset(1,0,3),Blocks.BOOKSHELF);
        var p=new FakePlayer(h.getLevel(),new GameProfile(UUID.randomUUID(),"Ritual02"));p.setPos(Vec3.atCenterOf(h.absolutePos(T)).add(0,1,1));
        var r=new Rig(h,p,(EnchantingTableBlockEntity)h.getBlockEntity(T),(PedestalBlockEntity)h.getBlockEntity(A),(PedestalBlockEntity)h.getBlockEntity(B),(ChiseledBookShelfBlockEntity)h.getBlockEntity(S));
        var target=new ItemStack(Items.IRON_PICKAXE);target.enchant(ench(h,"fortune"),3);target.setDamageValue(42);target.set(DataComponents.CUSTOM_NAME,Component.literal("Kept name"));AltarStateService.target(r.table(),target);
        r.a().setItem(new ItemStack(Items.PAPER));r.b().setItem(new ItemStack(Items.QUARTZ));r.shelf().setItem(0,new ItemStack(Items.BOOK));r.shelf().setItem(1,new ItemStack(Items.BOOK));return r;
    }
    private static Holder<Enchantment> ench(GameTestHelper h,String id){return h.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(ResourceLocation.parse(id.contains(":")?id:"minecraft:"+id)).orElseThrow();}
    private static int target(Rig r,String id){return EnchantmentHelper.getEnchantmentsForCrafting(AltarStateService.get(r.table()).target()).getLevel(ench(r.h(),id));}
    private static int book(Rig r,int slot,String id){return EnchantmentHelper.getEnchantmentsForCrafting(r.shelf().getItem(slot)).getLevel(ench(r.h(),id));}
    @GameTest(template="test_altar",timeoutTicks=60) public static void extractionTransfersOneEnchantmentAndPreservesComponents(GameTestHelper h) {
        var r=rig(h);var item=AltarStateService.get(r.table()).target();item.enchant(ench(h,"efficiency"),4);item.enchant(ench(h,"unbreaking"),3);AltarStateService.target(r.table(),item);
        h.assertTrue(r.start("inscribed:test_02_fortune_3").started(),"Extraction did not start");
        h.assertTrue(target(r,"fortune")==3 && r.shelf().getItem(0).is(Items.BOOK),"Extraction consumed before commit");
        h.runAtTickTime(25,()->{var result=AltarStateService.get(r.table()).target();h.assertTrue(target(r,"fortune")==0 && target(r,"efficiency")==4 && target(r,"unbreaking")==3,"Extraction removed other enchantments");
            h.assertTrue(result.getDamageValue()==42 && result.getHoverName().getString().equals("Kept name"),"Extraction lost other components");
            h.assertTrue(book(r,0,"fortune")==3 && r.shelf().getItem(1).isEmpty() && r.a().item().getCount()==0 && r.b().item().getCount()==0,"Wrong exact books/cost/output placement");h.succeed();});
    }
    @GameTest(template="test_altar") public static void extractionRejectsMissingBooksPowerIngredientsAndUnconfiguredLevels(GameTestHelper h) {
        var r=rig(h);r.shelf().removeItemNoUpdate(1);h.assertTrue(!r.start("inscribed:test_02_fortune_3").started(),"Insufficient books accepted");r.shelf().setItem(1,new ItemStack(Items.BOOK));
        r.a().setItem(ItemStack.EMPTY);h.assertTrue(!r.start("inscribed:test_02_fortune_3").started(),"Missing ingredient accepted");r.a().setItem(new ItemStack(Items.PAPER));
        h.setBlock(T.offset(0,0,3),Blocks.AIR);h.setBlock(T.offset(1,0,3),Blocks.AIR);h.assertTrue(!r.start("inscribed:test_02_fortune_3").started(),"Missing power accepted");h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=60) public static void selectionOnlyOfferedOptionsNonceAndReplayProtection(GameTestHelper h) {
        var r=selectionRig(h);
        var options=RitualSelection.open(r.player(),r.pos());h.assertTrue(options!=null && options.options().size()==2,"Selection leaked unavailable recipes");
        h.assertTrue(!RitualSelection.choose(r.player(),UUID.randomUUID(),0).started(),"Forged nonce accepted");
        int index=java.util.stream.IntStream.range(0,options.options().size()).filter(i->options.options().get(i).name().getString().contains("Fortune")).findFirst().orElseThrow();
        h.assertTrue(RitualSelection.choose(r.player(),options.token(),index).started(),"Valid option rejected");h.assertTrue(!RitualSelection.choose(r.player(),options.token(),index).started(),"Replay accepted");
        h.runAtTickTime(25,()->{h.assertTrue(target(r,"fortune")==3 && target(r,"efficiency")==0,"Infusion choice applied incorrect enchantment");h.succeed();});
    }
    private static Rig selectionRig(GameTestHelper h) {
        var r=rig(h);AltarStateService.target(r.table(),new ItemStack(Items.IRON_PICKAXE));
        for(var name:List.of("fortune","efficiency")){var b=new ItemStack(Items.ENCHANTED_BOOK);b.enchant(ench(h,name),name.equals("fortune")?3:4);r.shelf().setItem(name.equals("fortune")?2:3,b);}
        r.a().setItem(new ItemStack(Items.PRISMARINE_SHARD));r.b().setItem(new ItemStack(Items.GLOWSTONE_DUST));
        var third=T.offset(-2,0,-1);h.setBlock(third,InscribedRegistries.PEDESTAL.get());((PedestalBlockEntity)h.getBlockEntity(third)).setItem(new ItemStack(Items.GLOWSTONE_DUST));return r;
    }
    @GameTest(template="test_altar") public static void selectionRevalidatesChangedPhysicalInputs(GameTestHelper h) {
        var r=selectionRig(h);
        var options=RitualSelection.open(r.player(),r.pos());r.b().setItem(ItemStack.EMPTY);
        h.assertTrue(!RitualSelection.choose(r.player(),options.token(),0).started() && target(r,"fortune")==0 && r.shelf().getItem(0).is(Items.BOOK),"Stale selection started or consumed inputs");h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=60) public static void extractionIngredientsSelectWithoutScreen(GameTestHelper h) {
        var r=rig(h);var item=AltarStateService.get(r.table()).target();item.enchant(ench(h,"efficiency"),4);AltarStateService.target(r.table(),item);
        h.assertTrue(RitualSelection.open(r.player(),r.pos())==null,"Extraction opened a selector");
        var matches=RitualMatcher.matches(h.getLevel(),AltarScanner.scan(h.getLevel(),r.pos()));
        h.assertTrue(matches.size()==1 && matches.getFirst().enchantment().equals(ench(h,"fortune")),"Ingredients did not select Fortune");
        r.player().setShiftKeyDown(true);
        AltarInteraction.rightClick(new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(r.player(),net.minecraft.world.InteractionHand.MAIN_HAND,r.pos(),new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(r.pos()),Direction.UP,r.pos(),false)));
        h.assertTrue(RitualController.state(h.getLevel(),r.pos())==RitualState.RUNNING,"Physical extraction did not start directly");
        h.runAtTickTime(25,()->{h.assertTrue(target(r,"fortune")==0 && target(r,"efficiency")==4 && book(r,0,"fortune")==3,"Ingredient-selected extraction incorrect");h.succeed();});
    }
    @GameTest(template="test_altar") public static void ambiguousExtractionRejectsWithoutConsumingOrOpeningSelection(GameTestHelper h) {
        var r=rig(h);var item=AltarStateService.get(r.table()).target();item.enchant(ench(h,"efficiency"),4);AltarStateService.target(r.table(),item);
        var third=T.offset(0,0,2);h.setBlock(third,InscribedRegistries.PEDESTAL.get());((PedestalBlockEntity)h.getBlockEntity(third)).setItem(new ItemStack(Items.LAPIS_LAZULI));
        var result=RitualController.start(h.getLevel(),r.pos(),r.player().getUUID());
        h.assertTrue(!result.started() && result.reason().equals("ambiguous_extraction") && RitualSelection.open(r.player(),r.pos())==null,"Ambiguous extraction silently chose or opened UI");
        h.assertTrue(target(r,"fortune")==3 && r.shelf().getItem(0).is(Items.BOOK) && r.a().item().getCount()==1,"Ambiguity consumed resources");h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=60) public static void failedExtractionDestroysOnlyChosenEnchantmentAndConsumesExactCosts(GameTestHelper h) {
        var r=rig(h);var item=AltarStateService.get(r.table()).target();item.enchant(ench(h,"efficiency"),4);item.enchant(ench(h,"unbreaking"),3);AltarStateService.target(r.table(),item);r.b().setItem(new ItemStack(Items.COAL));
        h.assertTrue(RitualController.start(h.getLevel(),r.pos(),r.player().getUUID()).started(),"Failure fixture did not start");
        h.assertTrue(target(r,"fortune")==3 && r.shelf().getItem(0).is(Items.BOOK),"Failure occurred before commit");
        h.runAtTickTime(25,()->{var out=AltarStateService.get(r.table()).target();
            h.assertTrue(target(r,"fortune")==0 && target(r,"efficiency")==4 && target(r,"unbreaking")==3,"Failed extraction lost other enchantments");
            h.assertTrue(out.getDamageValue()==42 && out.getHoverName().getString().equals("Kept name"),"Failure lost components");
            h.assertTrue(r.shelf().getItem(0).isEmpty() && r.shelf().getItem(1).isEmpty() && r.a().item().getCount()==0 && r.b().item().getCount()==0,"Failed extraction produced book or consumed wrong costs");h.succeed();});
    }
    @GameTest(template="test_altar") public static void failureChanceSchemaDefaultsAndRejectsInvalidValues(GameTestHelper h) {
        var base=com.google.gson.JsonParser.parseString("{\"enchantment\":\"minecraft:fortune\",\"level\":3,\"minimum_bookshelf_power\":0,\"extraction\":{\"ingredients\":[],\"duration_ticks\":20,\"empty_books_required\":1}}").getAsJsonObject();
        h.assertTrue(RecipeParser.parse(ResourceLocation.parse("inscribed:risk_test"),base,h.getLevel().registryAccess()).extraction().orElseThrow().failureChance()==0,"Old recipe gained failure chance");
        for(String value:List.of("-0.1","1.1","\"0.2\"","true","null")){var data=base.deepCopy();data.getAsJsonObject("extraction").add("failure_chance",com.google.gson.JsonParser.parseString(value));boolean rejected=false;try{RecipeParser.parse(ResourceLocation.parse("inscribed:risk_test"),data,h.getLevel().registryAccess());}catch(IllegalArgumentException ex){rejected=true;}h.assertTrue(rejected,"Invalid failure_chance accepted: "+value);}
        for(double chance:new double[]{0,.2,1}){var data=base.deepCopy();data.getAsJsonObject("extraction").addProperty("failure_chance",chance);h.assertTrue(RecipeParser.parse(ResourceLocation.parse("inscribed:risk_test"),data,h.getLevel().registryAccess()).extraction().orElseThrow().failureChance()==chance,"Valid chance rejected");}h.succeed();
    }
    private static void cancellation(GameTestHelper h,int mutation) {
        var r=rig(h);h.assertTrue(r.start("inscribed:test_02_fortune_3").started(),"Start failed");
        switch(mutation){case 0->r.shelf().removeItemNoUpdate(1);case 1->r.a().setItem(ItemStack.EMPTY);case 2->h.setBlock(A,Blocks.AIR);case 3->AltarStateService.target(r.table(),ItemStack.EMPTY);case 4->{h.setBlock(T.offset(0,0,3),Blocks.AIR);h.setBlock(T.offset(1,0,3),Blocks.AIR);}}
        h.runAtTickTime(4,()->{h.assertTrue(RitualController.state(h.getLevel(),r.pos())==RitualState.IDLE && r.shelf().getItem(0).is(Items.BOOK) && r.b().item().getCount()==1,"Cancellation consumed or duplicated untouched inputs");h.succeed();});
    }
    @GameTest(template="test_altar") public static void extractionBookRemovalCancels(GameTestHelper h){cancellation(h,0);}
    @GameTest(template="test_altar") public static void extractionIngredientRemovalCancels(GameTestHelper h){cancellation(h,1);}
    @GameTest(template="test_altar") public static void extractionPedestalRemovalCancels(GameTestHelper h){cancellation(h,2);}
    @GameTest(template="test_altar") public static void extractionTargetRemovalCancels(GameTestHelper h){cancellation(h,3);}
    @GameTest(template="test_altar") public static void extractionPowerRemovalCancels(GameTestHelper h){cancellation(h,4);}
    @GameTest(template="test_altar",batch="zzz_extraction_reload",timeoutTicks=300) public static void realReloadCancelsExtractionAndSelection(GameTestHelper h) {
        var r=rig(h);h.assertTrue(r.start("inscribed:test_02_fortune_3").started(),"Start failed");
        h.getLevel().getServer().reloadResources(h.getLevel().getServer().getPackRepository().getSelectedIds());
        h.runAtTickTime(10,()->{h.assertTrue(RitualController.state(h.getLevel(),r.pos())==RitualState.IDLE && target(r,"fortune")==3 && r.shelf().getItem(0).is(Items.BOOK),"Reload consumed extraction inputs");h.succeed();});
    }
    @GameTest(template="test_altar",timeoutTicks=60) public static void cursesRequireRecipeAndRemainWhenUnselected(GameTestHelper h) {
        var r=rig(h);var item=AltarStateService.get(r.table()).target();item.enchant(ench(h,"vanishing_curse"),1);AltarStateService.target(r.table(),item);
        h.assertTrue(RitualMatcher.matches(h.getLevel(),AltarScanner.scan(h.getLevel(),r.pos())).stream().noneMatch(m->m.enchantment().equals(ench(h,"vanishing_curse"))),"Curse without sufficient configured power offered");
        h.assertTrue(r.start("inscribed:test_02_fortune_3").started(),"Start failed");h.runAtTickTime(25,()->{h.assertTrue(target(r,"vanishing_curse")==1 && target(r,"fortune")==0,"Unselected curse lost");h.succeed();});
    }
    @GameTest(template="test_altar",timeoutTicks=60) public static void extractionPreservesNonstandardEnchantedTargetType(GameTestHelper h){
        var r=rig(h);var item=new ItemStack(Items.BOOK);item.enchant(ench(h,"fortune"),3);AltarStateService.target(r.table(),item);
        h.assertTrue(r.start("inscribed:test_02_fortune_3").started(),"Nonstandard enchanted target rejected");
        h.runAtTickTime(25,()->{h.assertTrue(AltarStateService.get(r.table()).target().is(Items.BOOK) && target(r,"fortune")==0 && book(r,0,"fortune")==3,"Extraction changed target item type");h.succeed();});
    }
    private static void specialExtraction(GameTestHelper h,String name){
        var r=rig(h);for(int x=-4;x<=4;x++)h.setBlock(T.offset(x,0,3),Blocks.BOOKSHELF);h.setBlock(T.offset(0,0,4),Blocks.BOOKSHELF);
        var item=AltarStateService.get(r.table()).target();item.enchant(ench(h,name),1);AltarStateService.target(r.table(),item);
        r.a().setItem(new ItemStack(Items.AMETHYST_SHARD));r.b().setItem(new ItemStack(name.equals("mending")?Items.ECHO_SHARD:Items.PHANTOM_MEMBRANE));
        var third=T.offset(-2,0,-1);h.setBlock(third,InscribedRegistries.PEDESTAL.get());((PedestalBlockEntity)h.getBlockEntity(third)).setItem(new ItemStack(Items.AMETHYST_SHARD));
        h.assertTrue(r.start("inscribed:test_02_"+name+"_1").started(),"Configured special extraction rejected");
        h.runAtTickTime(110,()->{h.assertTrue(target(r,name)==0 && target(r,"fortune")==3 && book(r,0,name)==1,"Special extraction lost unselected enchantment or output");h.succeed();});
    }
    @GameTest(template="test_altar",timeoutTicks=150) public static void configuredCurseCanBeExtracted(GameTestHelper h){specialExtraction(h,"vanishing_curse");}
    @GameTest(template="test_altar",timeoutTicks=150) public static void configuredTreasureCanBeExtracted(GameTestHelper h){specialExtraction(h,"mending");}
    @GameTest(template="test_altar",timeoutTicks=220) public static void createRegisteredEnchantmentExtractsThroughGenericRegistry(GameTestHelper h) {
        if(!ModList.get().isLoaded("create")){h.succeed();return;}
        var r=rig(h);for(int x=-4;x<=4;x++)h.setBlock(T.offset(x,0,3),Blocks.BOOKSHELF);h.setBlock(T.offset(0,0,4),Blocks.BOOKSHELF);
        var item=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:copper_backtank")));item.enchant(ench(h,"create:capacity"),3);AltarStateService.target(r.table(),item);
        var recipe=InscribedRecipeRegistry.recipes().stream().filter(x->x.id().toString().equals("inscribed:create/capacity_3")).findFirst().orElseThrow();
        UnitPedestals.set(h,T,recipe.extraction().orElseThrow().ingredients().stream().map(c->c.ingredient().getItems()[0].copyWithCount(1)).toList());
        for(int x=-4;x<=4;x++)h.setBlock(T.offset(x,1,3),Blocks.BOOKSHELF);for(int x=-1;x<=1;x++)h.setBlock(T.offset(x,1,4),Blocks.BOOKSHELF);
        h.assertTrue(r.start("inscribed:create/capacity_3").started(),"Production Create extraction rejected");
        h.runAtTickTime(190,()->{h.assertTrue(target(r,"create:capacity")==0 && (book(r,0,"create:capacity")==3 || r.shelf().getItem(0).isEmpty()) && r.a().item().isEmpty() && r.b().item().isEmpty(),"Modded production extraction transaction failed");h.succeed();});
    }
    private static AnvilMenu anvil(Rig r,ItemStack left,ItemStack right) {var menu=new AnvilMenu(5,r.player().getInventory(),ContainerLevelAccess.create(r.h().getLevel(),r.pos()));menu.getSlot(0).set(left);menu.getSlot(1).set(right);menu.createResult();return menu;}
    @GameTest(template="test_altar") public static void anvilBlocksBookTransferMergeAndCopyEvenCreative(GameTestHelper h) {
        var r=rig(h);
        for(boolean creative:new boolean[]{false,true}) {
            r.player().getAbilities().instabuild=creative;
            var book=new ItemStack(Items.ENCHANTED_BOOK);book.enchant(ench(h,"sharpness"),3);
            h.assertTrue(anvil(r,new ItemStack(Items.DIAMOND_SWORD),book).getSlot(2).getItem().isEmpty(),"Book transfer escaped anvil guard");
            var a=new ItemStack(Items.DIAMOND_SWORD);a.enchant(ench(h,"sharpness"),3);a.setDamageValue(300);
            var b=a.copy();h.assertTrue(anvil(r,a,b).getSlot(2).getItem().isEmpty(),"Level merge escaped guard");
            b=new ItemStack(Items.DIAMOND_SWORD);b.enchant(ench(h,"unbreaking"),3);b.setDamageValue(100);h.assertTrue(anvil(r,a,b).getSlot(2).getItem().isEmpty(),"New enchantment copied through repair");
        }h.succeed();
    }
    @GameTest(template="test_altar") public static void anvilRenameAndPureRepairsPreserveEnchantmentsAndPenalty(GameTestHelper h) {
        var r=rig(h);var sword=new ItemStack(Items.DIAMOND_SWORD);sword.enchant(ench(h,"sharpness"),3);sword.setDamageValue(400);sword.set(DataComponents.REPAIR_COST,3);
        var renamed=anvil(r,sword.copy(),ItemStack.EMPTY);renamed.setItemName("Corsair's Blade");var output=renamed.getSlot(2).getItem();
        h.assertTrue(!output.isEmpty() && output.getHoverName().getString().equals("Corsair's Blade") && EnchantmentHelper.getEnchantmentsForCrafting(output).getLevel(ench(h,"sharpness"))==3,"Rename blocked or lost enchantment");
        var material=anvil(r,sword.copy(),new ItemStack(Items.DIAMOND));output=material.getSlot(2).getItem();h.assertTrue(!output.isEmpty() && output.getDamageValue()<400 && output.getOrDefault(DataComponents.REPAIR_COST,0)>=3,"Material repair or prior-work penalty changed");
        var other=new ItemStack(Items.DIAMOND_SWORD);other.setDamageValue(300);output=anvil(r,sword.copy(),other).getSlot(2).getItem();h.assertTrue(!output.isEmpty() && output.getDamageValue()<400 && EnchantmentHelper.getEnchantmentsForCrafting(output).getLevel(ench(h,"sharpness"))==3,"Pure item repair blocked");h.succeed();
    }
    @GameTest(template="test_altar") public static void grindstoneStaysDestructiveAndPreservesCurses(GameTestHelper h) {
        var r=rig(h);var sword=new ItemStack(Items.DIAMOND_SWORD);sword.enchant(ench(h,"sharpness"),3);sword.enchant(ench(h,"vanishing_curse"),1);
        var menu=new GrindstoneMenu(6,r.player().getInventory(),ContainerLevelAccess.create(h.getLevel(),r.pos()));menu.getSlot(0).set(sword);
        var result=menu.getSlot(2).getItem();h.assertTrue(!result.isEmpty() && EnchantmentHelper.getEnchantmentsForCrafting(result).getLevel(ench(h,"sharpness"))==0 && EnchantmentHelper.getEnchantmentsForCrafting(result).getLevel(ench(h,"vanishing_curse"))==1,"Grindstone ceased vanilla destructive behavior");h.succeed();
    }
}
