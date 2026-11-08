package dev.inscribed;

import com.mojang.authlib.GameProfile;
import dev.inscribed.config.InscribedConfig;
import dev.inscribed.registry.InscribedRegistries;
import dev.inscribed.trade.CatalystTrades;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("inscribed") @PrefixGameTestTemplate(false)
public final class CatalystAcquisitionGameTests {
    private static Villager librarian(GameTestHelper h,int level){
        var v=new Villager(EntityType.VILLAGER,h.getLevel());v.setVillagerData(v.getVillagerData().setProfession(VillagerProfession.LIBRARIAN).setLevel(level));
        v.setPos(Vec3.atCenterOf(h.absolutePos(new BlockPos(2,2,2))));v.setNoAi(true);h.getLevel().addFreshEntity(v);return v;
    }
    private static MerchantOffer catalyst(Villager v){return v.getOffers().stream().filter(o->CatalystTrades.catalyst(o.getResult())).findFirst().orElseThrow();}
    @GameTest(template="test_altar",batch="zx_catalyst_01") public static void librarianAddsOneOfferWithoutReplacingVanillaTrades(GameTestHelper h){
        var v=librarian(h,4);var offers=v.getOffers();var trade=catalyst(v);int size=offers.size();
        h.assertTrue(size>1&&trade.getBaseCostA().is(Items.EMERALD)&&trade.getBaseCostA().getCount()==24&&trade.getResult().getCount()==1&&trade.getMaxUses()==4,"Default librarian offer or vanilla offers missing");
        for(int i=0;i<25;i++)v.getOffers();h.assertTrue(v.getOffers().size()==size&&v.getOffers().stream().filter(o->CatalystTrades.catalyst(o.getResult())).count()==1,"Duplicate offers added on repeated access");
        h.assertTrue(librarian(h,1).getOffers().stream().noneMatch(o->CatalystTrades.catalyst(o.getResult())),"Novice sells advanced catalyst");
        var farmer=librarian(h,4);farmer.setVillagerData(farmer.getVillagerData().setProfession(VillagerProfession.FARMER));
        h.assertTrue(farmer.getOffers().stream().noneMatch(o->CatalystTrades.catalyst(o.getResult())),"Unrelated profession was changed");h.succeed();
    }
    @GameTest(template="test_altar",batch="zx_catalyst_02") public static void togglePreservesSavedOfferStockAndRestoresIt(GameTestHelper h){
        boolean old=InscribedConfig.CATALYST_TRADE.get();try{
            var v=librarian(h,4);var original=catalyst(v);original.increaseUses();original.addToSpecialPriceDiff(-2);
            var tag=new CompoundTag();v.saveWithoutId(tag);var loaded=new Villager(EntityType.VILLAGER,h.getLevel());loaded.load(tag);
            var restored=catalyst(loaded);h.assertTrue(restored.getUses()==1&&restored.getCostA().getCount()==22,"Saved stock or discount changed");
            InscribedConfig.CATALYST_TRADE.set(false);h.assertTrue(catalyst(loaded).isOutOfStock(),"Disabled offer still available");
            var disabledTag=new CompoundTag();loaded.saveWithoutId(disabledTag);var again=new Villager(EntityType.VILLAGER,h.getLevel());again.load(disabledTag);
            h.assertTrue(catalyst(again).isOutOfStock()&&catalyst(again).getUses()==1,"Saving while disabled erased stock history");
            InscribedConfig.CATALYST_TRADE.set(true);h.assertTrue(catalyst(loaded)==restored&&restored.getUses()==1&&restored.getCostA().getCount()==22&&!restored.isOutOfStock()&&catalyst(again).getUses()==1,"Re-enable replaced the saved offer or erased stock");
        }finally{InscribedConfig.CATALYST_TRADE.set(old);}h.succeed();
    }
    @GameTest(template="test_altar",batch="zx_catalyst_03") public static void pendingPurchaseCannotBypassDisabledConfig(GameTestHelper h){
        boolean old=InscribedConfig.CATALYST_TRADE.get();try{
            var v=librarian(h,4);var offer=catalyst(v);var p=new FakePlayer(h.getLevel(),new GameProfile(UUID.randomUUID(),"CatalystTradeQA"));
            var menu=new MerchantMenu(401,p.getInventory(),v);p.containerMenu=menu;v.setTradingPlayer(p);
            menu.setSelectionHint(v.getOffers().indexOf(offer));menu.getSlot(0).set(new ItemStack(Items.EMERALD,24));
            h.assertTrue(menu.getSlot(2).getItem().is(InscribedRegistries.ENCHANTMENT_CATALYST.get()),"Actual merchant menu produced no catalyst");
            InscribedConfig.CATALYST_TRADE.set(false);
            h.assertTrue(!menu.getSlot(2).mayPickup(p),"Pending result slot did not enforce disabled trade");
            menu.clicked(2,0,ClickType.PICKUP,p);menu.clicked(2,0,ClickType.QUICK_MOVE,p);
            h.assertTrue(menu.getCarried().isEmpty()&&p.getInventory().countItem(InscribedRegistries.ENCHANTMENT_CATALYST.get())==0&&menu.getSlot(0).getItem().getCount()==24&&offer.getUses()==0,"Disabled pending trade: cursor="+menu.getCarried()+", inventory="+p.getInventory().countItem(InscribedRegistries.ENCHANTMENT_CATALYST.get())+", emeralds="+menu.getSlot(0).getItem()+", uses="+offer.getUses());
            InscribedConfig.CATALYST_TRADE.set(true);menu.setSelectionHint(v.getOffers().indexOf(offer));menu.clicked(2,0,ClickType.PICKUP,p);
            h.assertTrue(menu.getCarried().is(InscribedRegistries.ENCHANTMENT_CATALYST.get())&&menu.getSlot(0).getItem().isEmpty()&&offer.getUses()==1,"Re-enabled actual purchase failed");
        }finally{InscribedConfig.CATALYST_TRADE.set(old);}h.succeed();
    }
    @GameTest(template="test_altar",batch="zx_catalyst_04") public static void configuredLevelPriceAndStockApplyToNewOffers(GameTestHelper h){
        int level=InscribedConfig.CATALYST_TRADE_LEVEL.get(),price=InscribedConfig.CATALYST_EMERALD_COST.get(),stock=InscribedConfig.CATALYST_TRADE_MAX_USES.get();
        try{InscribedConfig.CATALYST_TRADE_LEVEL.set(3);InscribedConfig.CATALYST_EMERALD_COST.set(32);InscribedConfig.CATALYST_TRADE_MAX_USES.set(2);
            var trade=catalyst(librarian(h,3));h.assertTrue(trade.getBaseCostA().getCount()==32&&trade.getMaxUses()==2,"Configured price/stock/level ignored");
        }finally{InscribedConfig.CATALYST_TRADE_LEVEL.set(level);InscribedConfig.CATALYST_EMERALD_COST.set(price);InscribedConfig.CATALYST_TRADE_MAX_USES.set(stock);}h.succeed();
    }
    private static int catalystLoot(GameTestHelper h,ResourceLocation id,int samples){
        var table=h.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,id));
        var params=new LootParams.Builder(h.getLevel()).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(h.absolutePos(new BlockPos(2,2,2)))).create(LootContextParamSets.CHEST);
        int count=0;for(int seed=1;seed<=samples;seed++)for(var item:table.getRandomItems(params,seed))if(CatalystTrades.catalyst(item))count+=item.getCount();return count;
    }
    @GameTest(template="test_altar",batch="zx_catalyst_05") public static void actualChestLootAndLibrarianSwitchesAreIndependent(GameTestHelper h){
        boolean loot=InscribedConfig.CATALYST_LOOT.get(),trade=InscribedConfig.CATALYST_TRADE.get(),global=InscribedConfig.LOOT_INJECTION.get();
        try{
            var table=ResourceLocation.parse("minecraft:chests/ancient_city");InscribedConfig.LOOT_INJECTION.set(true);InscribedConfig.CATALYST_LOOT.set(true);InscribedConfig.CATALYST_TRADE.set(false);
            h.assertTrue(catalystLoot(h,table,120)>0,"Real chest loot never injected a catalyst");
            InscribedConfig.CATALYST_LOOT.set(false);InscribedConfig.CATALYST_TRADE.set(true);
            h.assertTrue(catalystLoot(h,table,120)==0&&catalyst(librarian(h,4))!=null,"Independent switches failed");
            InscribedConfig.LOOT_INJECTION.set(false);InscribedConfig.CATALYST_LOOT.set(true);h.assertTrue(catalystLoot(h,table,60)==0,"Global loot switch ignored");
        }finally{InscribedConfig.CATALYST_LOOT.set(loot);InscribedConfig.CATALYST_TRADE.set(trade);InscribedConfig.LOOT_INJECTION.set(global);}h.succeed();
    }
    @GameTest(template="test_altar",batch="zx_catalyst_06") public static void readmeExampleUsesTheActualServerParser(GameTestHelper h)throws Exception{
        var path=java.nio.file.Path.of(System.getProperty("inscribed.readmeExample"));
        var json=com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(path)).getAsJsonObject();
        var recipe=dev.inscribed.recipe.RecipeParser.parse(ResourceLocation.parse("example:sharpness_1"),json,h.getLevel().registryAccess());
        h.assertTrue(recipe.enchantment().toString().equals("minecraft:sharpness")&&recipe.level()==1&&recipe.minimumPower()==2
                &&recipe.infusion().orElseThrow().ingredients().size()==3&&recipe.extraction().orElseThrow().ingredients().size()==2
                &&recipe.extraction().orElseThrow().failureChance()==.2,"Published recipe example disagrees with the server parser");h.succeed();
    }
}
