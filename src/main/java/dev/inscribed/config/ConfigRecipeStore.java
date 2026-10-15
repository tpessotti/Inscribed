package dev.inscribed.config;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

/** Server-owned JSON config. Defaults are exported once; later edits are never overwritten. */
public final class ConfigRecipeStore {
    private static JsonObject lastGood;
    public static Path path() { return FMLPaths.CONFIGDIR.get().resolve("inscribed/recipes.json"); }
    public static JsonObject load() {
        try {
            var path=path();
            if(!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                try(var input=ConfigRecipeStore.class.getResourceAsStream("/inscribed/default-recipes.json")) {
                    if(input==null)throw new IOException("Missing bundled recipe config");
                    Files.copy(input,path);
                }
            }
            if(Files.size(path)>5_000_000)throw new IOException("Recipe config exceeds 5 MB");
            try(var reader=Files.newBufferedReader(path,StandardCharsets.UTF_8)) {
                var document=JsonParser.parseReader(reader).getAsJsonObject();validateDocument(document);
                lastGood=document.deepCopy();return document;
            }
        }catch(IOException|RuntimeException ex){
            LogUtils.getLogger().error("Inscribed recipe config {} rejected; keeping last valid config: {}",path(),ex.getMessage());
            if(lastGood!=null)return lastGood.deepCopy();
            // First-boot parse failure must not silently reactivate disabled recipes.
            var empty=new JsonObject();empty.addProperty("include_datapack_recipes",false);return empty;
        }
    }
    public static void validateDocument(JsonObject document) {
        if(!document.has("format_version") || !document.get("format_version").isJsonPrimitive() || !document.get("format_version").getAsJsonPrimitive().isNumber()
            || document.get("format_version").getAsBigDecimal().intValueExact()!=1)throw new IllegalArgumentException("format_version must be 1");
        if(document.has("include_datapack_recipes") && (!document.get("include_datapack_recipes").isJsonPrimitive() || !document.get("include_datapack_recipes").getAsJsonPrimitive().isBoolean()))throw new IllegalArgumentException("include_datapack_recipes must be boolean");
        for(String field:List.of("enchantment_recipes","crafting_recipes"))if(document.has(field) && !document.get(field).isJsonObject())throw new IllegalArgumentException(field+" must be an object keyed by recipe ID");
    }
    public static Map<ResourceLocation,JsonElement> merge(JsonObject document,Map<ResourceLocation,JsonElement> data,String field) {
        var result=new TreeMap<ResourceLocation,JsonElement>();
        // This flag only replaces Inscribed ritual recipes; unrelated crafting/datapack recipes remain intact.
        if(!field.equals("enchantment_recipes") || !document.has("include_datapack_recipes") || document.get("include_datapack_recipes").getAsBoolean())result.putAll(data);
        var entries=document.getAsJsonObject(field);if(entries==null)return result;
        for(var entry:entries.entrySet()) {
            try {
                var id=ResourceLocation.parse(entry.getKey());var value=entry.getValue().getAsJsonObject();
                if(value.has("enabled")) {
                    if(!value.get("enabled").isJsonPrimitive() || !value.get("enabled").getAsJsonPrimitive().isBoolean())throw new IllegalArgumentException("enabled must be boolean");
                    if(!value.get("enabled").getAsBoolean()){result.remove(id);continue;}
                }
                var copy=value.deepCopy();copy.remove("enabled");result.put(id,copy);
            }catch(RuntimeException ex){LogUtils.getLogger().error("Invalid Inscribed config entry {} / {}: {}",field,entry.getKey(),ex.getMessage());}
        }
        return result;
    }
    public static void clearCache(){lastGood=null;}
    private ConfigRecipeStore() {}
}
