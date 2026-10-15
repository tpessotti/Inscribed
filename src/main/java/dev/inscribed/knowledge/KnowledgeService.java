package dev.inscribed.knowledge;
import java.util.*;
import dev.inscribed.recipe.*;
import dev.inscribed.registry.InscribedRegistries;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.network.PacketDistributor;
import com.mojang.logging.LogUtils;

public final class KnowledgeService {
    private static final Map<UUID,Long> REQUESTS=new HashMap<>();
    private static long sequence;
    private static long previewGeneration=-1;
    private static final Map<ResourceLocation,List<ItemStack>> PREVIEWS=new HashMap<>();
    public static KnowledgeData get(ServerPlayer p){return p.getData(InscribedRegistries.KNOWLEDGE);}
    public static void change(ServerPlayer p,ResourceLocation id,boolean enchantment,boolean learn){
        if(!p.server.isSameThread())throw new IllegalStateException("Knowledge must change on the server thread");
        var previous=get(p);var next=previous.change(id,enchantment,learn);
        if(!next.equals(previous))p.setData(InscribedRegistries.KNOWLEDGE,next);
        if(enchantment&&learn)p.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(id).ifPresent(h->{
            try{KnowledgeIntegrations.get().learn(p,EnchantmentNames.component(h));}catch(RuntimeException e){LogUtils.getLogger().error("Optional word learning failed; Inscribed discovery retained",e);}
        });
        if(!next.equals(previous))sync(p,false);
    }
    public static void clear(ServerPlayer p){p.setData(InscribedRegistries.KNOWLEDGE,KnowledgeData.empty());sync(p,false);}
    public static void learnBook(ServerPlayer p,ItemStack stack){
        if(!stack.is(Items.ENCHANTED_BOOK))return;
        for(var h:EnchantmentHelper.getEnchantmentsForCrafting(stack).keySet())h.unwrapKey().ifPresent(k->change(p,k.location(),true,true));
    }
    public static void request(ServerPlayer p){request(p,false);}
    public static void request(ServerPlayer p,boolean open){long tick=p.serverLevel().getGameTime();var last=REQUESTS.get(p.getUUID());if(last==null||tick<last||tick-last>=10){REQUESTS.put(p.getUUID(),tick);sync(p,open);}}
    public static void forgetSession(UUID id){REQUESTS.remove(id);}
    public static GuideSnapshot snapshot(ServerPlayer p){
        var data=get(p);var enchants=p.registryAccess().registryOrThrow(Registries.ENCHANTMENT);var integration=KnowledgeIntegrations.get();
        var known=new ArrayList<GuideSnapshot.KnownEnchantment>();var recipes=new ArrayList<GuideSnapshot.Recipe>();
        var targets=new HashMap<ResourceLocation,List<ItemStack>>();
        for(var id:new TreeSet<>(data.knownEnchantments()))enchants.getHolder(id).ifPresent(h->{var n=integration.render(p,EnchantmentNames.component(h));known.add(new GuideSnapshot.KnownEnchantment(id,n.visible(),n.searchable(),h.value().getMaxLevel()));});
        for(var recipe:InscribedRecipeRegistry.recipes()){
            if(!RecipeVisibilityService.visible(recipe,data))continue;
            var h=enchants.getHolder(recipe.enchantment()).orElse(null);if(h==null)continue;
            var n=integration.render(p,EnchantmentNames.component(h));
            var name=n.visible().copy().append(" ").append(Component.translatable("enchantment.level."+recipe.level()));
            var search=n.searchable().copy().append(" ").append(Component.translatable("enchantment.level."+recipe.level()));
            var ts=targets.computeIfAbsent(recipe.enchantment(),id->previewTargets(id,h));
            recipe.infusion().ifPresent(i->recipes.add(new GuideSnapshot.Recipe(recipe.id(),recipe.enchantment(),recipe.level(),false,name,search,recipe.minimumPower(),i.durationTicks(),i.bookConsumptionChance(),0,recipe.exactBookLevel(),costs(i.ingredients()),ts)));
            recipe.extraction().ifPresent(e->recipes.add(new GuideSnapshot.Recipe(recipe.id(),recipe.enchantment(),recipe.level(),true,name,search,recipe.minimumPower(),e.durationTicks(),e.failureChance(),e.emptyBooksRequired(),true,costs(e.ingredients()),ts)));
        }
        return new GuideSnapshot(InscribedRecipeRegistry.generation(),known,recipes);
    }
    private static List<ItemStack> previewTargets(ResourceLocation id,net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment){
        if(previewGeneration!=InscribedRecipeRegistry.generation()){PREVIEWS.clear();previewGeneration=InscribedRecipeRegistry.generation();}
        var cached=PREVIEWS.computeIfAbsent(id,key->{var result=new ArrayList<ItemStack>();for(var item:BuiltInRegistries.ITEM){var stack=new ItemStack(item);if(!stack.isEmpty()&&!stack.is(Items.ENCHANTED_BOOK)&&!stack.is(Items.BOOK)&&stack.supportsEnchantment(enchantment)){result.add(stack);if(result.size()==12)break;}}return List.copyOf(result);});
        return cached.stream().map(ItemStack::copy).toList();
    }
    public static void clearSessions(){REQUESTS.clear();PREVIEWS.clear();previewGeneration=-1;}
    private static List<GuideSnapshot.Cost> costs(List<EnchantmentRecipe.Cost> cs){return cs.stream().map(c->new GuideSnapshot.Cost(Arrays.stream(c.ingredient().getItems()).limit(64).map(s->{var copy=s.copy();copy.setCount(1);return copy;}).toList(),c.count())).toList();}
    public static Component enchantmentName(ServerPlayer p,net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> h,int level){
        return KnowledgeIntegrations.get().render(p,EnchantmentNames.component(h)).visible().copy().append(" ").append(Component.translatable("enchantment.level."+level));
    }
    public static List<GuidePayload> packets(ServerPlayer p,boolean open,GuideSnapshot snapshot){
        long batch=++sequence;var result=new ArrayList<GuidePayload>();var es=new ArrayList<GuideSnapshot.KnownEnchantment>();var rs=new ArrayList<GuideSnapshot.Recipe>();int bytes=64;
        var rows=new ArrayList<Object>();rows.addAll(snapshot.enchantments());rows.addAll(snapshot.recipes());
        for(var row:rows){
            var single=new GuideSnapshot(snapshot.generation(),row instanceof GuideSnapshot.KnownEnchantment e?List.of(e):List.of(),row instanceof GuideSnapshot.Recipe r?List.of(r):List.of());
            var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),p.registryAccess());int size;
            try{GuidePayload.STREAM_CODEC.encode(buffer,new GuidePayload(false,single));size=buffer.readableBytes();}finally{buffer.release();}
            if(size>750000){LogUtils.getLogger().error("Guide row exceeds network budget; omitted: {}",row instanceof GuideSnapshot.Recipe r?r.id():((GuideSnapshot.KnownEnchantment)row).id());continue;}
            if(bytes+size>750000||es.size()>=4096||rs.size()>=8192){result.add(new GuidePayload(false,new GuideSnapshot(snapshot.generation(),es,rs),batch,false));es=new ArrayList<>();rs=new ArrayList<>();bytes=64;}
            if(row instanceof GuideSnapshot.KnownEnchantment e)es.add(e);else rs.add((GuideSnapshot.Recipe)row);bytes+=size;
        }
        result.add(new GuidePayload(open,new GuideSnapshot(snapshot.generation(),es,rs),batch,true));return List.copyOf(result);
    }
    public static void sync(ServerPlayer p,boolean open){if(p.connection!=null)for(var packet:packets(p,open,snapshot(p)))PacketDistributor.sendToPlayer(p,packet);}
    private KnowledgeService(){}
}
