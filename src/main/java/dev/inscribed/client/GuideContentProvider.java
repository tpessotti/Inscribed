package dev.inscribed.client;
import java.util.*;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
/** One resource-backed document serves the standalone guide and the LSBC tab. */
public final class GuideContentProvider {
    public record Chapter(String id,String title,String body){}
    public static List<Chapter> chapters(){
        var mc=Minecraft.getInstance();String language=mc.getLanguageManager().getSelected();
        var manager=mc.getResourceManager();var resource=manager.getResource(ResourceLocation.parse("inscribed:guide/"+language+".json"));
        if(resource.isEmpty())resource=manager.getResource(ResourceLocation.parse("inscribed:guide/en_us.json"));
        if(resource.isEmpty())return List.of();
        try(var reader=resource.get().openAsReader()){
            var result=new ArrayList<Chapter>();for(var e:JsonParser.parseReader(reader).getAsJsonArray()){var o=e.getAsJsonObject();result.add(new Chapter(o.get("id").getAsString(),o.get("title").getAsString(),o.get("body").getAsString()));}return List.copyOf(result);
        }catch(Exception e){com.mojang.logging.LogUtils.getLogger().error("Invalid Inscribed guide resource",e);return List.of();}
    }
    private GuideContentProvider(){}
}
