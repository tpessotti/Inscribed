package dev.inscribed;

import com.google.gson.*;
import dev.inscribed.altar.*;
import dev.inscribed.config.InscribedConfig;
import dev.inscribed.loot.LootInjection;
import dev.inscribed.recipe.*;
import dev.inscribed.ritual.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("inscribed") @PrefixGameTestTemplate(false)
public final class Progression05GameTests {
    private static final BlockPos T=new BlockPos(10,2,10),S=T.offset(0,0,-3);
    private static ResourceLocation id(String s){return ResourceLocation.parse(s);}
    private static List<EnchantmentRecipe> catalog(){return InscribedRecipeRegistry.recipes().stream().filter(r->r.id().getPath().startsWith("minecraft/") || r.id().getPath().startsWith("create/") || r.id().getPath().equals("test_sharpness_1")).toList();}
    private static LootContext context(GameTestHelper h,String table,long seed){var params=new LootParams.Builder(h.getLevel()).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(h.absolutePos(T))).create(LootContextParamSets.CHEST);return new LootContext.Builder(params).withOptionalRandomSeed(seed).withQueriedLootTableId(id(table)).create(Optional.empty());}
    @GameTest(template="test_altar") public static void allCatalogTiersAndSourcesRemainConsistent(GameTestHelper h){
        var known=EnumSet.noneOf(ProgressionTier.class);
        for(var r:catalog()){
            int tier=r.tier().ordinal();known.add(r.tier());h.assertTrue(tier>0 && RecipeConstraints.valid(r.infusion().orElseThrow().ingredients()) && RecipeConstraints.valid(r.extraction().orElseThrow().ingredients()),"Unclassified or invalid unit recipe "+r.id());
            double risk=r.infusion().get().bookConsumptionChance();double[] min={0,.05,.15,.30,.50,.60},max={0,.15,.30,.50,.75,.85};h.assertTrue(risk>=min[tier] && risk<=max[tier],"Risk does not fit tier "+r.id());
            h.assertTrue(r.extraction().get().failureChance()==.2,"User-confirmed extraction risk changed");
            boolean source=LootInjection.pools().stream().flatMap(p->p.entries().stream()).anyMatch(e->r.enchantment().equals(e.enchantment()) && e.maxLevel()>=r.level());h.assertTrue(source,"No injected source for "+r.id());
        }
        h.assertTrue(known.size()==5,"Missing progression tier");
        for(var p:LootInjection.pools())for(var e:p.entries())if(e.enchantment()!=null)for(int level=e.minLevel();level<=e.maxLevel();level++){
            final int n=level;var r=catalog().stream().filter(x->x.enchantment().equals(e.enchantment()) && x.level()==n).findFirst();h.assertTrue(r.isPresent() && r.get().tier().ordinal()<=p.structureTier(),"Loot has no recipe or exceeds host tier "+p.id()+" "+e.enchantment()+" "+level);
        }
        h.succeed();
    }
    private static void power(GameTestHelper h,int n){for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)if(x*x+z*z<=25){h.setBlock(T.offset(x,1,z),n-->0 ? Blocks.BOOKSHELF : Blocks.AIR);}}
    @GameTest(template="test_altar") public static void earlyMidLateInfrastructureGatesRealRecipes(GameTestHelper h){
        h.setBlock(T,Blocks.ENCHANTING_TABLE);h.setBlock(S,Blocks.CHISELED_BOOKSHELF);var table=(EnchantingTableBlockEntity)h.getBlockEntity(T);var shelf=(ChiseledBookShelfBlockEntity)h.getBlockEntity(S);
        for(String recipeId:List.of("inscribed:test_sharpness_1","inscribed:minecraft/unbreaking_2","inscribed:minecraft/fortune_3","inscribed:minecraft/mending_1","inscribed:minecraft/efficiency_5")){
            var r=catalog().stream().filter(x->x.id().equals(id(recipeId))).findFirst().orElseThrow();AltarStateService.target(table,new ItemStack(Items.BOOK));var book=new ItemStack(Items.ENCHANTED_BOOK);book.enchant(h.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(r.enchantment()).orElseThrow(),r.level());shelf.setItem(0,book);
            UnitPedestals.set(h,T,r.infusion().orElseThrow().ingredients().stream().map(c->c.ingredient().getItems()[0]).toList());
            power(h,r.minimumPower()-1);h.assertTrue(RitualMatcher.matches(h.getLevel(),AltarScanner.scan(h.getLevel(),h.absolutePos(T))).stream().noneMatch(m->m.recipe().id().equals(r.id())),"A rare early book bypassed infrastructure "+r.id());
            power(h,r.minimumPower());h.assertTrue(RitualController.startSelected(h.getLevel(),h.absolutePos(T),UUID.randomUUID(),r.id(),RitualType.INFUSION).started(),"Appropriate tier infrastructure failed "+r.id());RitualController.cancelAt(h.getLevel(),h.absolutePos(T),"admin_cancelled");
        }
        h.succeed();
    }
    @GameTest(template="test_altar") public static void oceanMiningCombatGeneratedBooksMatchThemeAndTier(GameTestHelper h){
        var themes=Map.of("minecraft:chests/shipwreck_treasure",Set.of("respiration","aqua_affinity","depth_strider","luck_of_the_sea","lure","loyalty","riptide","impaling","channeling","unbreaking"),
          "minecraft:chests/abandoned_mineshaft",Set.of("efficiency","fortune","unbreaking","protection","capacity","potato_recovery"),
          "minecraft:chests/pillager_outpost",Set.of("sharpness","smite","bane_of_arthropods","power","punch","knockback","looting","fire_aspect","flame","piercing","quick_charge","multishot","protection","blast_protection","projectile_protection","fire_protection","thorns","binding_curse","vanishing_curse"));
        for(var row:themes.entrySet()){
            int books=0;
            for(int seed=1;seed<=300;seed++){var loot=new ArrayList<ItemStack>();LootInjection.inject(loot,context(h,row.getKey(),seed));for(var stack:loot)if(stack.is(Items.ENCHANTED_BOOK)){
                books++;for(var entry:EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()){
                    var enchant=entry.getKey().unwrapKey().orElseThrow().location();h.assertTrue(row.getValue().contains(enchant.getPath()),"Wrong thematic reward "+enchant+" in "+row.getKey());
                    var r=catalog().stream().filter(x->x.enchantment().equals(enchant) && x.level()==entry.getIntValue()).findFirst().orElseThrow();h.assertTrue(r.tier().ordinal()<=2,"High-tier book in low-tier source");
                }
            }}
            h.assertTrue(books>10 && books<120,"Thematic loot flooded or never occurred: "+row.getKey()+" "+books);
        }
        h.succeed();
    }
    @GameTest(template="test_altar",batch="zx_progression_multipliers") public static void lootMultipliersRespectZeroAndTreasureSeparation(GameTestHelper h){
        double all=InscribedConfig.LOOT_MULTIPLIER.get(),treasure=InscribedConfig.TREASURE_LOOT_MULTIPLIER.get();
        try{
            InscribedConfig.LOOT_MULTIPLIER.set(0.0);for(int i=1;i<=30;i++){var loot=new ArrayList<ItemStack>();LootInjection.inject(loot,context(h,"minecraft:chests/ancient_city",i));h.assertTrue(loot.isEmpty(),"Zero global multiplier ignored");}
            InscribedConfig.LOOT_MULTIPLIER.set(1.0);InscribedConfig.TREASURE_LOOT_MULTIPLIER.set(0.0);int mid=0;
            for(int i=1;i<=100;i++){var high=new ArrayList<ItemStack>();LootInjection.inject(high,context(h,"minecraft:chests/ancient_city",i));h.assertTrue(high.isEmpty(),"Zero treasure multiplier ignored");var loot=new ArrayList<ItemStack>();mid+=LootInjection.inject(loot,context(h,"minecraft:chests/shipwreck_treasure",i));}
            h.assertTrue(mid>0,"Treasure multiplier disabled unrelated mid-tier loot");
        }finally{InscribedConfig.LOOT_MULTIPLIER.set(all);InscribedConfig.TREASURE_LOOT_MULTIPLIER.set(treasure);}h.succeed();
    }
    @GameTest(template="test_altar") public static void invalidLootEntryDoesNotDiscardOtherValidRewards(GameTestHelper h){
        var data=JsonParser.parseString("{\"loot_tables\":[\"minecraft:chests/ancient_city\"],\"chance\":0.2,\"min_rolls\":1,\"max_rolls\":1,\"entries\":[{\"enchantment\":\"inscribed:missing\",\"weight\":10,\"min_level\":1,\"max_level\":1},{\"item\":\"inscribed:enchantment_catalyst\",\"weight\":1}]}").getAsJsonObject();
        var pool=LootInjection.parse(id("inscribed:partial_validation"),data,h.getLevel().registryAccess());h.assertTrue(pool.entries().size()==1 && pool.totalWeight()==1,"Invalid book destroyed valid catalyst entry");h.succeed();
    }
    @GameTest(template="test_altar") public static void missingTierIsBackwardCompatibleAndInvalidTierRejects(GameTestHelper h){
        var json=JsonParser.parseString("{\"enchantment\":\"minecraft:sharpness\",\"level\":1,\"minimum_bookshelf_power\":2,\"infusion\":{\"ingredients\":[],\"duration_ticks\":10,\"book_consumption_chance\":0.1}}").getAsJsonObject();
        h.assertTrue(RecipeParser.parse(id("inscribed:old_custom"),json,h.getLevel().registryAccess()).tier()==ProgressionTier.UNCLASSIFIED,"Old custom recipe acquired an arbitrary enforced tier");
        json.addProperty("progression_tier","TIER_99");boolean rejected=false;try{RecipeParser.parse(id("inscribed:bad_tier"),json,h.getLevel().registryAccess());}catch(IllegalArgumentException e){rejected=true;}h.assertTrue(rejected,"Invalid tier admitted");h.succeed();
    }
    @GameTest(template="test_altar",batch="zx_progression_audit") public static void operatorDiagnosticsAndDisabledSourcesAreAudited(GameTestHelper h)throws Exception{
        var p=new net.neoforged.neoforge.common.util.FakePlayer(h.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"Audit05"));
        var dispatcher=h.getLevel().getServer().getCommands().getDispatcher();var plain=p.createCommandSourceStack().withPermission(0);
        String[] commands={"inscribed debug balance inscribed:minecraft/fortune_3","inscribed debug loot table minecraft:chests/ancient_city","inscribed debug loot enchantment minecraft:fortune","inscribed debug progression report"};
        for(var command:commands){boolean denied=false;try{dispatcher.execute(command,plain);}catch(com.mojang.brigadier.exceptions.CommandSyntaxException e){denied=true;}h.assertTrue(denied,"Non-operator accessed "+command);h.assertTrue(dispatcher.execute(command,plain.withPermission(2))>0,"Operator debug failed "+command);}
        h.assertTrue(ProgressionAudit.warnings().isEmpty(),"Default runtime source audit has warnings: "+ProgressionAudit.warnings());
        boolean old=InscribedConfig.CATALYST_LOOT.get(),trade=InscribedConfig.CATALYST_TRADE.get();try{
            InscribedConfig.CATALYST_LOOT.set(false);InscribedConfig.CATALYST_TRADE.set(true);
            h.assertTrue(ProgressionAudit.warnings().stream().noneMatch(s->s.startsWith("NO_CATALYST_SOURCE")),"Librarian source was not recognized");
            InscribedConfig.CATALYST_TRADE.set(false);h.assertTrue(ProgressionAudit.warnings().stream().anyMatch(s->s.startsWith("NO_CATALYST_SOURCE")),"Disabled required catalyst sources not detected");
        }finally{InscribedConfig.CATALYST_LOOT.set(old);InscribedConfig.CATALYST_TRADE.set(trade);}h.succeed();
    }
}
