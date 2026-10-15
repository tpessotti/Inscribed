package dev.inscribed.loot;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import dev.inscribed.config.InscribedConfig;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/** Reloadable structure pools, with config overrides and bounded additive rolls. */
public final class LootInjection extends SimpleJsonResourceReloadListener {
    public record Entry(ResourceLocation item, ResourceLocation enchantment, int weight, int minLevel, int maxLevel, int minCount, int maxCount) {}
    public record Pool(ResourceLocation id, Set<ResourceLocation> tables, double chance, int minRolls, int maxRolls, List<Entry> entries,
                       String theme,int structureTier,boolean treasure,boolean requireRecipe,int totalWeight) {}
    private static List<Pool> pools = List.of();
    private static Map<ResourceLocation,List<Pool>> byTable=Map.of();
    private static JsonObject lastConfig;
    private final RegistryAccess registries;
    private LootInjection(RegistryAccess registries) { super(new Gson(), "inscribed/loot_injections"); this.registries = registries; }
    public static Path path() { return FMLPaths.CONFIGDIR.get().resolve("inscribed/loot.json"); }
    public static List<Pool> pools() { return pools; }
    public static void registerReload(AddReloadListenerEvent event) { event.addListener(new LootInjection(event.getRegistryAccess())); }
    private static JsonObject config() {
        try {
            if (!Files.exists(path())) {
                Files.createDirectories(path().getParent());
                try (var in = LootInjection.class.getResourceAsStream("/inscribed/default-loot.json")) {
                    if (in == null) throw new IllegalStateException("Missing default loot configuration");
                    Files.copy(in, path());
                }
            }
            if (Files.size(path()) > 4_000_000) throw new IllegalArgumentException("loot.json exceeds 4 MB");
            var root = JsonParser.parseString(Files.readString(path(), StandardCharsets.UTF_8)).getAsJsonObject();
            if (integer(root,"format_version",1,1) != 1 || !root.has("pools") || !root.get("pools").isJsonObject()) throw new IllegalArgumentException("Expected format_version=1 and pools object");
            if (root.getAsJsonObject("pools").size() > 256) throw new IllegalArgumentException("At most 256 pools");
            if (root.has("include_datapack_pools")) bool(root,"include_datapack_pools",true);
            lastConfig = root;
        } catch (Exception e) { LogUtils.getLogger().error("Inscribed loot config {} rejected; keeping last valid config: {}", path(), e.getMessage()); }
        if(lastConfig!=null)return lastConfig.deepCopy();
        var disabled=new JsonObject();disabled.addProperty("include_datapack_pools",false);return disabled;
    }
    @Override protected void apply(Map<ResourceLocation,JsonElement> data, ResourceManager resources, ProfilerFiller profiler) {
        var cfg = config();
        var merged = new TreeMap<ResourceLocation,JsonElement>();
        if (bool(cfg,"include_datapack_pools",true)) merged.putAll(data);
        if (cfg.has("pools")) for (var e : cfg.getAsJsonObject("pools").entrySet()) {
            try { merged.put(ResourceLocation.parse(e.getKey()), e.getValue()); }
            catch (RuntimeException ex) { LogUtils.getLogger().error("Invalid Inscribed loot pool ID {}; entry omitted",e.getKey()); }
        }
        var next = new ArrayList<Pool>();
        for (var e : merged.entrySet()) try {
            var json = e.getValue().getAsJsonObject();
            if (bool(json,"enabled",true)) {
                var pool=parse(e.getKey(),json,registries);
                for(var table:pool.tables())if(resources.getResource(ResourceLocation.fromNamespaceAndPath(table.getNamespace(),"loot_table/"+table.getPath()+".json")).isEmpty())
                    LogUtils.getLogger().warn("Inscribed loot pool {} targets table {} without a JSON resource; verify the table exists",pool.id(),table);
                next.add(pool);
            }
        } catch (RuntimeException ex) { LogUtils.getLogger().error("Invalid Inscribed loot pool {}: {}; pool omitted",e.getKey(),ex.getMessage()); }
        pools = List.copyOf(next);
        var index=new TreeMap<ResourceLocation,List<Pool>>();
        for(var pool:pools)for(var table:pool.tables())index.computeIfAbsent(table,k->new ArrayList<>()).add(pool);
        var frozen=new TreeMap<ResourceLocation,List<Pool>>();index.forEach((k,v)->frozen.put(k,List.copyOf(v)));byTable=Map.copyOf(frozen);
        LogUtils.getLogger().info("Loaded {} Inscribed exploration loot pools",pools.size());
    }
    public static Pool parse(ResourceLocation id, JsonObject json, RegistryAccess registries) {
        var tables = new LinkedHashSet<ResourceLocation>();
        var list = json.has("loot_tables")&&json.get("loot_tables").isJsonArray()?json.getAsJsonArray("loot_tables"):null;
        if (list == null || list.isEmpty() || list.size() > 64) throw new IllegalArgumentException("loot_tables must contain 1..64 exact IDs");
        for (var value : list) tables.add(ResourceLocation.parse(value.getAsString()));
        if(!json.has("chance")||!json.get("chance").isJsonPrimitive()||!json.get("chance").getAsJsonPrimitive().isNumber())throw new IllegalArgumentException("chance must be a number between 0.0 and 1.0");
        double chance = json.get("chance").getAsDouble();
        if (!json.get("chance").getAsJsonPrimitive().isNumber() || !Double.isFinite(chance) || chance < 0 || chance > 1) throw new IllegalArgumentException("chance outside 0..1");
        int min = integer(json,"min_rolls",0,8), max = integer(json,"max_rolls",min,8);
        var entries = new ArrayList<Entry>();
        var array = json.has("entries")&&json.get("entries").isJsonArray()?json.getAsJsonArray("entries"):null;
        if (array == null || array.isEmpty() || array.size() > 64) throw new IllegalArgumentException("entries must contain 1..64 weighted entries");
        int invalid=0;
        boolean requireRecipe=bool(json,"require_recipe",false);
        for (var value : array) try {
            var e = value.getAsJsonObject();
            if (e.has("required_mod") && !ModList.get().isLoaded(e.get("required_mod").getAsString())) continue;
            int weight = integer(e,"weight",1,1_000_000);
            ResourceLocation item = e.has("item") ? ResourceLocation.parse(e.get("item").getAsString()) : ResourceLocation.withDefaultNamespace("enchanted_book");
            if (!BuiltInRegistries.ITEM.containsKey(item) || BuiltInRegistries.ITEM.get(item)==Items.AIR) throw new IllegalArgumentException("Unknown/empty item " + item);
            ResourceLocation enchant = e.has("enchantment") ? ResourceLocation.parse(e.get("enchantment").getAsString()) : null;
            if (enchant != null && !item.equals(ResourceLocation.withDefaultNamespace("enchanted_book"))) throw new IllegalArgumentException("enchantment entry must create enchanted_book");
            if (enchant == null && item.equals(ResourceLocation.withDefaultNamespace("enchanted_book"))) throw new IllegalArgumentException("enchanted_book requires enchantment ID");
            int minLevel=0,maxLevel=0;
            if (enchant != null) {
                var holder=registries.registryOrThrow(Registries.ENCHANTMENT).getHolder(enchant).orElseThrow(() -> new IllegalArgumentException("Unknown enchantment " + e.get("enchantment")));
                minLevel=integer(e,"min_level",1,holder.value().getMaxLevel());
                maxLevel=integer(e,"max_level",minLevel,holder.value().getMaxLevel());
                if(requireRecipe)for(int level=minLevel;level<=maxLevel;level++) {
                    final int n=level;final var ench=enchant;
                    if(dev.inscribed.recipe.InscribedRecipeRegistry.recipes().stream().noneMatch(r->r.enchantment().equals(ench) && r.level()==n))throw new IllegalArgumentException("No recipe for "+enchant+" level "+level);
                }
            }
            int minCount=e.has("min_count") ? integer(e,"min_count",1,4) : 1;
            int maxCount=e.has("max_count") ? integer(e,"max_count",minCount,4) : minCount;
            if (maxCount > BuiltInRegistries.ITEM.get(item).getDefaultInstance().getMaxStackSize()) throw new IllegalArgumentException("max_count exceeds item stack limit");
            entries.add(new Entry(item,enchant,weight,minLevel,maxLevel,minCount,maxCount));
        }catch(RuntimeException e){invalid++;LogUtils.getLogger().error("Invalid entry in Inscribed loot pool {}: {}; entry omitted",id,e.getMessage());}
        if(entries.isEmpty() && invalid>0)throw new IllegalArgumentException("No valid weighted entries remain");
        int structureTier=json.has("structure_tier") ? integer(json,"structure_tier",1,5) : 5;
        String theme=json.has("theme") ? json.get("theme").getAsString() : "UNSPECIFIED";
        if(theme.length()>64)throw new IllegalArgumentException("theme exceeds 64 characters");
        return new Pool(id,Set.copyOf(tables),chance,min,max,List.copyOf(entries),theme,structureTier,bool(json,"treasure",false),requireRecipe,entries.stream().mapToInt(Entry::weight).sum());
    }
    public static int inject(List<ItemStack> loot, LootContext context) {
        if (!InscribedConfig.LOOT_INJECTION.get()) return 0;
        int added = 0;
        var random = context.getRandom();
        for (var pool : byTable.getOrDefault(context.getQueriedLootTableId(),List.of())) {
            if (pool.entries().isEmpty()) continue;
            int rolls = pool.minRolls() + random.nextInt(pool.maxRolls()-pool.minRolls()+1);
            int total = pool.totalWeight();
            double chance=Math.min(1,pool.chance()*InscribedConfig.LOOT_MULTIPLIER.get()*(pool.treasure() ? InscribedConfig.TREASURE_LOOT_MULTIPLIER.get() : 1));
            for (int roll=0;roll<rolls && added<8;roll++) {
                if (random.nextDouble() >= chance) continue;
                int choice=random.nextInt(total);
                Entry selected=null;
                for (var entry:pool.entries()) {choice-=entry.weight();if(choice<0){selected=entry;break;}}
                if (selected==null) continue;
                boolean catalyst=selected.item().equals(ResourceLocation.fromNamespaceAndPath("inscribed","enchantment_catalyst"));
                if (catalyst && !InscribedConfig.CATALYST_LOOT.get() || selected.enchantment()!=null && !InscribedConfig.BOOK_LOOT.get()) continue;
                var item=new ItemStack(BuiltInRegistries.ITEM.get(selected.item()),selected.minCount()+random.nextInt(selected.maxCount()-selected.minCount()+1));
                if (selected.enchantment()!=null) {
                    var holder=context.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(selected.enchantment());
                    if(holder.isEmpty())continue;
                    item.enchant(holder.get(),selected.minLevel()+random.nextInt(selected.maxLevel()-selected.minLevel()+1));
                }
                loot.add(item);added++;
            }
            if (added>=8) break;
        }
        return added;
    }
    public static void clear(){pools=List.of();byTable=Map.of();lastConfig=null;}
    private static int integer(JsonObject o,String key,int min,int max) {
        var value=o.get(key);
        if(value==null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException(key+" must be integer");
        int n=value.getAsBigDecimal().intValueExact();
        if(n<min || n>max)throw new IllegalArgumentException(key+" outside "+min+".."+max);
        return n;
    }
    private static boolean bool(JsonObject o,String key,boolean fallback) {
        if(!o.has(key))return fallback;
        var value=o.get(key);
        if(!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean())throw new IllegalArgumentException(key+" must be boolean");
        return value.getAsBoolean();
    }
}
