package dev.inscribed.client;
import dev.inscribed.knowledge.*;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import java.util.*;
@EventBusSubscriber(modid="inscribed",value=Dist.CLIENT)
public final class ClientKnowledge {
    private static GuideSnapshot snapshot=GuideSnapshot.empty();
    private static final List<Runnable> LISTENERS=new ArrayList<>();
    private static long sequence=-1;
    private static final List<GuideSnapshot.KnownEnchantment> ENCHANTMENTS=new ArrayList<>();
    private static final List<GuideSnapshot.Recipe> RECIPES=new ArrayList<>();
    public static GuideSnapshot snapshot(){return snapshot;}
    public static void listen(Runnable r){LISTENERS.add(r);}
    public static void receive(GuidePayload p){
        if(sequence!=p.sequence()){ENCHANTMENTS.clear();RECIPES.clear();sequence=p.sequence();}
        ENCHANTMENTS.addAll(p.snapshot().enchantments());RECIPES.addAll(p.snapshot().recipes());
        if(!p.complete())return;
        snapshot=new GuideSnapshot(p.snapshot().generation(),ENCHANTMENTS,RECIPES);ENCHANTMENTS.clear();RECIPES.clear();LISTENERS.forEach(Runnable::run);var mc=Minecraft.getInstance();
        if(p.open())mc.setScreen(new GuideScreen(mc.screen));else if(mc.screen instanceof GuideScreen screen)screen.refresh();
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){snapshot=GuideSnapshot.empty();sequence=-1;ENCHANTMENTS.clear();RECIPES.clear();LISTENERS.forEach(Runnable::run);}
    private ClientKnowledge(){}
}
