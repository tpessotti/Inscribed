package dev.inscribed.mixin;
import dev.inscribed.trade.CatalystTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Merchant quick-move can transfer its result before consulting the result slot's pickup policy. */
@Mixin(MerchantMenu.class)
public abstract class CatalystTradeQuickMoveMixin {
    @Shadow @Final private Merchant trader;
    @Inject(method="quickMoveStack",at=@At("HEAD"),cancellable=true)
    private void inscribed$checkCatalystBeforeTransfer(Player player,int slot,CallbackInfoReturnable<ItemStack> ci){
        if(slot==2&&!CatalystTrades.canTake(trader,((MerchantMenu)(Object)this).getSlot(2).getItem()))ci.setReturnValue(ItemStack.EMPTY);
    }
}
