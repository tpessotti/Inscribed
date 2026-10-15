package dev.inscribed.ritual;

import dev.inscribed.altar.*;
import dev.inscribed.recipe.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.network.PacketDistributor;

public final class RitualSelection {
    private record Session(UUID token,AltarScanner.Snapshot altar,List<RitualMatch> matches,long generation,long expires) {}
    private static final Map<UUID,Session> SESSIONS=new HashMap<>();
    public static SelectionPayload open(ServerPlayer player,BlockPos pos) {
        var altar=AltarScanner.scan(player.serverLevel(),pos);var all=RitualMatcher.matches(player.serverLevel(),altar);
        if(all.stream().anyMatch(m->m.type()==RitualType.EXTRACTION))return null;
        var matches=all.stream().filter(m->m.type()==RitualType.INFUSION).toList();
        if(matches.size()<2 || matches.size()>4096 || !near(player,altar))return null;
        var token=UUID.randomUUID();var options=new ArrayList<SelectionPayload.Option>();
        for(var m:matches) {
            var ingredients=Component.empty();
            for(var use:m.ingredients()) {if(!ingredients.getString().isEmpty())ingredients.append(", ");ingredients.append(Component.translatable("inspection.inscribed.stack",use.count(),use.pedestal().stack().getHoverName()));}
            if(m.type()==RitualType.EXTRACTION)ingredients.append(Component.translatable("selection.inscribed.empty_books",m.recipe().extraction().orElseThrow().emptyBooksRequired()));
            options.add(new SelectionPayload.Option(dev.inscribed.knowledge.KnowledgeService.enchantmentName(player,m.enchantment(),m.recipe().level()),m.type(),m.duration(),m.recipe().minimumPower(),m.type()==RitualType.INFUSION ? m.recipe().infusion().orElseThrow().bookConsumptionChance() : -1,ingredients));
        }
        SESSIONS.put(player.getUUID(),new Session(token,altar,matches,InscribedRecipeRegistry.generation(),player.level().getGameTime()+dev.inscribed.config.InscribedConfig.SELECTION_TIMEOUT.get()));
        var payload=new SelectionPayload(token,options);PacketDistributor.sendToPlayer(player,payload);return payload;
    }
    public static RitualController.StartResult choose(ServerPlayer player,UUID token,int index) {
        var session=SESSIONS.get(player.getUUID());
        if(session==null || !session.token().equals(token))return new RitualController.StartResult(false,"selection_expired",0);
        SESSIONS.remove(player.getUUID());
        if(index==-1)return new RitualController.StartResult(false,"selection_closed",0);
        if(index<0 || index>=session.matches().size() || !near(player,session.altar()) || session.generation()!=InscribedRecipeRegistry.generation()
                || player.level().getGameTime()>session.expires() || session.altar().table().isRemoved()
                || player.level().getBlockEntity(session.altar().tablePos())!=session.altar().table()
                || AltarStateService.get(session.altar().table()).revision()!=session.altar().targetRevision())return new RitualController.StartResult(false,"selection_expired",0);
        var match=session.matches().get(index);
        if(RitualMatcher.matches(player.serverLevel(),AltarScanner.scan(player.serverLevel(),session.altar().tablePos())).stream().anyMatch(m->m.type()==RitualType.EXTRACTION))return new RitualController.StartResult(false,"recipe_changed",0);
        return RitualController.startSelected(player.serverLevel(),session.altar().tablePos(),player.getUUID(),match.recipe().id(),match.type());
    }
    private static boolean near(ServerPlayer p,AltarScanner.Snapshot altar) {
        return altar.table()!=null && p.isAlive() && !p.isSpectator() && p.level()==altar.table().getLevel()
            && p.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(altar.tablePos()))<=Math.pow(dev.inscribed.config.InscribedConfig.SELECTION_REACH.get(),2) && p.level().mayInteract(p,altar.tablePos());
    }
    public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if(event.getEntity() instanceof ServerPlayer p) {var s=SESSIONS.get(p.getUUID());if(s!=null && (!near(p,s.altar()) || p.level().getGameTime()>s.expires()))SESSIONS.remove(p.getUUID());}
    }
    public static void leave(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event){if(event.getEntity() instanceof ServerPlayer p)SESSIONS.remove(p.getUUID());}
    public static void stop(net.neoforged.neoforge.event.server.ServerStoppedEvent event){SESSIONS.clear();}
    private RitualSelection() {}
}
