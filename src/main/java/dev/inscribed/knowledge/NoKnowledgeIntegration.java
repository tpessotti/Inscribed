package dev.inscribed.knowledge;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
public final class NoKnowledgeIntegration implements KnowledgeIntegration {
    public Name render(ServerPlayer p,Component c){return new Name(c,c);}
    public void learn(ServerPlayer p,Component c){}
}
