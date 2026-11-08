package dev.inscribed.mixin;
import dev.inscribed.trade.CatalystTrades;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(AbstractVillager.class)
public abstract class LibrarianCatalystMixin {
    @Inject(method="getOffers",at=@At("RETURN"),cancellable=true)
    private void inscribed$catalystOffer(CallbackInfoReturnable<MerchantOffers> ci) {
        ci.setReturnValue(CatalystTrades.offers((AbstractVillager)(Object)this,ci.getReturnValue()));
    }
}
