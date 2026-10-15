package dev.inscribed.inspection;

import dev.inscribed.altar.AltarScanner;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** A lens analysis is a separate read-only session, never a ritual or a reservation. */
public final class AltarInspection {
    private static final ResourceLocation LENS = ResourceLocation.parse("lsbc:magnifying_glass");
    private record Session(BlockPos pos, ResourceKey<Level> dimension, InteractionHand hand, long started) {}
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    public static boolean isLens(ItemStack stack) { return dev.inscribed.config.InscribedConfig.ENABLE_LENS.get() && !stack.isEmpty() && LENS.equals(BuiltInRegistries.ITEM.getKey(stack.getItem())); }
    public static boolean isBookFallback(ItemStack stack) { return dev.inscribed.config.InscribedConfig.ENABLE_BOOK.get() && !net.neoforged.fml.ModList.get().isLoaded("lsbc") && stack.is(Items.WRITABLE_BOOK); }
    public static boolean isInspectionTool(ItemStack stack) { return isLens(stack) || isBookFallback(stack); }
    public static void writeBook(ServerPlayer player, BlockPos pos, InteractionHand hand) {
        var book = player.getItemInHand(hand);
        if (!isBookFallback(book) || player.getCooldowns().isOnCooldown(Items.WRITABLE_BOOK) || player.isSpectator() || !lookingAt(player,pos)) return;
        var report = snapshot(AltarScanner.scan(player.serverLevel(),pos),player);
        if (!AltarBookReport.append(book, report, player.clientInformation().language())) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("inspection.inscribed.book_full"),true); return;
        }
        player.getInventory().setChanged(); player.inventoryMenu.broadcastChanges();
        player.getCooldowns().addCooldown(Items.WRITABLE_BOOK,dev.inscribed.config.InscribedConfig.BOOK_COOLDOWN.get());
        PacketDistributor.sendToPlayer(player, report);
    }
    public static void begin(ServerPlayer player, BlockPos pos, InteractionHand hand) {
        if (!isLens(player.getItemInHand(hand)) || player.isSpectator() || !player.serverLevel().hasChunkAt(pos)
                || !player.serverLevel().getBlockState(pos).is(Blocks.ENCHANTING_TABLE)
                || player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > Math.pow(dev.inscribed.config.InscribedConfig.INSPECTION_REACH.get()+1,2)) return;
        var previous = SESSIONS.get(player.getUUID());
        if (previous != null && previous.pos().equals(pos) && previous.dimension().equals(player.level().dimension())
                && previous.hand() == hand && player.isUsingItem()) return;
        SESSIONS.put(player.getUUID(), new Session(pos.immutable(), player.level().dimension(), hand, player.level().getGameTime()));
        player.startUsingItem(hand);
        player.playNotifySound(SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 1, 1);
        PacketDistributor.sendToPlayer(player, InspectionPayload.progress(pos, 0));
    }
    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var session = SESSIONS.get(player.getUUID()); if (session == null) return;
        if (!player.isAlive() || player.isSpectator() || !player.isUsingItem() || player.getUsedItemHand() != session.hand()
                || !isLens(player.getUseItem()) || !session.dimension().equals(player.level().dimension())
                || !lookingAt(player, session.pos())) { cancel(player, session); return; }
        long elapsed = player.level().getGameTime() - session.started();
        int duration=dev.inscribed.config.InscribedConfig.INSPECTION_TICKS.get();
        if (elapsed >= duration) {
            var snapshot = snapshot(AltarScanner.scan(player.serverLevel(), session.pos()),player);
            SESSIONS.remove(player.getUUID());
            PacketDistributor.sendToPlayer(player, snapshot);
            player.stopUsingItem();
            player.playNotifySound(SoundEvents.SPYGLASS_STOP_USING, SoundSource.PLAYERS, 1, 1);
        } else if (elapsed % 5 == 0) PacketDistributor.sendToPlayer(player, InspectionPayload.progress(session.pos(), (int)elapsed * 100 / duration));
    }
    public static boolean lookingAt(ServerPlayer player, BlockPos pos) {
        var level = player.serverLevel();
        if (!level.hasChunkAt(pos) || !level.getBlockState(pos).is(Blocks.ENCHANTING_TABLE)) return false;
        Vec3 eye = player.getEyePosition(), direction = player.getLookAngle();
        Vec3 end = eye.add(direction.scale(dev.inscribed.config.InscribedConfig.INSPECTION_REACH.get()+.000001));
        var context = new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        // Loaded-only ray, like the LSBC scanner; an unloaded boundary cancels instead of generating terrain.
        var hit = BlockGetter.traverseBlocks(eye, end, context, (clip, at) -> {
            if (!level.hasChunkAt(at)) return BlockHitResult.miss(eye, net.minecraft.core.Direction.UP, at);
            var state = level.getBlockState(at);
            return level.clipWithInteractionOverride(eye, end, at, clip.getBlockShape(state, level, at), state);
        }, clip -> BlockHitResult.miss(end, net.minecraft.core.Direction.UP, BlockPos.containing(end)));
        return hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(pos);
    }
    public static InspectionPayload snapshot(AltarScanner.Snapshot altar) {return snapshot(altar,null);}
    public static InspectionPayload snapshot(AltarScanner.Snapshot altar,net.minecraft.server.level.ServerPlayer player) {
        if (altar.table() == null) return new InspectionPayload(altar.tablePos(), 100, altar.target(), altar.power(), List.of(), altar.failure());
        var rows = new ArrayList<InspectionPayload.Row>();
        rows.add(new InspectionPayload.Row(ItemStack.EMPTY, Component.translatable("inspection.inscribed.ingredients")));
        int ingredients = 0;
        for (var pedestal : altar.pedestals()) {
            var stack = pedestal.stack(); if (stack.isEmpty()) continue;
            ingredients++;
            rows.add(new InspectionPayload.Row(stack, Component.translatable("inspection.inscribed.stack", stack.getCount(), stack.getHoverName())));
        }
        if (ingredients == 0) rows.add(new InspectionPayload.Row(ItemStack.EMPTY, Component.translatable("inspection.inscribed.none")));
        rows.add(new InspectionPayload.Row(ItemStack.EMPTY, Component.translatable("inspection.inscribed.books", altar.books().enchanted().size())));
        for (var book : altar.books().enchanted()) {
            var stack = book.stack(); var text = Component.empty();
            var entries = new ArrayList<>(EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet());
            entries.sort(Comparator.comparing(e -> e.getKey().getRegisteredName()));
            for (var entry : entries) {
                if (!text.getString().isEmpty()) text.append(Component.literal(", "));
                text.append(player==null?Enchantment.getFullname(entry.getKey(), entry.getIntValue()):dev.inscribed.knowledge.KnowledgeService.enchantmentName(player,entry.getKey(),entry.getIntValue()));
            }
            if (entries.isEmpty()) text.append(stack.getHoverName());
            rows.add(new InspectionPayload.Row(stack, text));
        }
        if (altar.books().enchanted().isEmpty()) rows.add(new InspectionPayload.Row(ItemStack.EMPTY, Component.translatable("inspection.inscribed.none")));
        rows.add(new InspectionPayload.Row(new ItemStack(Items.BOOK), Component.translatable("inspection.inscribed.empty_books", altar.books().empty().stream().mapToInt(b -> b.stack().getCount()).sum())));
        // Custom payloads are bounded: unusually large modded stacks/libraries remain inspectable.
        // Normal configured libraries fit in full; disclose any overflow rather than silently hiding it.
        var bounded = new ArrayList<InspectionPayload.Row>(); int bytes = 0, omitted = 0;
        for (var row : rows) {
            var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), altar.table().getLevel().registryAccess());
            try {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, row.icon());
                net.minecraft.network.chat.ComponentSerialization.STREAM_CODEC.encode(buffer, row.text());
                bytes += buffer.readableBytes();
                if (bytes > 700_000 || bounded.size() >= 8190) omitted++;
                else bounded.add(row);
            } finally { buffer.release(); }
        }
        if (omitted > 0) bounded.add(new InspectionPayload.Row(ItemStack.EMPTY, Component.translatable("inspection.inscribed.omitted", omitted)));
        return new InspectionPayload(altar.tablePos(), 100, altar.target(), altar.power(), bounded, altar.failure());
    }
    private static void cancel(ServerPlayer player, Session session) {
        SESSIONS.remove(player.getUUID()); PacketDistributor.sendToPlayer(player, InspectionPayload.progress(session.pos(), -1));
        player.stopUsingItem();
    }
    public static void leave(EntityLeaveLevelEvent event) { if (event.getEntity() instanceof ServerPlayer p) SESSIONS.remove(p.getUUID()); }
    public static void stop(ServerStoppedEvent event) { SESSIONS.clear(); }
    private AltarInspection() {}
}
