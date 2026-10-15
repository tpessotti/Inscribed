package dev.inscribedqa.client;

import java.nio.file.*;
import java.util.*;
import dev.inscribed.altar.*;
import dev.inscribed.client.*;
import dev.inscribed.config.ConfigRecipeStore;
import dev.inscribed.knowledge.KnowledgeService;
import dev.inscribed.pedestal.PedestalBlockEntity;
import dev.inscribed.registry.InscribedRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.lwjgl.glfw.GLFW;

/** Native client smoke against the real RC JAR, starting with a newly generated world. */
@EventBusSubscriber(modid="inscribed_qa",value=Dist.CLIENT)
public final class ReleaseSmokeClient {
    private static final BlockPos T=new BlockPos(0,80,0),S=T.offset(0,0,-3),P1=T.offset(-2,0,0),P2=T.offset(2,0,0),P3=T.offset(0,0,2);
    private static final ResourceLocation SHARP=ResourceLocation.parse("minecraft:sharpness");
    private static final Path OUT=Path.of("screenshots/rc-smoke");
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static int phase,ticks,wait;private static boolean opened,finished;private static volatile boolean ready;private static volatile Throwable error;
    private static String capture,originalConfig;
    private static boolean active(){return Boolean.getBoolean("inscribed.rcActiveRestart");}
    private static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    private static void next(int p){phase=p;wait=0;com.mojang.logging.LogUtils.getLogger().info("RC smoke phase {}",p);}
    private static void server(java.util.function.Consumer<ServerPlayer> action){ready=false;var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->{try{action.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));ready=true;}catch(Throwable t){error=t;ready=true;}});}
    private static EnchantingTableBlockEntity table(){return (EnchantingTableBlockEntity)Minecraft.getInstance().level.getBlockEntity(T);}
    private static PedestalBlockEntity pedestal(BlockPos pos){return (PedestalBlockEntity)Minecraft.getInstance().level.getBlockEntity(pos);}
    private static void use(BlockPos pos,int slot,boolean shift){var mc=Minecraft.getInstance();mc.setScreen(null);mc.player.getInventory().selected=slot;mc.player.setShiftKeyDown(shift);mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player,shift?ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY:ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        if(!Boolean.getBoolean("inscribed.rcSmoke")||finished)return;var mc=Minecraft.getInstance();GLFW.glfwHideWindow(mc.getWindow().getWindow());mc.options.pauseOnLostFocus=false;
        try{
            if(++ticks>3000)throw new AssertionError("RC smoke timeout phase "+phase);if(error!=null)throw new AssertionError("RC server fixture failed",error);if(mc.getOverlay()!=null)return;
            if(mc.player==null){if(!opened&&mc.screen!=null){opened=true;mc.createWorldOpenFlows().openWorld("InscribedValidation",()->finish(new AssertionError("World cancelled")));}return;}
            if(capture!=null||++wait<12)return;
            switch(phase){
                case 0->{
                    mc.options.renderDistance().set(3);mc.options.guiScale().set(3);mc.resizeDisplay();
                    if(Boolean.getBoolean("inscribed.rcSmokePersistence")){
                        server(p->{if(active()){try{Files.writeString(ConfigRecipeStore.path(),Files.readString(Path.of("rc-original-recipes.json")));}catch(Exception x){throw new RuntimeException(x);}}var l=p.serverLevel();var t=(EnchantingTableBlockEntity)l.getBlockEntity(T);check(t!=null&&AltarStateService.get(t).target().is(Items.IRON_SWORD)&&!AltarStateService.get(t).running(),"Idle target lost/resumed on restart");check(((PedestalBlockEntity)l.getBlockEntity(P3)).item().is(Items.IRON_INGOT),"Saved pedestal lost input");if(!active())check(KnowledgeService.get(p).knownEnchantments().contains(SHARP),"Saved knowledge lost");if(active()){check(((PedestalBlockEntity)l.getBlockEntity(P1)).item().is(Items.AMETHYST_SHARD)&&((PedestalBlockEntity)l.getBlockEntity(P2)).item().is(Items.AMETHYST_SHARD),"Active restart consumed ingredients");check(EnchantmentHelper.getEnchantmentsForCrafting(AltarStateService.get(t).target()).isEmpty(),"Active restart applied enchantment");}check(ConfigRecipeStore.load().getAsJsonObject("enchantment_recipes").getAsJsonObject("inscribed:test_sharpness_1").getAsJsonObject("extraction").get("failure_chance").getAsDouble()==.2,"Test risk override was not restored");});next(90);return;
                    }
                    server(p->{var l=p.serverLevel();p.getInventory().clearContent();KnowledgeService.clear(p);for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++){l.setBlockAndUpdate(new BlockPos(x,79,z),Blocks.STONE_BRICKS.defaultBlockState());for(int y=80;y<=84;y++)l.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());}
                        l.setBlockAndUpdate(T,Blocks.ENCHANTING_TABLE.defaultBlockState());l.setBlockAndUpdate(S,Blocks.CHISELED_BOOKSHELF.defaultBlockState());l.setBlockAndUpdate(T.offset(-2,0,-3),Blocks.BOOKSHELF.defaultBlockState());l.setBlockAndUpdate(T.offset(2,0,-3),Blocks.BOOKSHELF.defaultBlockState());for(var pos:List.of(P1,P2,P3))l.setBlockAndUpdate(pos,InscribedRegistries.PEDESTAL.get().defaultBlockState());
                        var book=new ItemStack(Items.ENCHANTED_BOOK);book.enchant(p.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(SHARP).orElseThrow(),1);((ChiseledBookShelfBlockEntity)l.getBlockEntity(S)).setItem(0,book);p.getInventory().setItem(0,new ItemStack(Items.IRON_SWORD));p.getInventory().setItem(1,new ItemStack(InscribedRegistries.GUIDE_BOOK.get()));p.getInventory().setItem(2,new ItemStack(Items.AMETHYST_SHARD));p.getInventory().setItem(3,new ItemStack(Items.AMETHYST_SHARD));p.getInventory().setItem(4,new ItemStack(Items.IRON_INGOT));p.setGameMode(GameType.SURVIVAL);p.teleportTo(.5,80,3.5);p.setYRot(180);p.setXRot(25);p.inventoryMenu.broadcastChanges();
                        try{originalConfig=Files.readString(ConfigRecipeStore.path());if(active())Files.writeString(Path.of("rc-original-recipes.json"),originalConfig);var cfg=com.google.gson.JsonParser.parseString(originalConfig).getAsJsonObject();var entries=cfg.getAsJsonObject("enchantment_recipes");for(var r:dev.inscribed.recipe.InscribedRecipeRegistry.recipes())if(!entries.has(r.id().toString())){var disabled=new com.google.gson.JsonObject();disabled.addProperty("enabled",false);entries.add(r.id().toString(),disabled);}if(active())cfg.getAsJsonObject("enchantment_recipes").getAsJsonObject("inscribed:test_sharpness_1").getAsJsonObject("infusion").addProperty("duration_ticks",1200);cfg.getAsJsonObject("enchantment_recipes").getAsJsonObject("inscribed:test_sharpness_1").getAsJsonObject("extraction").addProperty("failure_chance",0);Files.writeString(ConfigRecipeStore.path(),cfg.toString());p.server.reloadResources(p.server.getPackRepository().getSelectedIds());}catch(Exception t){throw new RuntimeException(t);}
                    });next(1);
                }
                case 1->{if(!ready||wait<35)return;mc.player.getInventory().selected=1;mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);next(2);}
                case 2->{if(!(mc.screen instanceof GuideScreen))return;capture="01-guide.png";report.put("guideActualItemUse","PASS");next(3);}
                case 3->{use(P1,2,false);next(4);}
                case 4->{if(!pedestal(P1).item().is(Items.AMETHYST_SHARD))return;use(P2,3,false);next(5);}
                case 5->{if(!pedestal(P2).item().is(Items.AMETHYST_SHARD))return;use(P3,4,false);next(6);}
                case 6->{if(!pedestal(P3).item().is(Items.IRON_INGOT))return;use(T,0,false);next(7);}
                case 7->{if(AltarStateService.get(table()).target().isEmpty())return;use(T,6,true);next(8);}
                case 8->{if(!AltarStateService.get(table()).running())return;capture="02-infusion.png";if(active()){server(p->{check(AltarStateService.get((EnchantingTableBlockEntity)p.serverLevel().getBlockEntity(T)).running(),"Active save not running");p.server.saveEverything(true,true,true);});next(91);}else next(9);}
                case 9->{var state=AltarStateService.get(table());if(state.running())return;var h=mc.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(SHARP).orElseThrow();check(EnchantmentHelper.getEnchantmentsForCrafting(state.target()).getLevel(h)==1,"Infusion output incorrect");if(!(pedestal(P1).item().isEmpty()&&pedestal(P2).item().isEmpty()&&pedestal(P3).item().isEmpty()))return;report.put("physicalInfusionActualPackets","PASS");capture="03-infused.png";use(T,6,false);next(10);}
                case 10->{if(!AltarStateService.get(table()).target().isEmpty())return;server(p->{var shelf=(ChiseledBookShelfBlockEntity)p.serverLevel().getBlockEntity(S);shelf.setItem(1,new ItemStack(Items.BOOK));p.getInventory().setItem(2,new ItemStack(Items.AMETHYST_SHARD));p.getInventory().setItem(3,new ItemStack(Items.FLINT));p.inventoryMenu.broadcastChanges();});next(11);}
                case 11->{if(!ready)return;use(P1,2,false);next(12);}
                case 12->{if(!pedestal(P1).item().is(Items.AMETHYST_SHARD))return;use(P2,3,false);next(13);}
                case 13->{if(!pedestal(P2).item().is(Items.FLINT))return;int slot=-1;var h=mc.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(SHARP).orElseThrow();for(int i=0;i<9;i++)if(EnchantmentHelper.getEnchantmentsForCrafting(mc.player.getInventory().getItem(i)).getLevel(h)==1){slot=i;break;}check(slot>=0,"Retrieved enchanted sword absent from hotbar");use(T,slot,false);next(14);}
                case 14->{if(AltarStateService.get(table()).target().isEmpty())return;use(T,6,true);next(15);}
                case 15->{if(!AltarStateService.get(table()).running())return;check(AltarStateService.get(table()).extraction(),"Extraction direction not set");capture="04-extraction.png";next(16);}
                case 16->{if(AltarStateService.get(table()).running())return;check(EnchantmentHelper.getEnchantmentsForCrafting(AltarStateService.get(table()).target()).isEmpty(),"Extraction failed to remove selected enchantment");server(p->{var shelf=(ChiseledBookShelfBlockEntity)p.serverLevel().getBlockEntity(S);check(shelf.getItem(1).is(Items.ENCHANTED_BOOK),"No extraction book");p.getInventory().setItem(8,shelf.removeItem(1,1));p.getInventory().setItem(4,new ItemStack(Items.IRON_INGOT));p.inventoryMenu.broadcastChanges();});report.put("physicalExtractionActualPackets","PASS");next(17);}
                case 17->{if(!ready||ClientKnowledge.snapshot().enchantments().stream().noneMatch(v->v.id().equals(SHARP)))return;use(P3,4,false);next(18);}
                case 18->{if(!pedestal(P3).item().is(Items.IRON_INGOT))return;if(ModList.get().isLoaded("jei")){GuideJeiProbe.check();GuideJeiProbe.showEnchantment("minecraft:sharpness");report.put("jeiLookupAfterExtraction","PASS");capture="05-jei.png";}server(p->{try{Files.writeString(ConfigRecipeStore.path(),originalConfig);p.server.reloadResources(p.server.getPackRepository().getSelectedIds()).whenComplete((v,t)->p.server.execute(()->{if(t!=null)error=t;p.server.saveEverything(true,true,true);}));}catch(Exception t){throw new RuntimeException(t);}});next(19);}
                case 19->{if(!ready||wait<60)return;report.put("saveReloadIdleTargetPedestalKnowledge","PASS");report.put("smokeExtractionRiskOverride","test world only: 0, restored to shipped 0.2");finish(null);}
                case 91->{if(!ready||wait<25)return;report.put("saveDuringActiveRitual","PASS");finish(null);}
                case 90->{if(!ready)return;if(!active())check(ClientKnowledge.snapshot().enchantments().stream().anyMatch(v->v.id().equals(SHARP)),"Restored knowledge not synchronized");report.put(active()?"activeRestartSafelyCancelledPreservingInputs":"restartPersistentTargetPedestalKnowledgeAndConfig","PASS");finish(null);}
            }
        }catch(Throwable t){finish(t);}
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e){if(capture==null||finished)return;try{Files.createDirectories(OUT);try(var img=net.minecraft.client.Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())){img.writeToFile(OUT.resolve(capture));}capture=null;}catch(Throwable t){finish(t);}}
    private static void finish(Throwable t){finished=true;report.put("status",t==null?"PASS":"FAIL");report.put("phase",phase);if(t!=null){report.put("error",t.toString());com.mojang.logging.LogUtils.getLogger().error("RC smoke failed",t);}try{Files.createDirectories(OUT);Files.writeString(OUT.resolve(Boolean.getBoolean("inscribed.rcSmokePersistence")?"restart.json":"results.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception ignored){}Minecraft.getInstance().stop();}
}
