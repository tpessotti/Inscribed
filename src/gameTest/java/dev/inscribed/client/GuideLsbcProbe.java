package dev.inscribed.client;
import dev.lsbc.unknowntext.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
public final class GuideLsbcProbe {
    public static void partial(ServerPlayer p){
        var h=p.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(net.minecraft.resources.ResourceKey.create(Registries.ENCHANTMENT,ResourceLocation.parse("minecraft:fire_protection")));
        var literal=ObservationLexicon.literal(p,h.value().description());var m=WordNormalization.TOKEN.matcher(literal);var words=new java.util.ArrayList<String>();while(m.find())if(!UnknownWordManager.INSTANCE.isStopword(m.group()))words.add(m.group());
        if(words.size()<2)throw new AssertionError("Multiword test name not localized: "+literal);
        for(var word:words)WordKnowledgeService.forgetWord(p,word);
        WordKnowledgeService.learnObservedWords(p,java.util.List.of(words.getFirst()));
        var rendered=UnknownTextRenderer.renderForPlayer(p,h.value().description());
        if(!rendered.visible().getString().equals(literal)||!UnknownTextRenderer.hasUnknown(rendered.visible())||rendered.searchable().getString().contains(words.getLast()))throw new AssertionError("Partial KnownWords changed identity or leaked search");
    }
    public static void openCodex(){net.minecraft.client.Minecraft.getInstance().setScreen(null);dev.lsbc.client.codex.CodexClient.open();}
}
