package dev.inscribed.ritual;

import com.mojang.logging.LogUtils;
import dev.inscribed.altar.*;
import dev.inscribed.config.InscribedConfig;
import dev.inscribed.recipe.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.*;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** All calls run on the server thread. Inputs are logically reserved and physically untouched until commit. */
public final class RitualController {
    public record StartResult(boolean started, String reason, int matches) {}
    private record ResourceSlot(BlockPos pos, int slot) {}
    private static final Map<ServerLevel, Map<BlockPos, Session>> ACTIVE = new IdentityHashMap<>();
    private static final class Session {
        final AltarScanner.Snapshot altar;
        final RitualMatch match;
        final UUID player;
        final boolean registeredPlayer;
        final long generation;
        final Set<ResourceSlot> reserved;
        RitualState state = RitualState.VALIDATING;
        int ticks;
        Session(AltarScanner.Snapshot altar, RitualMatch match, UUID player) {
            this.altar = altar; this.match = match; this.player = player;this.registeredPlayer=altar.table().getLevel().getServer().getPlayerList().getPlayer(player)!=null; generation = InscribedRecipeRegistry.generation();
            var slots = new HashSet<ResourceSlot>();
            for(var book:match.books())slots.add(new ResourceSlot(book.slot().pos(),book.slot().slot()));
            for (var use : match.ingredients()) slots.add(new ResourceSlot(use.pedestal().pos(), -1));
            reserved = Set.copyOf(slots);
        }
    }
    public static RitualState state(Level level, BlockPos table) {
        var session = ACTIVE.getOrDefault(level, Map.of()).get(table); return session == null ? RitualState.IDLE : session.state;
    }
    public static StartResult start(ServerLevel level, BlockPos table, UUID player) {
        if (state(level, table) != RitualState.IDLE) return new StartResult(false, "already_running", 0);
        var altar = AltarScanner.scan(level, table);
        if (!altar.valid()) return new StartResult(false, altar.failure(), 0);
        if (altar.target().isEmpty()) return new StartResult(false, "missing_target", 0);
        var matches = RitualMatcher.matches(level, altar);
        var extractions=matches.stream().filter(m->m.type()==RitualType.EXTRACTION).toList();
        if(!extractions.isEmpty()) {
            if(extractions.size()!=1)return new StartResult(false,"ambiguous_extraction",extractions.size());
            return startMatch(level,altar,extractions.getFirst(),player);
        }
        if (matches.size() != 1) return new StartResult(false, matches.isEmpty() ? RitualDiagnostics.reason(level,altar) : "ambiguous", matches.size());
        return startMatch(level,altar,matches.getFirst(),player);
    }
    public static StartResult startSelected(ServerLevel level,BlockPos table,UUID player,net.minecraft.resources.ResourceLocation recipe,RitualType type) {
        if(state(level,table)!=RitualState.IDLE)return new StartResult(false,"already_running",0);
        var altar=AltarScanner.scan(level,table);
        if(!altar.valid())return new StartResult(false,altar.failure(),0);
        var chosen=RitualMatcher.matches(level,altar).stream().filter(m->m.type()==type && m.recipe().id().equals(recipe)).findFirst();
        return chosen.isEmpty() ? new StartResult(false,"recipe_changed",0) : startMatch(level,altar,chosen.get(),player);
    }
    private static StartResult startMatch(ServerLevel level,AltarScanner.Snapshot altar,RitualMatch match,UUID player) {
        var table=altar.tablePos();var session = new Session(altar, match, player);
        for (var active : ACTIVE.getOrDefault(level, Map.of()).values())
            if (!Collections.disjoint(active.reserved, session.reserved)) return new StartResult(false, "resource_reserved", 1);
        ACTIVE.computeIfAbsent(level, unused -> new LinkedHashMap<>()).put(table.immutable(), session);
        session.state = RitualState.RUNNING;
        AltarStateService.visual(altar.table(),match.duration(),match.type()==RitualType.EXTRACTION,false);
        for (var use : session.match.ingredients()) use.pedestal().owner().visual(match.duration(),false);
        RitualVisuals.start(level, table, match.type()); return new StartResult(true, "started", 1);
    }
    public static void tick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        for (var session : List.copyOf(ACTIVE.getOrDefault(level, Map.of()).values())) {
            String failure = unchanged(level, session);
            if (!failure.isEmpty()) { cancel(level, session, failure); continue; }
            int duration = session.match.duration();
            session.ticks++; RitualVisuals.running(level, session.altar.tablePos(), session.match, session.ticks, duration);
            if (session.ticks >= duration) complete(level, session);
        }
    }
    private static String unchanged(ServerLevel level, Session s) {
        if(s.registeredPlayer){var actor=level.getServer().getPlayerList().getPlayer(s.player);if(actor==null||!actor.isAlive()||actor.isSpectator()||actor.serverLevel()!=level)return "player_left";}
        if (s.generation != InscribedRecipeRegistry.generation()) return "recipes_reloaded";
        if (!s.altar.settings().equals(InscribedConfig.settings())) return "config_changed";
        for (var chunk : s.altar.chunks()) if (!level.hasChunk(chunk.x, chunk.z)) return "unloaded_chunk";
        if (s.altar.table().isRemoved() || level.getBlockEntity(s.altar.tablePos()) != s.altar.table()) return "table_removed";
        var target = AltarStateService.get(s.altar.table());
        if (target.revision() != s.altar.targetRevision() || !ItemStack.matches(target.target(), s.altar.target())) return "target_changed";
        for(var use:s.match.books())if(level.getBlockEntity(use.slot().pos())!=use.slot().owner() || !use.slot().unchanged())return "book_changed";
        for (var use : s.match.ingredients()) {
            var p = use.pedestal();
            if (p.owner().isRemoved() || level.getBlockEntity(p.pos()) != p.owner() || p.owner().revision() != p.revision()
                    || !ItemStack.matches(p.stack(), p.owner().item())) return "ingredient_changed";
        }
        if (BookshelfPowerService.total(level, s.altar.powerSources()) < s.match.recipe().minimumPower()) return "insufficient_power";
        return "";
    }
    private static void complete(ServerLevel level, Session s) {
        String failure = unchanged(level, s);
        if (!failure.isEmpty()) { cancel(level, s, failure); return; }
        var fresh = AltarScanner.scan(level, s.altar.tablePos());
        if (!fresh.valid()) { cancel(level, s, fresh.failure()); return; }
        var matches = RitualMatcher.matches(level, fresh);
        if (matches.stream().noneMatch(m->m.type()==s.match.type() && m.recipe().id().equals(s.match.recipe().id()))) { cancel(level, s, "recipe_changed"); return; }
        // Every physical input, identity, count and component has been checked before this point.
        var result = s.altar.target();
        if (s.match.type()==RitualType.INFUSION && result.is(Items.BOOK)) result = result.transmuteCopy(Items.ENCHANTED_BOOK);
        var enchantment = s.match.enchantment(); int targetLevel = s.match.recipe().level();
        if(s.match.type()==RitualType.EXTRACTION)EnchantmentHelper.updateEnchantments(result,mutable->mutable.removeIf(enchantment::equals));
        else EnchantmentHelper.updateEnchantments(result, mutable -> mutable.set(enchantment, targetLevel));
        double chance = s.match.type()==RitualType.INFUSION ? s.match.recipe().infusion().orElseThrow().bookConsumptionChance() : 0;
        boolean consumeBook = chance >= 1 || (chance > 0 && level.random.nextDouble() < chance);
        double failureChance=s.match.type()==RitualType.EXTRACTION ? s.match.recipe().extraction().orElseThrow().failureChance() : 0;
        boolean extractionFailed=failureChance>=1 || (failureChance>0 && level.random.nextDouble()<failureChance);
        s.state = RitualState.COMPLETING;
        ACTIVE.get(level).remove(s.altar.tablePos()); // Release the logical lease before controlled inventory mutations.
        try {
            AltarStateService.target(s.altar.table(), result);
            for (var use : s.match.ingredients()) {
                var remaining = use.pedestal().stack(); remaining.shrink(use.count()); use.pedestal().owner().setItem(remaining);
            }
            if (consumeBook) { s.match.book().owner().removeItem(s.match.book().slot(), 1); s.match.book().owner().setChanged(); }
            if(s.match.type()==RitualType.EXTRACTION) {
                for(var use:s.match.books()) { var remaining=use.slot().stack();remaining.shrink(use.count());use.slot().owner().setItem(use.slot().slot(),remaining);use.slot().owner().setChanged(); }
                if(!extractionFailed) {
                    var output=new ItemStack(Items.ENCHANTED_BOOK);output.enchant(enchantment,targetLevel);
                    s.match.book().owner().setItem(s.match.book().slot(),output);s.match.book().owner().setChanged();
                }
                for(var use:s.match.books()){var pos=use.slot().pos();level.sendBlockUpdated(pos,level.getBlockState(pos),level.getBlockState(pos),3);}
            }
            resetVisuals(s);
            if(!extractionFailed) {
                AltarStateService.visual(s.altar.table(),s.match.duration(),s.match.type()==RitualType.EXTRACTION,true);
                for(var use:s.match.ingredients())use.pedestal().owner().visual(s.match.duration(),true);
            }
            if(extractionFailed)RitualVisuals.failed(level,s.altar.tablePos());else RitualVisuals.complete(level, s.altar.tablePos(), s.match);
            message(level, s.player, extractionFailed ? "extraction_failed" : "completed"); s.state = RitualState.IDLE;
        } catch (RuntimeException ex) {
            // Roll back the staged snapshots if a third-party inventory hook throws during commit.
            AltarStateService.target(s.altar.table(), s.altar.target());
            for (var use : s.match.ingredients()) use.pedestal().owner().setItem(use.pedestal().stack());
            for(var use:s.match.books()) { use.slot().owner().setItem(use.slot().slot(),use.slot().stack());use.slot().owner().setChanged(); }
            resetVisuals(s); s.state = RitualState.CANCELLED;
            LogUtils.getLogger().error("Inscribed commit rolled back at {}", s.altar.tablePos(), ex); message(level, s.player, "commit_failed");
        }
        if(dev.inscribed.config.InscribedConfig.KNOWLEDGE_EXTRACTION.get() && s.state==RitualState.IDLE && s.match.type()==RitualType.EXTRACTION && !extractionFailed) {
            var player=level.getServer().getPlayerList().getPlayer(s.player);
            if(player!=null)try{dev.inscribed.knowledge.KnowledgeService.change(player,s.match.recipe().enchantment(),true,true);}
            catch(RuntimeException e){LogUtils.getLogger().error("Extraction committed; knowledge update failed",e);}
        }
    }
    public static void cancelAt(Level level, BlockPos table, String reason) {
        if (!(level instanceof ServerLevel server)) return;
        var session = ACTIVE.getOrDefault(server, Map.of()).get(table);
        if (session != null) cancel(server, session, reason);
    }
    public static void cancelUsing(Level level, BlockPos resource, String reason) {
        if (!(level instanceof ServerLevel server)) return;
        for (var s : List.copyOf(ACTIVE.getOrDefault(server, Map.of()).values()))
            if (s.reserved.stream().anyMatch(slot -> slot.pos().equals(resource))) cancel(server, s, reason);
    }
    private static void cancel(ServerLevel level, Session s, String reason) {
        s.state = RitualState.CANCELLED; ACTIVE.get(level).remove(s.altar.tablePos()); resetVisuals(s);
        var player = level.getServer().getPlayerList().getPlayer(s.player);
        if (player != null) player.displayClientMessage(Component.translatable("message.inscribed.cancelled", Component.translatable("reason.inscribed." + reason)), true);
    }
    private static void resetVisuals(Session s) {
        if (!s.altar.table().isRemoved()) AltarStateService.running(s.altar.table(), false);
        for (var use : s.match.ingredients()) if (!use.pedestal().owner().isRemoved()) use.pedestal().owner().running(false);
    }
    private static void message(ServerLevel level, UUID playerId, String key) {
        var player = level.getServer().getPlayerList().getPlayer(playerId);
        if (player != null) player.displayClientMessage(Component.translatable("message.inscribed." + key), true);
    }
    public static void chunkUnload(ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        for (var s : List.copyOf(ACTIVE.getOrDefault(level, Map.of()).values()))
            if (s.altar.chunks().contains(event.getChunk().getPos())) cancel(level, s, "unloaded_chunk");
    }
    public static void levelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            for (var s : List.copyOf(ACTIVE.getOrDefault(level, Map.of()).values())) cancel(level, s, "unloaded_chunk");
            ACTIVE.remove(level);
        }
    }
    public static void leave(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event){
        if(event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)
            for(var session:List.copyOf(ACTIVE.getOrDefault(level,Map.of()).values()))if(session.player.equals(player.getUUID()))cancel(level,session,"player_left");
    }
    public static void stop(ServerStoppedEvent event) { ACTIVE.clear(); InscribedRecipeRegistry.clear();dev.inscribed.config.ConfigRecipeStore.clearCache();dev.inscribed.loot.LootInjection.clear(); }
    private RitualController() {}
}
