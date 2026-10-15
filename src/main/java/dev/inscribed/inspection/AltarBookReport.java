package dev.inscribed.inspection;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.WritableBookContent;

/** Append-only plain-text report, staged before changing the player's writable book. */
public final class AltarBookReport {
    private static final Map<String,Map<String,String>> LANGUAGES = new HashMap<>();
    public static boolean append(ItemStack book, InspectionPayload report, String language) {
        if (!book.is(Items.WRITABLE_BOOK)) return false;
        var original = book.getOrDefault(DataComponents.WRITABLE_BOOK_CONTENT, WritableBookContent.EMPTY);
        var text = new StringBuilder();
        var translations = language(language);
        text.append(localize(Component.translatable("inspection.inscribed.title"),translations)).append('\n');
        text.append(report.table().getX()).append(", ").append(report.table().getY()).append(", ").append(report.table().getZ()).append("\n\n");
        var target = report.target().isEmpty() ? Component.translatable("inspection.inscribed.no_target") : report.target().getHoverName();
        text.append(localize(Component.translatable("inspection.inscribed.target",target),translations)).append('\n');
        text.append(localize(Component.translatable("inspection.inscribed.power",String.format(Locale.ROOT,"%.1f",report.power())),translations)).append("\n\n");
        for (var row : report.rows()) text.append(localize(row.text(),translations)).append('\n');
        if (!report.failure().isEmpty()) text.append('\n').append(localize(Component.translatable("message.inscribed."+report.failure()),translations));
        var additions = pages(text.toString());
        if (original.pages().size()+additions.size()>100) return false;
        var result = new ArrayList<>(original.pages());
        for (String page : additions) result.add(Filterable.passThrough(page));
        book.set(DataComponents.WRITABLE_BOOK_CONTENT,new WritableBookContent(result));
        return true;
    }
    /** Conservative vanilla font layout: <=18 code points/line and 12 lines/page. */
    public static List<String> pages(String text) {
        var lines = new ArrayList<String>();
        for (String paragraph : text.split("\n",-1)) {
            String remaining = paragraph;
            while (remaining.codePointCount(0,remaining.length())>18) {
                int cut = remaining.offsetByCodePoints(0,18), space = remaining.lastIndexOf(' ',cut);
                if (space>0) cut=space;
                lines.add(remaining.substring(0,cut)); remaining=remaining.substring(cut).stripLeading();
            }
            lines.add(remaining);
        }
        var result = new ArrayList<String>();
        for(int start=0;start<lines.size();start+=12) result.add(String.join("\n",lines.subList(start,Math.min(lines.size(),start+12))));
        return List.copyOf(result);
    }
    private static Map<String,String> language(String requested) {
        String code = requested.toLowerCase(Locale.ROOT).matches("[a-z]{2}_[a-z]{2}") ? requested.toLowerCase(Locale.ROOT) : "en_us";
        return LANGUAGES.computeIfAbsent(code,key -> {
            var result = new HashMap<String,String>();
            load(result,"/data/inscribed/book_languages/en_us.json"); load(result,"/assets/inscribed/lang/en_us.json");
            load(result,"/data/inscribed/book_languages/"+key+".json"); load(result,"/assets/inscribed/lang/"+key+".json");
            return Map.copyOf(result);
        });
    }
    private static void load(Map<String,String> into,String path) {
        try(var stream=AltarBookReport.class.getResourceAsStream(path)) {
            if(stream==null)return;
            try(var reader=new InputStreamReader(stream,StandardCharsets.UTF_8)) {
                JsonParser.parseReader(reader).getAsJsonObject().asMap().forEach((k,v)->into.put(k,v.getAsString()));
            }
        } catch(java.io.IOException e) { throw new IllegalStateException("Could not read book report language",e); }
    }
    private static String localize(Component component,Map<String,String> translations) {
        String value;
        if(component.getContents() instanceof TranslatableContents content && translations.containsKey(content.getKey())) {
            Object[] args=Arrays.stream(content.getArgs()).map(a->a instanceof Component c ? localize(c,translations) : a).toArray();
            value=String.format(Locale.ROOT,translations.get(content.getKey()),args);
        } else value=component.getContents().visit(Optional::of).orElse("");
        var result=new StringBuilder(value);
        for(var sibling:component.getSiblings())result.append(localize(sibling,translations));
        return result.toString();
    }
    private AltarBookReport() {}
}
