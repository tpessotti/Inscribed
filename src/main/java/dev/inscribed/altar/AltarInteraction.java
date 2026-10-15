package dev.inscribed.altar;

import dev.inscribed.config.InscribedConfig;
import dev.inscribed.ritual.RitualController;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class AltarInteraction {
    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled() || !event.getLevel().getBlockState(event.getPos()).is(Blocks.ENCHANTING_TABLE)) return;
        var player = event.getEntity(); var level = event.getLevel();
        // Let the optional LSBC lens inspect without inserting it or starting an infusion.
        var lensHand = dev.inscribed.inspection.AltarInspection.isInspectionTool(player.getItemInHand(event.getHand())) ? event.getHand()
                : event.getHand() == InteractionHand.MAIN_HAND && dev.inscribed.inspection.AltarInspection.isInspectionTool(player.getOffhandItem()) ? InteractionHand.OFF_HAND : null;
        if (player.isShiftKeyDown() && lensHand != null) {
            event.setCanceled(true); event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            if (player.isSpectator() || !level.mayInteract(player, event.getPos())) return;
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                if (dev.inscribed.inspection.AltarInspection.isBookFallback(player.getItemInHand(lensHand)))
                    dev.inscribed.inspection.AltarInspection.writeBook(serverPlayer,event.getPos(),lensHand);
                else dev.inscribed.inspection.AltarInspection.begin(serverPlayer, event.getPos(), lensHand);
            } else if (level.isClientSide && dev.inscribed.inspection.AltarInspection.isLens(player.getItemInHand(lensHand))) player.startUsingItem(lensHand);
            return;
        }
        // Capture both hands, but perform a single main-hand operation; vanilla's off-hand fallback cannot duplicate it.
        event.setCanceled(true); event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        if (event.getHand() != InteractionHand.MAIN_HAND || player.isSpectator() || !level.mayInteract(player, event.getPos()) || !player.isAlive() || player.getEyePosition().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(event.getPos())) > Math.pow(player.blockInteractionRange()+1,2)) return;
        if (level.isClientSide || !(level instanceof ServerLevel server) || !(level.getBlockEntity(event.getPos()) instanceof EnchantingTableBlockEntity table)) return;
        var held = player.getMainHandItem(); var state = AltarStateService.get(table);
        if (player.isShiftKeyDown() || (!InscribedConfig.REQUIRE_SHIFT.get() && !held.isEmpty() && !state.target().isEmpty())) {
            var result = RitualController.start(server, event.getPos(), player.getUUID());
            if(result.reason().equals("ambiguous") && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                if(dev.inscribed.ritual.RitualSelection.open(serverPlayer,event.getPos())!=null)return;
            }
            player.displayClientMessage(Component.translatable("message.inscribed." + result.reason(), result.matches()), true);
            if(!result.started())dev.inscribed.ritual.RitualVisuals.invalid(server,event.getPos(),player);
        } else if (held.isEmpty()) {
            var stack = state.target();
            if (!stack.isEmpty()) { AltarStateService.target(table, ItemStack.EMPTY); returnItem(player, stack); }
        } else if (state.target().isEmpty() && validTarget(held, level.registryAccess())) {
            AltarStateService.target(table, held.copyWithCount(1)); held.shrink(1);
        } else {
            player.displayClientMessage(Component.translatable(state.target().isEmpty() ? "message.inscribed.invalid_target" : "message.inscribed.target_occupied"), true);
        }
    }
    public static boolean validTarget(ItemStack stack, net.minecraft.core.RegistryAccess registries) {
        if (stack.isEmpty() || stack.is(Items.ENCHANTED_BOOK)) return false;
        if (stack.is(Items.BOOK)) return true;
        return registries.registryOrThrow(Registries.ENCHANTMENT).holders().anyMatch(stack::supportsEnchantment);
    }
    public static void returnItem(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack) && !stack.isEmpty()) player.drop(stack, false);
    }
    private AltarInteraction() {}
}
