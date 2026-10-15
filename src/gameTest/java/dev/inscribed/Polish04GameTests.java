package dev.inscribed;

import com.google.gson.*;
import dev.inscribed.altar.*;
import dev.inscribed.config.InscribedConfig;
import dev.inscribed.loot.*;
import dev.inscribed.pedestal.*;
import dev.inscribed.recipe.*;
import dev.inscribed.registry.InscribedRegistries;
import dev.inscribed.ritual.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("inscribed") @PrefixGameTestTemplate(false)
public final class Polish04GameTests {
    private static final BlockPos T=new BlockPos(10,2,10),S=T.offset(0,0,-3);
    private static ResourceLocation id(String s){return ResourceLocation.parse(s);}
    private static EnchantingTableBlockEntity table(GameTestHelper h){
        h.setBlock(T,Blocks.ENCHANTING_TABLE);h.setBlock(S,Blocks.CHISELED_BOOKSHELF);
        for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)if(x*x+z*z<=20 && (x!=0 || z!=0))h.setBlock(T.offset(x,1,z),Blocks.BOOKSHELF);
        return (EnchantingTableBlockEntity)h.getBlockEntity(T);
    }
    @GameTest(template="test_altar") public static void countsExpandToDistinctSlotsAndOverBudgetRejects(GameTestHelper h){
        var json=JsonParser.parseString("{\"enchantment\":\"minecraft:sharpness\",\"level\":1,\"minimum_bookshelf_power\":0,\"infusion\":{\"ingredients\":[{\"item\":\"minecraft:diamond\",\"count\":8}],\"duration_ticks\":10,\"book_consumption_chance\":0}}").getAsJsonObject();
        var recipe=RecipeParser.parse(id("inscribed:legacy_count"),json,h.getLevel().registryAccess());
        h.assertTrue(recipe.infusion().orElseThrow().ingredients().size()==8 && RecipeConstraints.valid(recipe.infusion().get().ingredients()),"Legacy counts did not become eight unit costs");
        json.getAsJsonObject("infusion").getAsJsonArray("ingredients").get(0).getAsJsonObject().addProperty("count",9);
        boolean rejected=false;try{RecipeParser.parse(id("inscribed:too_many"),json,h.getLevel().registryAccess());}catch(IllegalArgumentException e){rejected=e.getMessage().contains("8 pedestals");}
        h.assertTrue(rejected,"Over-budget effective count accepted");
        var array=new JsonArray();for(int i=0;i<9;i++)array.add(JsonParser.parseString("{\"item\":\"minecraft:diamond\"}"));json.getAsJsonObject("infusion").add("ingredients",array);
        rejected=false;try{RecipeParser.parse(id("inscribed:too_many"),json,h.getLevel().registryAccess());}catch(IllegalArgumentException e){rejected=true;}
        h.assertTrue(rejected,"Nine explicit pedestal entries accepted");h.succeed();
    }
    @GameTest(template="test_altar") public static void legacySavedStackRemainsIntactAndCanBeRetrieved(GameTestHelper h){
        var pos=T.offset(-2,0,0);h.setBlock(pos,InscribedRegistries.PEDESTAL.get());var p=(PedestalBlockEntity)h.getBlockEntity(pos);
        var tag=p.saveWithFullMetadata(h.getLevel().registryAccess());tag.put("Item",new ItemStack(Items.DIAMOND,40).save(h.getLevel().registryAccess()));p.loadWithComponents(tag,h.getLevel().registryAccess());
        h.assertTrue(p.item().getCount()==40,"Upgrade truncated legacy saved stack");
        var player=new net.neoforged.neoforge.common.util.FakePlayer(h.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"Legacy04"));
        player.setPos(net.minecraft.world.phys.Vec3.atCenterOf(p.getBlockPos()).add(0,1,1));player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);InfusionPedestalBlock.interact(h.getLevel(),p.getBlockPos(),player,InteractionHand.MAIN_HAND);
        h.assertTrue(p.item().isEmpty() && player.getInventory().countItem(Items.DIAMOND)==40,"Legacy retrieval lost or duplicated items");
        boolean rejected=false;try{p.setItem(new ItemStack(Items.DIAMOND,2));}catch(IllegalArgumentException e){rejected=true;}
        h.assertTrue(rejected && p.item().isEmpty(),"Setter accepted multi-item stack or mutated on rejection");h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=230) public static void actualCatalogCatalystMissingThenInfusionCommits(GameTestHelper h){
        var table=table(h);var registry=h.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);var enchant=registry.getHolder(id("minecraft:fortune")).orElseThrow();
        AltarStateService.target(table,new ItemStack(Items.DIAMOND_PICKAXE));var book=new ItemStack(Items.ENCHANTED_BOOK);book.enchant(enchant,3);((ChiseledBookShelfBlockEntity)h.getBlockEntity(S)).setItem(0,book);
        var recipe=InscribedRecipeRegistry.recipes().stream().filter(r->r.id().equals(id("inscribed:minecraft/fortune_3"))).findFirst().orElseThrow();
        var pedestals=UnitPedestals.set(h,T,recipe.infusion().orElseThrow().ingredients().stream().map(c->c.ingredient().getItems()[0]).toList());
        var catalyst=pedestals.stream().filter(p->p.item().is(InscribedRegistries.ENCHANTMENT_CATALYST.get())).findFirst().orElseThrow();catalyst.setItem(ItemStack.EMPTY);
        var player=UUID.randomUUID();var result=RitualController.start(h.getLevel(),h.absolutePos(T),player);
        h.assertTrue(!result.started() && result.reason().equals("missing_catalyst") && AltarStateService.get(table).target().is(Items.DIAMOND_PICKAXE),"Missing catalyst not diagnosed safely: "+result.reason());
        catalyst.setItem(new ItemStack(InscribedRegistries.ENCHANTMENT_CATALYST.get()));h.assertTrue(RitualController.start(h.getLevel(),h.absolutePos(T),player).started(),"Catalyst did not enable real catalog recipe");
        h.runAtTickTime(185,()->{
            h.assertTrue(EnchantmentHelper.getEnchantmentsForCrafting(AltarStateService.get(table).target()).getLevel(enchant)==3 && catalyst.item().isEmpty(),"Catalyst infusion result/cost wrong");
            h.assertTrue(!AltarStateService.get(table).running() && AltarStateService.get(table).visualStrength(h.getLevel().getGameTime())>0,"Completion has no short visual fade");
        });
        h.runAtTickTime(210,()->{h.assertTrue(AltarStateService.get(table).visualStrength(h.getLevel().getGameTime())==0,"Completion animation persisted indefinitely");h.succeed();});
    }
    @GameTest(template="test_altar") public static void cancellationClearsNetworkAnimationWithoutConsumption(GameTestHelper h){
        var table=table(h);AltarStateService.target(table,new ItemStack(Items.IRON_SWORD));
        var book=new ItemStack(Items.ENCHANTED_BOOK);book.enchant(h.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(id("minecraft:sharpness")).orElseThrow(),1);((ChiseledBookShelfBlockEntity)h.getBlockEntity(S)).setItem(0,book);
        var pedestals=UnitPedestals.set(h,T,List.of(new ItemStack(Items.AMETHYST_SHARD,2),new ItemStack(Items.IRON_INGOT)));
        h.assertTrue(RitualController.start(h.getLevel(),h.absolutePos(T),UUID.randomUUID()).started(),"Fixture failed to start");
        var state=AltarStateService.get(table);var b=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),h.getLevel().registryAccess());
        try{AltarState.STREAM_CODEC.encode(b,state);var decoded=AltarState.STREAM_CODEC.decode(b);h.assertTrue(ItemStack.matches(decoded.target(),state.target()) && decoded.running()==state.running() && decoded.visualStart()==state.visualStart() && decoded.visualDuration()==state.visualDuration() && decoded.extraction()==state.extraction(),"Visual timing network roundtrip lost fields");}finally{b.release();}
        RitualController.cancelAt(h.getLevel(),h.absolutePos(T),"admin_cancelled");
        h.assertTrue(!AltarStateService.get(table).running() && AltarStateService.get(table).visualStrength(h.getLevel().getGameTime())==0 && pedestals.stream().allMatch(p->!p.running() && p.visualStrength(h.getLevel().getGameTime())==0),"Cancel left effects running");
        h.assertTrue(pedestals.stream().mapToInt(p->p.item().getCount()).sum()==3,"Cancellation consumed units");h.succeed();
    }
    private static LootContext context(GameTestHelper h,String table,long seed){
        var params=new LootParams.Builder(h.getLevel()).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(h.absolutePos(T))).create(LootContextParamSets.CHEST);
        return new LootContext.Builder(params).withOptionalRandomSeed(seed).withQueriedLootTableId(id(table)).create(Optional.empty());
    }
    @GameTest(template="test_altar") public static void realGlobalLootModifierAddsBooksAndCatalystsOnlyToTargets(GameTestHelper h){
        var modifier=new ExplorationLootModifier(new net.minecraft.world.level.storage.loot.predicates.LootItemCondition[0]);int books=0,catalysts=0;
        for(int seed=1;seed<=500;seed++){
            var loot=new it.unimi.dsi.fastutil.objects.ObjectArrayList<ItemStack>();loot.add(new ItemStack(Items.APPLE));modifier.apply(loot,context(h,"minecraft:chests/ancient_city",seed));
            h.assertTrue(loot.getFirst().is(Items.APPLE) && loot.size()<=4,"Injection replaced loot or flooded one roll");
            for(var item:loot){if(item.is(Items.ENCHANTED_BOOK)){books++;h.assertTrue(!EnchantmentHelper.getEnchantmentsForCrafting(item).isEmpty(),"Injected book has no enchantment");}if(item.is(InscribedRegistries.ENCHANTMENT_CATALYST.get()))catalysts++;}
        }
        h.assertTrue(books>90 && books<220 && catalysts>70 && catalysts<150,"Deterministic rarity outside reasonable range: "+books+"/"+catalysts);
        var unrelated=new ArrayList<ItemStack>();LootInjection.inject(unrelated,context(h,"minecraft:entities/zombie",1));h.assertTrue(unrelated.isEmpty(),"Injected structure rewards into unrelated loot");
        // Run the actual registered vanilla table through NeoForge's GLM path.
        var params=new LootParams.Builder(h.getLevel()).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(h.absolutePos(T))).create(LootContextParamSets.CHEST);
        var table=h.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,id("minecraft:chests/ancient_city")));
        boolean found=false;for(int seed=1;seed<=100;seed++)if(table.getRandomItems(params,seed).stream().anyMatch(s->s.is(InscribedRegistries.ENCHANTMENT_CATALYST.get()))){found=true;break;}
        h.assertTrue(found,"Registered GLM not applied to real vanilla loot table");h.succeed();
    }
    @GameTest(template="test_altar",batch="zx_loot_options") public static void independentLootTogglesDisableInjection(GameTestHelper h){
        boolean old=InscribedConfig.LOOT_INJECTION.get(),books=InscribedConfig.BOOK_LOOT.get(),catalysts=InscribedConfig.CATALYST_LOOT.get();
        try{
            InscribedConfig.LOOT_INJECTION.set(false);var empty=new ArrayList<ItemStack>();LootInjection.inject(empty,context(h,"minecraft:chests/ancient_city",123));h.assertTrue(empty.isEmpty(),"Global loot toggle ignored");
            InscribedConfig.LOOT_INJECTION.set(true);
            for(boolean onlyBooks:new boolean[]{true,false}){
                InscribedConfig.BOOK_LOOT.set(onlyBooks);InscribedConfig.CATALYST_LOOT.set(!onlyBooks);int generated=0;
                for(int seed=1;seed<=100;seed++){var loot=new ArrayList<ItemStack>();LootInjection.inject(loot,context(h,"minecraft:chests/ancient_city",seed));for(var item:loot){generated++;h.assertTrue(onlyBooks ? item.is(Items.ENCHANTED_BOOK) : item.is(InscribedRegistries.ENCHANTMENT_CATALYST.get()),"Independent loot switch ignored");}}
                h.assertTrue(generated>0,"Enabled reward type never generated");
            }
        }finally{InscribedConfig.LOOT_INJECTION.set(old);InscribedConfig.BOOK_LOOT.set(books);InscribedConfig.CATALYST_LOOT.set(catalysts);}h.succeed();
    }
    @GameTest(template="test_altar") public static void invalidLootWeightsAndRollsReject(GameTestHelper h){
        var json=JsonParser.parseString("{\"loot_tables\":[\"minecraft:chests/ancient_city\"],\"chance\":0.1,\"min_rolls\":1,\"max_rolls\":1,\"entries\":[{\"item\":\"inscribed:enchantment_catalyst\",\"weight\":1}]}").getAsJsonObject();
        h.assertTrue(LootInjection.parse(id("inscribed:valid"),json,h.getLevel().registryAccess()).entries().size()==1,"Valid loot pool rejected");
        for(String field:List.of("chance","max_rolls")){var bad=json.deepCopy();bad.addProperty(field,9);boolean rejected=false;try{LootInjection.parse(id("inscribed:bad"),bad,h.getLevel().registryAccess());}catch(RuntimeException e){rejected=true;}h.assertTrue(rejected,"Invalid "+field+" admitted");}
        var bad=json.deepCopy();bad.getAsJsonArray("entries").get(0).getAsJsonObject().addProperty("weight",0);boolean rejected=false;try{LootInjection.parse(id("inscribed:bad"),bad,h.getLevel().registryAccess());}catch(RuntimeException e){rejected=true;}h.assertTrue(rejected,"Zero weight admitted");h.succeed();
    }
    @GameTest(template="test_altar",batch="zz_loot_reload",timeoutTicks=500) public static void actualLootConfigReloadMalformedFallbackAndRestore(GameTestHelper h)throws Exception{
        var path=LootInjection.path();var original=java.nio.file.Files.readString(path);var edited=JsonParser.parseString(original).getAsJsonObject();
        edited.getAsJsonObject("pools").getAsJsonObject("inscribed:treasure_catalyst").addProperty("chance",1);
        java.nio.file.Files.writeString(path,new Gson().toJson(edited));
        var server=h.getLevel().getServer();long generation=InscribedRecipeRegistry.generation();server.reloadResources(server.getPackRepository().getSelectedIds());
        int[] stage={0};long[] previous={generation};Throwable[] failure={null};
        h.succeedWhen(()->{
            h.assertTrue(InscribedRecipeRegistry.generation()>previous[0],"Loot reload pending");
            try{
                var pool=LootInjection.pools().stream().filter(p->p.id().equals(id("inscribed:treasure_catalyst"))).findFirst().orElseThrow();
                if(stage[0]==0){
                    h.assertTrue(pool.chance()==1,"Loot config override not applied");
                    java.nio.file.Files.writeString(path,"{ malformed");previous[0]=InscribedRecipeRegistry.generation();stage[0]=1;server.reloadResources(server.getPackRepository().getSelectedIds());
                }else if(stage[0]==1){
                    h.assertTrue(pool.chance()==1 && java.nio.file.Files.readString(path).equals("{ malformed"),"Malformed config discarded previous pools or overwrote edits");
                    java.nio.file.Files.writeString(path,original);previous[0]=InscribedRecipeRegistry.generation();stage[0]=2;server.reloadResources(server.getPackRepository().getSelectedIds());
                }else{
                    h.assertTrue(pool.chance()==.22,"Original loot config not restored");
                    if(failure[0]!=null)throw new RuntimeException(failure[0]);return;
                }
            }catch(Throwable t){
                if(stage[0]==2)throw new RuntimeException(t);
                failure[0]=t;
                try{java.nio.file.Files.writeString(path,original);}catch(Exception e){throw new RuntimeException(e);}
                previous[0]=InscribedRecipeRegistry.generation();stage[0]=2;server.reloadResources(server.getPackRepository().getSelectedIds());
            }
            h.assertTrue(false,"Waiting for next loot reload");
        });
    }
}
