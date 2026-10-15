package dev.inscribed;
import java.util.*;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import dev.inscribed.knowledge.*;
import dev.inscribed.recipe.EnchantmentRecipe;
import dev.inscribed.registry.InscribedRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("inscribed") @PrefixGameTestTemplate(false)
public final class KnowledgeGameTests {
    private static ResourceLocation id(String s){return ResourceLocation.parse(s);}
    private static FakePlayer player(GameTestHelper h){return new FakePlayer(h.getLevel(),new GameProfile(UUID.randomUUID(),"KnowledgeTest"));}
    @GameTest(template="test_altar") public static void visibilitySeparatesDiscoveryFromExplicitGrants(GameTestHelper h){
        var e=id("minecraft:fortune");var r=id("inscribed:fortune");var none=KnowledgeData.empty();var known=none.change(e,true,true);var explicit=none.change(r,false,true);
        for(var v:EnchantmentRecipe.Visibility.values()){
            var recipe=new EnchantmentRecipe(r,e,3,10,v,false,Optional.empty(),Optional.empty());
            h.assertTrue(RecipeVisibilityService.visible(recipe,none)==(v==EnchantmentRecipe.Visibility.ALWAYS),"Unknown player sees non-public recipe");
            h.assertTrue(RecipeVisibilityService.visible(recipe,known)==(v!=EnchantmentRecipe.Visibility.EXPLICIT_DISCOVERY),"Enchantment discovery bypassed explicit grant");
            h.assertTrue(RecipeVisibilityService.visible(recipe,explicit)==(v!=EnchantmentRecipe.Visibility.ON_ENCHANTMENT_DISCOVERY),"Explicit recipe grant changed enchantment discovery");
        }h.succeed();
    }
    @GameTest(template="test_altar") public static void knowledgeRoundTripRetainsStaleStableIdsAndIsIdempotent(GameTestHelper h){
        var data=KnowledgeData.empty().change(id("removed_mod:old_enchantment"),true,true).change(id("inscribed:removed_recipe"),false,true);
        var encoded=KnowledgeData.CODEC.encodeStart(JsonOps.INSTANCE,data).getOrThrow();var decoded=KnowledgeData.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow();
        h.assertTrue(data.equals(decoded)&&decoded.equals(decoded.change(id("removed_mod:old_enchantment"),true,true)),"Stable identity/idempotence lost");
        h.assertTrue(decoded.change(id("removed_mod:old_enchantment"),true,false).knownRecipes().size()==1,"Forgetting an enchantment erased recipe grants");h.succeed();
    }
    @GameTest(template="test_altar") public static void snapshotIsPrivatePerPlayerAndFiltersUnknownRecipeMetadata(GameTestHelper h){
        var a=player(h);var b=player(h);var enchant=id("minecraft:fortune");a.setData(InscribedRegistries.KNOWLEDGE,KnowledgeData.empty().change(enchant,true,true));
        var va=KnowledgeService.snapshot(a);var vb=KnowledgeService.snapshot(b);
        h.assertTrue(va.enchantments().stream().anyMatch(e->e.id().equals(enchant))&&vb.enchantments().isEmpty(),"Knowledge shared between players");
        for(var r:dev.inscribed.recipe.InscribedRecipeRegistry.recipes())if(r.visibility()==EnchantmentRecipe.Visibility.ON_ENCHANTMENT_DISCOVERY&&r.enchantment().equals(enchant))h.assertTrue(va.recipes().stream().anyMatch(v->v.id().equals(r.id()))&&vb.recipes().stream().noneMatch(v->v.id().equals(r.id())),"Per-player recipe filter failed");
        var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),h.getLevel().registryAccess());
        try{GuidePayload.STREAM_CODEC.encode(buffer,new GuidePayload(false,vb));var round=GuidePayload.STREAM_CODEC.decode(buffer);h.assertTrue(round.snapshot().enchantments().isEmpty()&&round.snapshot().recipes().size()==vb.recipes().size(),"Private network snapshot changed");}finally{buffer.release();}h.succeed();
    }
    @GameTest(template="test_altar") public static void playerAttachmentActuallyPersistsInEntityNbt(GameTestHelper h){
        var a=player(h);var expected=KnowledgeData.empty().change(id("minecraft:fortune"),true,true).change(id("inscribed:explicit_recipe"),false,true);a.setData(InscribedRegistries.KNOWLEDGE,expected);
        var nbt=a.saveWithoutId(new net.minecraft.nbt.CompoundTag());var b=player(h);b.load(nbt);
        h.assertTrue(KnowledgeService.get(b).equals(expected),"Knowledge attachment missing after player NBT reload");h.succeed();
    }
    @GameTest(template="test_altar") public static void completedPickupLearnsEveryBookEnchantmentOnlyOnce(GameTestHelper h){
        var p=player(h);var stack=new ItemStack(Items.ENCHANTED_BOOK);var reg=h.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        for(var s:List.of("fortune","unbreaking"))stack.enchant(reg.getHolder(id("minecraft:"+s)).orElseThrow(),2);
        var item=new net.minecraft.world.entity.item.ItemEntity(h.getLevel(),0,0,0,ItemStack.EMPTY);
        var event=new net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Post(p,item,stack);
        KnowledgeEvents.pickup(event);var first=KnowledgeService.get(p);KnowledgeEvents.pickup(event);
        h.assertTrue(first.knownEnchantments().size()==2&&first.equals(KnowledgeService.get(p)),"Pickup did not learn both enchantments idempotently");h.succeed();
    }
    @GameTest(template="test_altar") public static void inventoryAcquisitionAndAdminForgetDoNotContinuouslyRelearn(GameTestHelper h){
        var p=player(h);p.tickCount=10;KnowledgeEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
        var book=new ItemStack(Items.ENCHANTED_BOOK);book.enchant(h.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(id("minecraft:fortune")).orElseThrow(),3);p.getInventory().setItem(0,book);
        p.tickCount=20;KnowledgeEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));h.assertTrue(KnowledgeService.get(p).knownEnchantments().contains(id("minecraft:fortune")),"Actual possession did not learn");
        KnowledgeService.change(p,id("minecraft:fortune"),true,false);p.tickCount=30;KnowledgeEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));h.assertTrue(KnowledgeService.get(p).knownEnchantments().isEmpty(),"Unchanged inventory immediately undid admin forget");
        KnowledgeEvents.logout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(p));h.succeed();
    }
    @GameTest(template="test_altar") public static void successfulTradeTeachesTheResultBook(GameTestHelper h){
        var p=player(h);var book=new ItemStack(Items.ENCHANTED_BOOK);book.enchant(h.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(id("minecraft:mending")).orElseThrow(),1);
        var offer=new net.minecraft.world.item.trading.MerchantOffer(new net.minecraft.world.item.trading.ItemCost(Items.EMERALD,1),book,12,1,.05f);
        KnowledgeEvents.trade(new net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent(p,offer,null));
        h.assertTrue(KnowledgeService.get(p).knownEnchantments().contains(id("minecraft:mending")),"Completed book trade did not teach its enchantment");h.succeed();
    }
    @GameTest(template="test_altar") public static void deathCloneRetainsPrivateKnowledge(GameTestHelper h){
        var original=player(h);var expected=KnowledgeData.empty().change(id("minecraft:fortune"),true,true).change(id("inscribed:explicit_recipe"),false,true);original.setData(InscribedRegistries.KNOWLEDGE,expected);
        var respawned=player(h);respawned.restoreFrom(original,false);
        h.assertTrue(KnowledgeService.get(respawned).equals(expected),"Knowledge lost on death clone");h.succeed();
    }
    @GameTest(template="test_altar") public static void largePrivateCatalogIsSplitIntoBoundedAtomicBatches(GameTestHelper h){
        var p=player(h);var entries=new ArrayList<GuideSnapshot.KnownEnchantment>();for(int i=0;i<5000;i++)entries.add(new GuideSnapshot.KnownEnchantment(id("test:enchantment_"+i),net.minecraft.network.chat.Component.literal("Known name"),net.minecraft.network.chat.Component.literal("Known name"),3));
        var packets=KnowledgeService.packets(p,true,new GuideSnapshot(12,entries,List.of()));int total=0;long seq=packets.getFirst().sequence();
        h.assertTrue(packets.size()>1&&packets.getLast().complete()&&packets.getLast().open(),"Large catalog not split with final open marker");
        for(int i=0;i<packets.size();i++){
            var packet=packets.get(i);h.assertTrue(packet.sequence()==seq&&packet.complete()==(i==packets.size()-1),"Batch boundaries lost");
            var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),h.getLevel().registryAccess());try{GuidePayload.STREAM_CODEC.encode(buffer,packet);h.assertTrue(buffer.readableBytes()<750000,"Packet exceeds safe budget");total+=GuidePayload.STREAM_CODEC.decode(buffer).snapshot().enchantments().size();}finally{buffer.release();}
        }h.assertTrue(total==5000,"Chunking dropped records");h.succeed();
    }
}
