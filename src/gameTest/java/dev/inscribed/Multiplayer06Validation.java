package dev.inscribed;

import java.nio.file.*;
import dev.inscribed.altar.*;
import dev.inscribed.pedestal.PedestalBlockEntity;
import dev.inscribed.registry.InscribedRegistries;
import dev.inscribed.knowledge.KnowledgeService;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Real localhost players/packets, isolated test world. Not included in the JAR. */
@EventBusSubscriber(modid="inscribed")
public final class Multiplayer06Validation {
    private static final BlockPos T=new BlockPos(0,80,0), P=T.offset(-2,0,0), S=T.offset(0,0,-3);
    private static boolean setup, started, cancelled, complete, finished;
    private static int ticks, joins;
    private static ServerPlayer actor, observer;
    private static final ResourceLocation SHARP=ResourceLocation.parse("minecraft:sharpness");
    private static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){
        if(!Boolean.getBoolean("inscribed.qaMultiplayerServer")||!(e.getEntity() instanceof ServerPlayer p))return;
        var level=p.serverLevel();
        if(!setup){
            setup=true;
            for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)level.setBlockAndUpdate(new BlockPos(x,79,z),Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(T,Blocks.ENCHANTING_TABLE.defaultBlockState());level.setBlockAndUpdate(P,InscribedRegistries.PEDESTAL.get().defaultBlockState());level.setBlockAndUpdate(S,Blocks.CHISELED_BOOKSHELF.defaultBlockState());
            AltarStateService.target((EnchantingTableBlockEntity)level.getBlockEntity(T),ItemStack.EMPTY);
            ((PedestalBlockEntity)level.getBlockEntity(P)).setItem(new ItemStack(Items.BLAZE_ROD));
            var book=new ItemStack(Items.ENCHANTED_BOOK);book.enchant(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(SHARP).orElseThrow(),3);book.enchant(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(ResourceLocation.parse("minecraft:unbreaking")).orElseThrow(),2);
            ((ChiseledBookShelfBlockEntity)level.getBlockEntity(S)).setItem(0,book);
        }
        p.setGameMode(GameType.CREATIVE);p.teleportTo(.5,80,2.5);p.setYRot(180);p.setXRot(25);
        if(p.getScoreboardName().endsWith("_A")){actor=p;joins++;if(joins==1){p.getInventory().clearContent();p.getInventory().setItem(0,new ItemStack(Items.IRON_SWORD));var book=new ItemStack(Items.ENCHANTED_BOOK);book.enchant(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(SHARP).orElseThrow(),1);p.getInventory().setItem(2,book);}}
        else{observer=p;p.getInventory().clearContent();}
        p.getInventory().selected=0;p.inventoryMenu.broadcastChanges();
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(!Boolean.getBoolean("inscribed.qaMultiplayerServer")||finished)return;
        var server=e.getServer();
        try{
            if(++ticks>5000)throw new AssertionError("Multiplayer timeout");
            if(!setup||actor==null||observer==null)return;
            var level=server.overworld();var table=(EnchantingTableBlockEntity)level.getBlockEntity(T);var state=AltarStateService.get(table);
            if(state.running())started=true;
            if(started&&!cancelled&&server.getPlayerList().getPlayer(actor.getUUID())==null){
                check(!state.running(),"Departure did not cancel");check(!state.target().isEmpty(),"Cancellation lost target");check(((PedestalBlockEntity)level.getBlockEntity(P)).item().is(Items.BLAZE_ROD),"Cancellation consumed reagent");
                cancelled=true;Files.writeString(Path.of("cancelled.txt"),"PASS");
            }
            if(cancelled&&!state.target().isEmpty()&&EnchantmentHelper.getEnchantmentsForCrafting(state.target()).getLevel(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(SHARP).orElseThrow())==1)complete=true;
            if(complete&&state.target().isEmpty()){
                int swords=0;for(var stack:actor.getInventory().items)if(stack.is(Items.IRON_SWORD)){swords+=stack.getCount();check(EnchantmentHelper.getEnchantmentsForCrafting(stack).getLevel(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(SHARP).orElseThrow())==1,"Wrong output level");}
                check(swords==1,"Target duplication/loss");check(observer.getInventory().items.stream().noneMatch(s->s.is(Items.IRON_SWORD)),"Observer obtained a duplicate");
                check(((PedestalBlockEntity)level.getBlockEntity(P)).item().isEmpty(),"Reagent not consumed");check(!((ChiseledBookShelfBlockEntity)level.getBlockEntity(S)).getItem(0).isEmpty(),"Zero-risk reference book consumed");
                check(KnowledgeService.get(observer).knownEnchantments().isEmpty(),"Knowledge leaked to observer");check(KnowledgeService.get(actor).knownEnchantments().contains(SHARP),"Reconnect lost actor knowledge");
                Files.writeString(Path.of("results.json"),"{\"status\":\"PASS\",\"realPlayers\":2,\"joins\":"+joins+",\"cancelOnDisconnect\":true,\"inventoryConservation\":true,\"privateKnowledge\":true}");finished=true;server.halt(false);
            }
        }catch(Throwable failure){finished=true;try{Files.writeString(Path.of("results.json"),"{\"status\":\"FAIL\",\"error\":"+new com.google.gson.Gson().toJson(failure.toString())+"}");}catch(Exception ignored){}com.mojang.logging.LogUtils.getLogger().error("Multiplayer validation failed",failure);server.halt(false);}
    }
}
