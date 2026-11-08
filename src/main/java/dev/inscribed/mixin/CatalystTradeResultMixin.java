package dev.inscribed.mixin;
import dev.inscribed.trade.CatalystTrades;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.*;
/** Recheck permission when a result is picked up, including a menu opened before config was disabled. */
@Mixin(MerchantResultSlot.class)
public abstract class CatalystTradeResultMixin extends Slot {
    @Shadow @Final private Merchant merchant;
    protected CatalystTradeResultMixin(Container container,int slot,int x,int y){super(container,slot,x,y);}
    @Override public boolean mayPickup(Player player) {
        return CatalystTrades.canTake(merchant,getItem()) && super.mayPickup(player);
    }
}
