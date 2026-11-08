package dev.inscribed.mixin;
import dev.inscribed.trade.*;
import java.lang.ref.WeakReference;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Expose policy-disabled stock without overwriting uses, price, offer order or persisted NBT. */
@Mixin(MerchantOffer.class)
public abstract class CatalystOfferStockMixin implements CatalystOfferPolicy {
    @Unique private WeakReference<Villager> inscribed$owner=new WeakReference<>(null);
    @Override public void inscribed$setOwner(Villager owner){inscribed$owner=new WeakReference<>(owner);}
    @Inject(method="isOutOfStock",at=@At("HEAD"),cancellable=true)
    private void inscribed$stockPolicy(CallbackInfoReturnable<Boolean> ci){
        var owner=inscribed$owner.get();if(owner!=null&&!CatalystTrades.available(owner))ci.setReturnValue(true);
    }
}
