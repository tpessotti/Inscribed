package dev.inscribed.client;

import dev.inscribed.altar.*;
import dev.inscribed.recipe.*;
import dev.inscribed.registry.InscribedRegistries;
import dev.inscribed.ritual.*;
import dev.inscribed.pedestal.PedestalBlockEntity;
import java.util.*;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Native client/server fixture using real catalog, packets, renderers and particle effects. */
@EventBusSubscriber(modid="inscribed",value=Dist.CLIENT)
public final class PolishClientValidation {
    private static int renderSample;
    private static final BlockPos T=new BlockPos(0,80,0),S=T.offset(0,0,-3);
    private static final Path OUTPUT=Path.of("screenshots/inscribed-polish04");
    private static final Map<String,Object> REPORT=new LinkedHashMap<>();
    private static int ticks,phase,wait;private static boolean opened,finished;
    private static volatile boolean ready;private static volatile Throwable error;private static String capture;
    private static ResourceLocation id(String s){return ResourceLocation.parse(s);}
    private static EnchantingTableBlockEntity table(){return (EnchantingTableBlockEntity)Minecraft.getInstance().level.getBlockEntity(T);}
    private static void check(boolean ok,String text){if(!ok)throw new AssertionError(text);}
    private static void server(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> action){
        var mc=Minecraft.getInstance();ready=false;mc.getSingleplayerServer().execute(()->{try{action.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));ready=true;}catch(Throwable t){error=t;}});
    }
    private static void prepare(net.minecraft.server.level.ServerPlayer player,boolean extraction){
        var level=player.serverLevel();
        var recipe=InscribedRecipeRegistry.recipes().stream().filter(r->r.id().equals(id("inscribed:minecraft/fortune_3"))).findFirst().orElseThrow();
        var costs=extraction ? recipe.extraction().orElseThrow().ingredients() : recipe.infusion().orElseThrow().ingredients();
        for(int i=0;i<8;i++){
            var pos=T.offset(dev.inscribed.UnitPedestals.OFFSETS.get(i));level.setBlock(pos,InscribedRegistries.PEDESTAL.get().defaultBlockState(),3);
            ((PedestalBlockEntity)level.getBlockEntity(pos)).setItem(i<costs.size() ? costs.get(i).ingredient().getItems()[0].copyWithCount(1) : ItemStack.EMPTY);
        }
        var shelf=(ChiseledBookShelfBlockEntity)level.getBlockEntity(S);for(int i=0;i<6;i++)shelf.setItem(i,ItemStack.EMPTY);
        var book=new ItemStack(extraction ? Items.BOOK : Items.ENCHANTED_BOOK);
        if(!extraction)book.enchant(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(id("minecraft:fortune")).orElseThrow(),3);
        shelf.setItem(0,book);player.getInventory().clearContent();player.inventoryMenu.broadcastChanges();
    }
    private static void click(){var mc=Minecraft.getInstance();mc.player.setShiftKeyDown(true);mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(T),Direction.UP,T,false));mc.player.setShiftKeyDown(false);mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));}
    private static void next(int n){phase=n;wait=0;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        if(!Boolean.getBoolean("inscribed.validatePolish") || finished)return;
        var mc=Minecraft.getInstance();org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().getWindow());mc.options.pauseOnLostFocus=false;mc.options.enableVsync().set(false);
        try{
            if(++ticks>4000)throw new AssertionError("Native polish timeout phase "+phase);
            if(error!=null)throw new AssertionError("Server fixture failed",error);
            if(mc.getOverlay()!=null)return;
            if(mc.player==null || mc.level==null){if(!opened && mc.screen!=null){opened=true;mc.createWorldOpenFlows().openWorld("InscribedValidation",()->finish(new AssertionError("World cancelled")));}return;}
            wait++;
            switch(phase){
                case 0->{mc.options.renderDistance().set(3);mc.options.guiScale().set(3);mc.options.hideGui=true;mc.resizeDisplay();server(p->{
                    var l=p.serverLevel();for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++){l.setBlock(new BlockPos(x,79,z),Blocks.SMOOTH_STONE.defaultBlockState(),3);for(int y=80;y<=83;y++)l.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);}
                    l.setBlock(T,Blocks.ENCHANTING_TABLE.defaultBlockState(),3);l.setBlock(S,Blocks.CHISELED_BOOKSHELF.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH),3);
                    for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)if(x*x+z*z<=25 && Math.abs(x)+Math.abs(z)>=4)l.setBlock(T.offset(x,0,z),Blocks.BOOKSHELF.defaultBlockState(),3);
                    AltarStateService.target((EnchantingTableBlockEntity)l.getBlockEntity(T),new ItemStack(Items.DIAMOND_PICKAXE));prepare(p,false);
                    p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.teleportTo(3.5,80,3.5);p.setYRot(135);p.setXRot(17);l.setDayTime(13000);
                    dev.inscribed.config.InscribedConfig.DYNAMIC_LIGHT.set(true);REPORT.put("light","item emissive curve; no world relight");
                });next(1);}
                case 1->{if(!ready || wait<40 || table()==null || table().getLevel()==null)return;mc.player.setYRot(135);mc.player.setXRot(17);capture="01-idle-catalyst.png";next(2);}
                case 2->{if(capture!=null)return;click();next(3);}
                case 3->{if(!AltarStateService.get(table()).running())return;check(!AltarStateService.get(table()).extraction(),"Infusion visual direction reversed");capture="02-infusion-awakening.png";REPORT.put("infusionNetworkTiming","PASS");next(4);}
                case 4->{var s=AltarStateService.get(table());double p=(mc.level.getGameTime()-s.visualStart())/(double)Math.max(1,s.visualDuration());if(p>.4 && capture==null){check(RitualAmbientSound.activeCount()==1,"Infusion ambient loop missing or duplicated");REPORT.put("singleInfusionAmbientLoop","PASS");capture="03-infusion-transfer.png";next(5);}}
                case 5->{var s=AltarStateService.get(table());double p=(mc.level.getGameTime()-s.visualStart())/(double)Math.max(1,s.visualDuration());if(p>.84 && capture==null){capture="04-infusion-crescendo.png";next(6);}}
                case 6->{if(AltarStateService.get(table()).running() || capture!=null)return;var reg=mc.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);check(EnchantmentHelper.getEnchantmentsForCrafting(AltarStateService.get(table()).target()).getLevel(reg.getHolder(id("minecraft:fortune")).orElseThrow())==3,"Infusion result not synced");capture="05-infusion-completion.png";REPORT.put("physicalInfusionAndCompletion","PASS");next(7);}
                case 7->{if(wait<35 || capture!=null)return;check(AltarStateService.get(table()).visualStrength(mc.level.getGameTime())==0,"Infusion fade never cleared");server(p->prepare(p,true));next(8);}
                case 8->{if(!ready || wait<30)return;click();next(9);}
                case 9->{if(!AltarStateService.get(table()).running())return;check(RitualAmbientSound.activeCount()==0,"Infusion audio survived extraction");check(AltarStateService.get(table()).extraction(),"Extraction visual direction missing");capture="06-extraction-awakening.png";next(10);}
                case 10->{var s=AltarStateService.get(table());if((mc.level.getGameTime()-s.visualStart())/(double)Math.max(1,s.visualDuration())>.45 && capture==null){capture="07-extraction-transfer.png";REPORT.put("distinctReverseExtractionFlow","PASS");next(11);}}
                case 11->{if(AltarStateService.get(table()).running() || capture!=null)return;
                    // Vanilla chiseled shelves synchronize occupancy, not book components.
                    server(p->{var shelf=(ChiseledBookShelfBlockEntity)p.serverLevel().getBlockEntity(S);check(shelf.getItem(0).is(Items.ENCHANTED_BOOK),"Server extraction produced no book");REPORT.put("physicalExtractionBookAndCompletion","PASS");});next(111);}
                case 111->{if(!ready)return;capture="08-extraction-completion-book.png";next(12);}
                case 12->{if(wait<35 || capture!=null)return;check(AltarStateService.get(table()).visualStrength(mc.level.getGameTime())==0,"Extraction fade never cleared");server(p->prepare(p,false));next(13);}
                case 13->{if(!ready || wait<30)return;click();next(14);}
                case 14->{if(!AltarStateService.get(table()).running() || wait<25)return;server(p->{var pos=T.offset(dev.inscribed.UnitPedestals.OFFSETS.getFirst());((PedestalBlockEntity)p.serverLevel().getBlockEntity(pos)).setItem(ItemStack.EMPTY);});next(15);}
                case 15->{if(!ready || wait<30 || AltarStateService.get(table()).running())return;check(AltarStateService.get(table()).visualStrength(mc.level.getGameTime())==0,"Cancelled client animation survived");check(RitualAmbientSound.activeCount()==0,"Cancelled ambient loop survived");REPORT.put("ambientCancellationCleanup","PASS");capture="09-cancel-cleanup.png";REPORT.put("cancellationEffectCleanup","PASS");next(16);}
                case 16->{if(capture!=null || wait<30)return;REPORT.put("catalystItemModel",mc.getItemRenderer().getModel(new ItemStack(InscribedRegistries.ENCHANTMENT_CATALYST.get()),mc.level,mc.player,0)==mc.getModelManager().getMissingModel() ? "FAIL" : "PASS");check(REPORT.get("catalystItemModel").equals("PASS"),"Missing catalyst model");next(17);}
                case 17->{if(capture!=null)return;String[] models={"minecraft:diamond_sword","minecraft:enchanted_book","minecraft:stone","minecraft:amethyst_shard","minecraft:elytra","create:copper_backtank"};int index=renderSample;if(index==models.length){REPORT.put("multipleItemRendererModels","PASS");finish(null);return;}var modelId=id(models[index]);if(!net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(modelId)){renderSample++;return;}var stack=new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(modelId));check(mc.getItemRenderer().getModel(stack,mc.level,mc.player,0)!=mc.getModelManager().getMissingModel(),"Missing renderer model "+modelId);server(p->{AltarStateService.target((EnchantingTableBlockEntity)p.serverLevel().getBlockEntity(T),stack.copy());((PedestalBlockEntity)p.serverLevel().getBlockEntity(T.offset(dev.inscribed.UnitPedestals.OFFSETS.getFirst()))).setItem(stack.copy());});next(18);}
                case 18->{if(!ready||wait<20)return;capture="renderer-"+renderSample+".png";renderSample++;next(17);}
            }
        }catch(Throwable t){finish(t);}
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event){if(capture==null || finished)return;try{Files.createDirectories(OUTPUT);try(var image=net.minecraft.client.Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())){image.writeToFile(OUTPUT.resolve(capture));}capture=null;}catch(Throwable t){finish(t);}}
    private static void finish(Throwable t){if(finished)return;finished=true;REPORT.put("status",t==null ? "PASS" : "FAIL");REPORT.put("phase",phase);REPORT.put("ticks",ticks);if(t!=null){REPORT.put("error",t.toString());com.mojang.logging.LogUtils.getLogger().error("Native polish validation failed",t);}try{Files.createDirectories(OUTPUT);Files.writeString(OUTPUT.resolve("results.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(REPORT));}catch(Exception ignored){}Minecraft.getInstance().stop();}
}
