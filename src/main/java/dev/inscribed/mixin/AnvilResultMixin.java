package dev.inscribed.mixin;

import dev.inscribed.anvil.AnvilRestrictionService;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilResultMixin {
    @Inject(method="createResult",at=@At("RETURN"))
    private void inscribed$guardEnchantments(CallbackInfo ci) {
        var menu=(AnvilMenu)(Object)this;
        if(!AnvilRestrictionService.allowed(menu.getSlot(0).getItem(),menu.getSlot(2).getItem()))menu.getSlot(2).set(ItemStack.EMPTY);
    }
}
