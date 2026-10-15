package dev.inscribed.mixin;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** The altar replaces vanilla random enchanting, including menus opened by other mods. */
@Mixin(EnchantmentMenu.class)
public abstract class VanillaEnchantingMixin {
    @Inject(method="clickMenuButton",at=@At("HEAD"),cancellable=true)
    private void inscribed$rejectRandomEnchanting(Player player,int choice,CallbackInfoReturnable<Boolean> result){result.setReturnValue(false);}
}
