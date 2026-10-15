package dev.inscribed.knowledge;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
public interface KnowledgeIntegration {
    record Name(Component visible, Component searchable) {}
    Name render(ServerPlayer player, Component name);
    void learn(ServerPlayer player, Component name);
    default void initialize() {}
}
