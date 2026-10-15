package dev.inscribed.compat.lsbc;
import dev.inscribed.knowledge.*;
import dev.lsbc.unknowntext.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import java.util.ArrayList;
public final class LSBCKnowledgeIntegration implements KnowledgeIntegration {
    public Name render(ServerPlayer p,Component c){var r=UnknownTextRenderer.renderForPlayer(p,c);return new Name(r.visible(),r.searchable());}
    public void learn(ServerPlayer p,Component c){
        var words=new ArrayList<String>();
        var matcher=WordNormalization.TOKEN.matcher(ObservationLexicon.literal(p,c));
        while(matcher.find()) {var word=matcher.group();if(!UnknownWordManager.INSTANCE.isStopword(word)&&UnknownWordManager.INSTANCE.resolve(word).isPresent())words.add(word);}
        if(!words.isEmpty())WordKnowledgeService.learnObservedWords(p,words);
    }
    public void initialize(){NeoForge.EVENT_BUS.addListener((WordKnowledgeChangedEvent e)->KnowledgeService.sync(e.player(),false));}
}
