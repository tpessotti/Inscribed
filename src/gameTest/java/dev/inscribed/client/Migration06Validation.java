package dev.inscribed.client;

import java.nio.file.*;
import java.util.Map;
import dev.inscribed.knowledge.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid="inscribed",value=Dist.CLIENT)
public final class Migration06Validation {
    private static int ticks;private static boolean opened,submitted,finished;private static volatile boolean ready;private static volatile Throwable failure;
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        if(!Boolean.getBoolean("inscribed.validateMigration")||finished)return;var mc=Minecraft.getInstance();GLFW.glfwHideWindow(mc.getWindow().getWindow());mc.options.pauseOnLostFocus=false;
        try{
            if(++ticks>1800)throw new AssertionError("Migration timeout");if(mc.getOverlay()!=null)return;
            if(mc.player==null){if(!opened&&mc.screen!=null){opened=true;mc.createWorldOpenFlows().openWorld("InscribedValidation",()->finish(new AssertionError("World cancelled")));}return;}
            if(!submitted){submitted=true;mc.getSingleplayerServer().execute(()->{try{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());var id=ResourceLocation.parse("create:capacity");if(!KnowledgeService.get(p).knownEnchantments().contains(id))throw new AssertionError("Saved Create knowledge was lost");
                boolean visible=KnowledgeService.snapshot(p).enchantments().stream().anyMatch(v->v.id().equals(id));if(visible!=ModList.get().isLoaded("create"))throw new AssertionError("Missing provider filtering incorrect");p.getServer().saveEverything(true,true,true);ready=true;
            }catch(Throwable t){failure=t;ready=true;}});return;}
            if(ready)finish(failure);
        }catch(Throwable t){finish(t);}
    }
    private static void finish(Throwable t){finished=true;try{Files.writeString(Path.of("migration-"+ModList.get().isLoaded("create")+".json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status",t==null?"PASS":"FAIL","providerPresent",ModList.get().isLoaded("create"),"error",t==null?"":t.toString(),"savedKnowledgeRetained",t==null)));}catch(Exception ignored){}if(t!=null)com.mojang.logging.LogUtils.getLogger().error("Migration QA failed",t);Minecraft.getInstance().stop();}
}
