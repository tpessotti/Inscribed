package dev.inscribed.knowledge;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
/** Inventory deltas observe actual possession, never an open menu or a bookshelf. */
public final class KnowledgeEvents {
    private static final Map<UUID,Map<ResourceLocation,Integer>> INVENTORIES=new HashMap<>();
    private static final Map<UUID,Long> GENERATIONS=new HashMap<>();
    public static void pickup(ItemEntityPickupEvent.Post e){if(e.getPlayer() instanceof ServerPlayer p){if(dev.inscribed.config.InscribedConfig.KNOWLEDGE_PICKUP.get())KnowledgeService.learnBook(p,e.getOriginalStack());}}
    public static void trade(TradeWithVillagerEvent e){if(e.getEntity() instanceof ServerPlayer p){if(dev.inscribed.config.InscribedConfig.KNOWLEDGE_TRADE.get())KnowledgeService.learnBook(p,e.getMerchantOffer().getResult());}}
    public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){seed(p);KnowledgeService.sync(p,false);}}
    public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p){seed(p);KnowledgeService.sync(p,false);}}
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)KnowledgeService.sync(p,false);}
    public static void logout(PlayerEvent.PlayerLoggedOutEvent e){var id=e.getEntity().getUUID();INVENTORIES.remove(id);GENERATIONS.remove(id);KnowledgeService.forgetSession(id);}
    public static void stop(ServerStoppedEvent e){INVENTORIES.keySet().forEach(KnowledgeService::forgetSession);INVENTORIES.clear();GENERATIONS.clear();KnowledgeService.clearSessions();}
    public static void tick(PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p)||p.tickCount%dev.inscribed.config.InscribedConfig.KNOWLEDGE_INTERVAL.get()!=0)return;
        var now=counts(p);var previous=INVENTORIES.put(p.getUUID(),now);
        if(previous!=null&&dev.inscribed.config.InscribedConfig.KNOWLEDGE_INVENTORY.get())for(var entry:now.entrySet())if(entry.getValue()>previous.getOrDefault(entry.getKey(),0))KnowledgeService.change(p,entry.getKey(),true,true);
        long gen=dev.inscribed.recipe.InscribedRecipeRegistry.generation()*31+(dev.inscribed.config.InscribedConfig.LSBC_KNOWLEDGE.get()?1:0)+(dev.inscribed.config.InscribedConfig.JEI_RECIPES.get()?2:0);var old=GENERATIONS.put(p.getUUID(),gen);if(old==null||old!=gen)KnowledgeService.sync(p,false);
    }
    private static void seed(ServerPlayer p){INVENTORIES.put(p.getUUID(),counts(p));GENERATIONS.put(p.getUUID(),dev.inscribed.recipe.InscribedRecipeRegistry.generation());}
    private static Map<ResourceLocation,Integer> counts(ServerPlayer p){var result=new HashMap<ResourceLocation,Integer>();for(int slot=0;slot<p.getInventory().getContainerSize();slot++){var stack=p.getInventory().getItem(slot);if(stack.is(Items.ENCHANTED_BOOK))for(var h:EnchantmentHelper.getEnchantmentsForCrafting(stack).keySet())h.unwrapKey().ifPresent(k->result.merge(k.location(),stack.getCount(),Integer::sum));}return result;}
    private KnowledgeEvents(){}
}
