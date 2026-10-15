package dev.inscribed.client;

import java.nio.file.*;
import java.util.UUID;
import dev.inscribed.altar.AltarStateService;
import dev.inscribed.ritual.SelectionChoice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid="inscribed",value=Dist.CLIENT)
public final class Multiplayer06Client {
    private static final BlockPos T=new BlockPos(0,80,0);
    private static int phase,ticks,wait;
    private static boolean done;
    private static final Path SIGNAL=Path.of("../server");
    private static void next(int n){phase=n;wait=0;}
    private static void use(boolean shift){
        var mc=Minecraft.getInstance();mc.setScreen(null);mc.player.setShiftKeyDown(shift);
        mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player,shift?ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY:ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
        mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(.5,80.75,.5),Direction.UP,T,false));
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        String role=System.getProperty("inscribed.qaMultiplayerRole");if(role==null||done)return;
        var mc=Minecraft.getInstance();GLFW.glfwHideWindow(mc.getWindow().getWindow());mc.options.pauseOnLostFocus=false;
        try{
            if(++ticks>4800)throw new AssertionError("Client timeout role "+role+" phase "+phase);
            ++wait;if(mc.getOverlay()!=null)return;
            if(Files.exists(SIGNAL.resolve("results.json"))){done=true;Files.writeString(Path.of("results.json"),Files.readString(SIGNAL.resolve("results.json")));mc.stop();return;}
            if(role.equals("A")&&phase==3){if(wait<45||!Files.exists(SIGNAL.resolve("cancelled.txt")))return;ConnectScreen.startConnecting(new TitleScreen(),mc,ServerAddress.parseString("127.0.0.1:25773"),new ServerData("QA","127.0.0.1:25773",ServerData.Type.OTHER),false,null);next(4);return;}
            if(mc.player==null||mc.level==null||mc.gameMode==null)return;
            if(!(mc.level.getBlockEntity(T) instanceof EnchantingTableBlockEntity table)||wait<2)return;
            var state=AltarStateService.get(table);
            if(role.equals("A"))switch(phase){
                case 0->{if(!Files.exists(SIGNAL.resolve("observer-ready.txt")))return;use(false);next(1);}
                case 1->{if(state.target().isEmpty())return;use(true);next(2);}
                case 2->{if(!state.running()||!Files.exists(SIGNAL.resolve("observer-attempt.txt")))return;mc.disconnect(new TitleScreen());next(3);}
                case 4->{if(wait<10)return;use(true);next(5);}
                case 5->{if(state.running()||state.target().isEmpty())return;var holder=mc.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(ResourceLocation.parse("minecraft:sharpness")).orElseThrow();if(EnchantmentHelper.getEnchantmentsForCrafting(state.target()).getLevel(holder)!=1)return;use(false);next(6);}
            }
            else switch(phase){
                case 0->{Files.writeString(SIGNAL.resolve("observer-ready.txt"),"ready");next(1);}
                case 1->{if(!state.running())return;use(true);PacketDistributor.sendToServer(new SelectionChoice(UUID.randomUUID(),999));Files.writeString(SIGNAL.resolve("observer-attempt.txt"),"sent actual concurrent start and invalid nonce");next(2);}
            }
        }catch(Throwable failure){done=true;try{Files.writeString(Path.of("results.json"),"{\"status\":\"FAIL\",\"error\":"+new com.google.gson.Gson().toJson(failure.toString())+"}");}catch(Exception ignored){}com.mojang.logging.LogUtils.getLogger().error("Multiplayer client failed",failure);mc.stop();}
    }
}
