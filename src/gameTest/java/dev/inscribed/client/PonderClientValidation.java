package dev.inscribed.client;

import java.nio.file.*;
import java.util.*;
import dev.inscribed.pedestal.PedestalBlockEntity;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.lwjgl.glfw.GLFW;

public final class PonderClientValidation {
    private static int phase,ticks,wait;private static boolean opened,finished;private static String capture;
    private static final Path OUT=Path.of("screenshots/inscribed-ponder");
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static void check(boolean b,String text){if(!b)throw new AssertionError(text);}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        if(!Boolean.getBoolean("inscribed.validatePonder")||finished)return;
        var mc=Minecraft.getInstance();GLFW.glfwHideWindow(mc.getWindow().getWindow());mc.options.pauseOnLostFocus=false;
        try{
            if(++ticks>1800)throw new AssertionError("Ponder QA timeout phase "+phase);if(mc.getOverlay()!=null)return;
            if(mc.player==null){if(!opened&&mc.screen!=null){opened=true;mc.createWorldOpenFlows().openWorld("InscribedValidation",()->finish(new AssertionError("World cancelled")));}return;}
            if(capture!=null)return;if(++wait<15)return;
            if(phase==0){
                check(PonderIndex.getSceneAccess().doScenesExistForId(ResourceLocation.parse("minecraft:enchanting_table")),"Table scene not registered");
                check(PonderIndex.getSceneAccess().doScenesExistForId(ResourceLocation.parse("inscribed:infusion_pedestal")),"Pedestal scene not registered");
                var ui=PonderUI.of(new ItemStack(Items.ENCHANTING_TABLE));check(ui!=null,"Ponder table UI absent");mc.setScreen(ui);report.put("tableAndPedestalRegistered","PASS");phase++;wait=0;return;
            }
            var ui=(PonderUI)mc.screen;var scene=ui.getActiveScene();check(scene.getId().equals(ResourceLocation.parse("inscribed:enchanting_setup")),"Wrong scene ID");
            check(scene.getTitle().equals(net.minecraft.network.chat.Component.translatable("inscribed.ponder.enchanting_setup.header").getString()),"Ponder title not localized");
            if(phase==1)for(int caption=1;caption<=8;caption++){
                String key="inscribed.ponder.enchanting_setup.text_"+caption;
                check(net.minecraft.client.resources.language.I18n.exists(key)&&scene.getString("text_"+caption).equals(net.minecraft.client.resources.language.I18n.get(key)),"Missing localized caption "+key);
            }
            if(phase==1&&mc.options.languageCode.equals("en_us")){
                var entries=new int[]{0};PonderIndex.getLangAccess().provideLang("inscribed",(key,fallback)->{
                    if(key.startsWith("inscribed.ponder.enchanting_setup.")){check(net.minecraft.client.resources.language.I18n.get(key).equals(fallback),"Caption numbering disagrees with storyboard: "+key);entries[0]++;}
                });check(entries[0]==9,"Missing storyboard caption keys");report.put("captionsMatchStoryboard","PASS");
            }
            int[] checkpoints={110,220,350,410,490,580,680};
            if(phase<=checkpoints.length){
                ui.seekToTime(checkpoints[phase-1]);
                var world=scene.getWorld();
                if(phase==2){var shelf=(ChiseledBookShelfBlockEntity)world.getBlockEntity(new BlockPos(3,1,1));check(shelf!=null&&shelf.getItem(0).is(Items.ENCHANTED_BOOK),"Physical reference book absent");}
                if(phase==3){for(var pos:List.of(new BlockPos(1,1,3),new BlockPos(5,1,3)))check(((PedestalBlockEntity)world.getBlockEntity(pos)).item().is(Items.AMETHYST_SHARD),"Amethyst missing");check(((PedestalBlockEntity)world.getBlockEntity(new BlockPos(3,1,5))).item().is(Items.IRON_INGOT),"Iron missing");}
                if(phase==5)check(((PedestalBlockEntity)world.getBlockEntity(new BlockPos(1,1,3))).running(),"Pedestal ritual animation absent");
                if(phase==6){
                    check(((PedestalBlockEntity)world.getBlockEntity(new BlockPos(1,1,3))).item().isEmpty(),"Demo costs not consumed");
                    var sharp=world.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(ResourceLocation.parse("minecraft:sharpness")).orElseThrow();var count=new int[]{0};scene.forEachWorldEntity(ItemEntity.class,item->{if(item.getItem().is(Items.IRON_SWORD)){check(EnchantmentHelper.getEnchantmentsForCrafting(item.getItem()).getLevel(sharp)==1,"Wrong demo enchantment");count[0]++;}});check(count[0]==1,"Missing/duplicated demonstration target");report.put("physicalSetupAndInfusionOutput","PASS");
                }
                capture="stage-"+phase+".png";phase++;wait=0;return;
            }
            ui.seekToTime(350);check(((PedestalBlockEntity)scene.getWorld().getBlockEntity(new BlockPos(1,1,3))).item().is(Items.AMETHYST_SHARD),"Replay did not restore costs");report.put("replayRestoresScene","PASS");report.put("language",mc.options.languageCode);report.put("durationTicks",scene.getTotalTime());finish(null);
        }catch(Throwable failure){finish(failure);}
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e){if(capture==null||finished)return;try{Files.createDirectories(OUT);try(var img=net.minecraft.client.Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())){img.writeToFile(OUT.resolve(capture));}capture=null;}catch(Throwable t){finish(t);}}
    private static void finish(Throwable t){finished=true;report.put("status",t==null?"PASS":"FAIL");if(t!=null){report.put("error",t.toString());com.mojang.logging.LogUtils.getLogger().error("Ponder validation failed",t);}try{Files.createDirectories(OUT);Files.writeString(OUT.resolve("results.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception ignored){}Minecraft.getInstance().stop();}
}
