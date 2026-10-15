package dev.inscribed.client;

import java.nio.file.*;
import java.util.*;
import dev.inscribed.ritual.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid="inscribed",value=Dist.CLIENT)
public final class Aspect06Validation {
    private static int sample,ticks,wait;
    private static boolean opened,finished;
    private static String capture;
    private static final Path OUT=Path.of("screenshots/aspect06");
    private static final List<String> checks=new ArrayList<>();
    private static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        if(!Boolean.getBoolean("inscribed.validateAspect")||finished)return;var mc=Minecraft.getInstance();GLFW.glfwHideWindow(mc.getWindow().getWindow());mc.options.pauseOnLostFocus=false;
        try{
            if(++ticks>3000)throw new AssertionError("Aspect QA timeout");if(mc.getOverlay()!=null)return;
            if(mc.player==null){if(!opened&&mc.screen!=null){opened=true;mc.createWorldOpenFlows().openWorld("InscribedValidation",()->finish(new AssertionError("World cancelled")));}return;}
            if(capture!=null)return;
            if(sample==24){finish(null);return;}
            if(wait++==0){int[] size=new int[][]{{1280,720},{3440,1440},{640,480}}[sample/8];GLFW.glfwSetWindowSize(mc.getWindow().getWindow(),size[0],size[1]);mc.options.guiScale().set(new int[]{2,3,4,0}[(sample/2)%4]);mc.resizeDisplay();return;}
            if(wait<10)return;
            if(sample%2==0)mc.setScreen(new GuideScreen(null));else{
                var options=new ArrayList<SelectionPayload.Option>();for(int i=0;i<12;i++)options.add(new SelectionPayload.Option(Component.literal("Very long authored enchantment name with accents: Proteção Épica "+i),RitualType.INFUSION,72000,1000000,1,Component.literal("Eight reagents and physical reference book")));
                mc.setScreen(new RitualSelectionScreen(new SelectionPayload(UUID.randomUUID(),options)));
            }
            for(var child:mc.screen.children())if(child instanceof AbstractWidget w)check(w.getX()>=0&&w.getY()>=0&&w.getX()+w.getWidth()<=mc.screen.width&&w.getY()+w.getHeight()<=mc.screen.height,"Widget outside viewport sample "+sample);
            if(mc.screen instanceof GuideScreen guide){var g=guide.bookGeometry();for(var child:guide.children())if(child instanceof AbstractWidget w&&w.getX()<g.x()+g.width())check(((w.getX()>=g.leftX()&&w.getX()+w.getWidth()<=g.leftX()+g.leafWidth())||(w.getX()>=g.rightX()&&w.getX()+w.getWidth()<=g.rightX()+g.leafWidth()))&&w.getY()>=g.y()+g.inset()&&w.getY()+w.getHeight()<=g.y()+g.height()-g.inset(),"Guide widget outside leaf sample "+sample);}
            capture="sample-"+sample+".png";checks.add("PASS sample "+sample+" viewport "+mc.screen.width+"x"+mc.screen.height);sample++;wait=0;
        }catch(Throwable t){finish(t);}
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e){if(capture==null||finished)return;try{Files.createDirectories(OUT);try(var img=net.minecraft.client.Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())){img.writeToFile(OUT.resolve(capture));}capture=null;}catch(Throwable t){finish(t);}}
    private static void finish(Throwable t){finished=true;try{Files.createDirectories(OUT);Files.writeString(OUT.resolve("results.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status",t==null?"PASS":"FAIL","error",t==null?"":t.toString(),"checks",checks)));}catch(Exception ignored){}if(t!=null)com.mojang.logging.LogUtils.getLogger().error("Aspect QA failed",t);Minecraft.getInstance().stop();}
}
